

package org.springframework.amqp.rabbit.listener;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.PossibleAuthenticationFailureException;
import com.rabbitmq.client.ShutdownSignalException;
import org.springframework.amqp.AmqpAuthenticationException;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.AmqpIOException;
import org.springframework.amqp.AmqpIllegalStateException;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.ImmediateAcknowledgeAmqpException;
import org.springframework.amqp.core.BatchMessageListener;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactoryUtils;
import org.springframework.amqp.rabbit.connection.ConsumerChannelRegistry;
import org.springframework.amqp.rabbit.connection.RabbitResourceHolder;
import org.springframework.amqp.rabbit.connection.RabbitUtils;
import org.springframework.amqp.rabbit.connection.SimpleResourceHolder;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareBatchMessageListener;
import org.springframework.amqp.rabbit.listener.exception.FatalListenerExecutionException;
import org.springframework.amqp.rabbit.listener.exception.FatalListenerStartupException;
import org.springframework.amqp.rabbit.support.ActiveObjectCounter;
import org.springframework.amqp.rabbit.support.ConsumerCancelledException;
import org.springframework.amqp.rabbit.support.ListenerContainerAware;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.amqp.rabbit.support.RabbitExceptionTranslator;
import org.springframework.amqp.support.ConsumerTagStrategy;
import org.springframework.jmx.export.annotation.ManagedMetric;
import org.springframework.jmx.support.MetricType;
import org.springframework.lang.Nullable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;
import org.springframework.util.backoff.BackOffExecution;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class SimpleMessageListenerContainer extends AbstractMessageListenerContainer {
    private static final int RECOVERY_LOOP_WAIT_TIME = 200;
    private static final long DEFAULT_START_CONSUMER_MIN_INTERVAL = 10000L;
    private static final long DEFAULT_STOP_CONSUMER_MIN_INTERVAL = 60000L;
    private static final long DEFAULT_CONSUMER_START_TIMEOUT = 60000L;
    private static final int DEFAULT_CONSECUTIVE_ACTIVE_TRIGGER = 10;
    private static final int DEFAULT_CONSECUTIVE_IDLE_TRIGGER = 10;
    public static final long DEFAULT_RECEIVE_TIMEOUT = 1000L;
    private final AtomicLong lastNoMessageAlert = new AtomicLong();
    private final AtomicReference<Thread> containerStoppingForAbort = new AtomicReference();
    private final BlockingQueue<ListenerContainerConsumerFailedEvent> abortEvents = new LinkedBlockingQueue();
    private final ActiveObjectCounter<BlockingQueueConsumer> cancellationLock = new ActiveObjectCounter();
    private long startConsumerMinInterval = 10000L;
    private long stopConsumerMinInterval = 60000L;
    private int consecutiveActiveTrigger = 10;
    private int consecutiveIdleTrigger = 10;
    private int batchSize = 1;
    private boolean consumerBatchEnabled;
    private long receiveTimeout = 1000L;
    private Set<BlockingQueueConsumer> consumers;
    private Integer declarationRetries;
    private Long retryDeclarationInterval;
    private TransactionTemplate transactionTemplate;
    private long consumerStartTimeout = 60000L;
    private volatile int concurrentConsumers = 1;
    private volatile Integer maxConcurrentConsumers;
    private volatile long lastConsumerStarted;
    private volatile long lastConsumerStopped;

    public SimpleMessageListenerContainer() {
    }

    public SimpleMessageListenerContainer(ConnectionFactory connectionFactory) {
        this.setConnectionFactory(connectionFactory);
    }

    public void setConcurrentConsumers(int concurrentConsumers) {
        Assert.isTrue(concurrentConsumers > 0, "'concurrentConsumers' value must be at least 1 (one)");
        Assert.isTrue(!this.isExclusive() || concurrentConsumers == 1, "When the consumer is exclusive, the concurrency must be 1");
        if (this.maxConcurrentConsumers != null) {
            Assert.isTrue(concurrentConsumers <= this.maxConcurrentConsumers, "'concurrentConsumers' cannot be more than 'maxConcurrentConsumers'");
        }

        synchronized(this.consumersMonitor) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Changing consumers from " + this.concurrentConsumers + " to " + concurrentConsumers);
            }

            int delta = this.concurrentConsumers - concurrentConsumers;
            this.concurrentConsumers = concurrentConsumers;
            if (this.isActive()) {
                this.adjustConsumers(delta);
            }

        }
    }

    public void setMaxConcurrentConsumers(int maxConcurrentConsumers) {
        Assert.isTrue(maxConcurrentConsumers >= this.concurrentConsumers, "'maxConcurrentConsumers' value must be at least 'concurrentConsumers'");
        Assert.isTrue(!this.isExclusive() || maxConcurrentConsumers == 1, "When the consumer is exclusive, the concurrency must be 1");
        Integer oldMax = this.maxConcurrentConsumers;
        this.maxConcurrentConsumers = maxConcurrentConsumers;
        if (oldMax != null && this.isActive()) {
            int delta = oldMax - maxConcurrentConsumers;
            if (delta > 0) {
                this.adjustConsumers(delta);
            }
        }

    }

    public void setConcurrency(String concurrency) {
        try {
            int separatorIndex = concurrency.indexOf(45);
            if (separatorIndex != -1) {
                int consumersToSet = Integer.parseInt(concurrency.substring(0, separatorIndex));
                int maxConsumersToSet = Integer.parseInt(concurrency.substring(separatorIndex + 1));
                Assert.isTrue(maxConsumersToSet >= consumersToSet, "'maxConcurrentConsumers' value must be at least 'concurrentConsumers'");
                this.maxConcurrentConsumers = null;
                this.setConcurrentConsumers(consumersToSet);
                this.setMaxConcurrentConsumers(maxConsumersToSet);
            } else {
                this.setConcurrentConsumers(Integer.parseInt(concurrency));
            }

        } catch (NumberFormatException var5) {
            throw new IllegalArgumentException("Invalid concurrency value [" + concurrency + "]: only single fixed integer (e.g. \"5\") and minimum-maximum combo (e.g. \"3-5\") supported.", var5);
        }
    }

    public final void setExclusive(boolean exclusive) {
        Assert.isTrue(!exclusive || this.concurrentConsumers == 1 && (this.maxConcurrentConsumers == null || this.maxConcurrentConsumers == 1), "When the consumer is exclusive, the concurrency must be 1");
        super.setExclusive(exclusive);
    }

    public final void setStartConsumerMinInterval(long startConsumerMinInterval) {
        Assert.isTrue(startConsumerMinInterval > 0L, "'startConsumerMinInterval' must be > 0");
        this.startConsumerMinInterval = startConsumerMinInterval;
    }

    public final void setStopConsumerMinInterval(long stopConsumerMinInterval) {
        Assert.isTrue(stopConsumerMinInterval > 0L, "'stopConsumerMinInterval' must be > 0");
        this.stopConsumerMinInterval = stopConsumerMinInterval;
    }

    public final void setConsecutiveActiveTrigger(int consecutiveActiveTrigger) {
        Assert.isTrue(consecutiveActiveTrigger > 0, "'consecutiveActiveTrigger' must be > 0");
        this.consecutiveActiveTrigger = consecutiveActiveTrigger;
    }

    public final void setConsecutiveIdleTrigger(int consecutiveIdleTrigger) {
        Assert.isTrue(consecutiveIdleTrigger > 0, "'consecutiveIdleTrigger' must be > 0");
        this.consecutiveIdleTrigger = consecutiveIdleTrigger;
    }

    public void setReceiveTimeout(long receiveTimeout) {
        this.receiveTimeout = receiveTimeout;
    }

    public void setBatchSize(int batchSize) {
        Assert.isTrue(batchSize > 0, "'batchSize' must be > 0");
        this.batchSize = batchSize;
    }

    public void setConsumerBatchEnabled(boolean consumerBatchEnabled) {
        this.consumerBatchEnabled = consumerBatchEnabled;
    }

    public boolean isConsumerBatchEnabled() {
        return this.consumerBatchEnabled;
    }

    public void setMissingQueuesFatal(boolean missingQueuesFatal) {
        super.setMissingQueuesFatal(missingQueuesFatal);
    }

    public void setQueueNames(String... queueName) {
        super.setQueueNames(queueName);
        this.queuesChanged();
    }

    public void addQueueNames(String... queueName) {
        super.addQueueNames(queueName);
    }

    public boolean removeQueueNames(String... queueName) {
        if (super.removeQueueNames(queueName)) {
            this.queuesChanged();
            return true;
        } else {
            return false;
        }
    }

    public void addQueues(Queue... queue) {
        super.addQueues(queue);
        this.queuesChanged();
    }

    public boolean removeQueues(Queue... queue) {
        if (super.removeQueues(queue)) {
            this.queuesChanged();
            return true;
        } else {
            return false;
        }
    }

    public void setDeclarationRetries(int declarationRetries) {
        this.declarationRetries = declarationRetries;
    }

    public void setRetryDeclarationInterval(long retryDeclarationInterval) {
        this.retryDeclarationInterval = retryDeclarationInterval;
    }

    public void setConsumerStartTimeout(long consumerStartTimeout) {
        this.consumerStartTimeout = consumerStartTimeout;
    }

    protected void validateConfiguration() {
        super.validateConfiguration();
        Assert.state(!this.getAcknowledgeMode().isAutoAck() || this.getTransactionManager() == null, "The acknowledgeMode is NONE (autoack in Rabbit terms) which is not consistent with having an external transaction manager. Either use a different AcknowledgeMode or make sure the transactionManager is null.");
    }

    protected final boolean sharedConnectionEnabled() {
        return true;
    }

    protected void doInitialize() {
        if (this.consumerBatchEnabled) {
            this.setDeBatchingEnabled(true);
        }

    }

    @ManagedMetric(
            metricType = MetricType.GAUGE
    )
    public int getActiveConsumerCount() {
        return this.cancellationLock.getCount();
    }

    protected void doStart() {
        Assert.state(!this.consumerBatchEnabled || this.getMessageListener() instanceof BatchMessageListener || this.getMessageListener() instanceof ChannelAwareBatchMessageListener, "When setting 'consumerBatchEnabled' to true, the listener must support batching");
        this.checkListenerContainerAware();
        super.doStart();
        synchronized(this.consumersMonitor) {
            if (this.consumers != null) {
                throw new IllegalStateException("A stopped container should not have consumers");
            } else {
                int newConsumers = this.initializeConsumers();
                if (this.consumers == null) {
                    this.logger.info("Consumers were initialized and then cleared (presumably the container was stopped concurrently)");
                } else if (newConsumers <= 0) {
                    if (this.logger.isInfoEnabled()) {
                        this.logger.info("Consumers are already running");
                    }

                } else {
                    Set<SimpleMessageListenerContainer.AsyncMessageProcessingConsumer> processors = new HashSet();
                    Iterator var4 = this.consumers.iterator();

                    while(var4.hasNext()) {
                        BlockingQueueConsumer consumer = (BlockingQueueConsumer)var4.next();
                        SimpleMessageListenerContainer.AsyncMessageProcessingConsumer processor = new SimpleMessageListenerContainer.AsyncMessageProcessingConsumer(consumer);
                        processors.add(processor);
                        this.getTaskExecutor().execute(processor);
                        if (this.getApplicationEventPublisher() != null) {
                            this.getApplicationEventPublisher().publishEvent(new AsyncConsumerStartedEvent(this, consumer));
                        }
                    }

                    this.waitForConsumersToStart(processors);
                }
            }
        }
    }

    private void checkListenerContainerAware() {
        Object messageListener = this.getMessageListener();
        if (messageListener instanceof ListenerContainerAware) {
            Collection<String> expectedQueueNames = ((ListenerContainerAware)messageListener).expectedQueueNames();
            if (expectedQueueNames != null) {
                String[] queueNames = this.getQueueNames();
                Assert.state(expectedQueueNames.size() == queueNames.length, "Listener expects us to be listening on '" + expectedQueueNames + "'; our queues: " + Arrays.asList(queueNames));
                boolean found = false;
                String[] var5 = queueNames;
                int var6 = queueNames.length;

                for(int var7 = 0; var7 < var6; ++var7) {
                    String queueName = var5[var7];
                    if (!expectedQueueNames.contains(queueName)) {
                        found = false;
                        break;
                    }

                    found = true;
                }

                Assert.state(found, () -> {
                    return "Listener expects us to be listening on '" + expectedQueueNames + "'; our queues: " + Arrays.asList(queueNames);
                });
            }
        }

    }

    private void waitForConsumersToStart(Set<SimpleMessageListenerContainer.AsyncMessageProcessingConsumer> processors) {
        Iterator var2 = processors.iterator();

        while(var2.hasNext()) {
            SimpleMessageListenerContainer.AsyncMessageProcessingConsumer processor = (SimpleMessageListenerContainer.AsyncMessageProcessingConsumer)var2.next();
            FatalListenerStartupException startupException = null;

            try {
                startupException = processor.getStartupException();
            } catch (InterruptedException var6) {
                Thread.currentThread().interrupt();
                throw RabbitExceptionTranslator.convertRabbitAccessException(var6);
            }

            if (startupException != null) {
                throw new AmqpIllegalStateException("Fatal exception on listener startup", startupException);
            }
        }

    }

    protected void doShutdown() {
        Thread thread = (Thread)this.containerStoppingForAbort.get();
        if (thread != null && !thread.equals(Thread.currentThread())) {
            this.logger.info("Shutdown ignored - container is stopping due to an aborted consumer");
        } else {
            try {
                List<BlockingQueueConsumer> canceledConsumers = new ArrayList();
                synchronized(this.consumersMonitor) {
                    if (this.consumers == null) {
                        this.logger.info("Shutdown ignored - container is already stopped");
                        return;
                    }

                    Iterator consumerIterator = this.consumers.iterator();

                    while(true) {
                        if (!consumerIterator.hasNext()) {
                            break;
                        }

                        BlockingQueueConsumer consumer = (BlockingQueueConsumer)consumerIterator.next();
                        consumer.basicCancel(true);
                        canceledConsumers.add(consumer);
                        consumerIterator.remove();
                        if (consumer.declaring) {
                            consumer.thread.interrupt();
                        }
                    }
                }

                this.logger.info("Waiting for workers to finish.");
                boolean finished = this.cancellationLock.await(this.getShutdownTimeout(), TimeUnit.MILLISECONDS);
                if (finished) {
                    this.logger.info("Successfully waited for workers to finish.");
                } else {
                    this.logger.info("Workers not finished.");
                    if (this.isForceCloseChannel()) {
                        canceledConsumers.forEach((consumerx) -> {
                            if (this.logger.isWarnEnabled()) {
                                this.logger.warn("Closing channel for unresponsive consumer: " + consumerx);
                            }

                            consumerx.stop();
                        });
                    }
                }
            } catch (InterruptedException var10) {
                Thread.currentThread().interrupt();
                this.logger.warn("Interrupted waiting for workers.  Continuing with shutdown.");
            }

            synchronized(this.consumersMonitor) {
                this.consumers = null;
                this.cancellationLock.deactivate();
            }
        }
    }

    private boolean isActive(BlockingQueueConsumer consumer) {
        boolean consumerActive;
        synchronized(this.consumersMonitor) {
            consumerActive = this.consumers != null && this.consumers.contains(consumer);
        }

        return consumerActive && this.isActive();
    }

    protected int initializeConsumers() {
        int count = 0;
        synchronized(this.consumersMonitor) {
            if (this.consumers == null) {
                this.cancellationLock.reset();
                this.consumers = new HashSet(this.concurrentConsumers);

                for(int i = 1; i <= this.concurrentConsumers; ++i) {
                    BlockingQueueConsumer consumer = this.createBlockingQueueConsumer();
                    if (this.getConsumeDelay() > 0L) {
                        consumer.setConsumeDelay(this.getConsumeDelay() * (long)i);
                    }

                    this.consumers.add(consumer);
                    ++count;
                }
            }

            return count;
        }
    }

    protected void adjustConsumers(int deltaArg) {
        int delta = deltaArg;
        synchronized(this.consumersMonitor) {
            if (this.isActive() && this.consumers != null) {
                if (delta > 0) {
                    for(Iterator consumerIterator = this.consumers.iterator(); consumerIterator.hasNext() && delta > 0 && (this.maxConcurrentConsumers == null || this.consumers.size() > this.maxConcurrentConsumers); --delta) {
                        BlockingQueueConsumer consumer = (BlockingQueueConsumer)consumerIterator.next();
                        consumer.basicCancel(true);
                        consumerIterator.remove();
                    }
                } else {
                    this.addAndStartConsumers(-delta);
                }
            }

        }
    }

    protected void addAndStartConsumers(int delta) {
        synchronized(this.consumersMonitor) {
            if (this.consumers != null) {
                for(int i = 0; i < delta && (this.maxConcurrentConsumers == null || this.consumers.size() < this.maxConcurrentConsumers); ++i) {
                    BlockingQueueConsumer consumer = this.createBlockingQueueConsumer();
                    this.consumers.add(consumer);
                    SimpleMessageListenerContainer.AsyncMessageProcessingConsumer processor = new SimpleMessageListenerContainer.AsyncMessageProcessingConsumer(consumer);
                    if (this.logger.isDebugEnabled()) {
                        this.logger.debug("Starting a new consumer: " + consumer);
                    }

                    this.getTaskExecutor().execute(processor);
                    if (this.getApplicationEventPublisher() != null) {
                        this.getApplicationEventPublisher().publishEvent(new AsyncConsumerStartedEvent(this, consumer));
                    }

                    try {
                        FatalListenerStartupException startupException = processor.getStartupException();
                        if (startupException != null) {
                            this.consumers.remove(consumer);
                            throw new AmqpIllegalStateException("Fatal exception on listener startup", startupException);
                        }
                    } catch (InterruptedException var8) {
                        Thread.currentThread().interrupt();
                    } catch (Exception var9) {
                        consumer.stop();
                        this.logger.error("Error starting new consumer", var9);
                        this.cancellationLock.release(consumer);
                        this.consumers.remove(consumer);
                    }
                }
            }

        }
    }

    private void considerAddingAConsumer() {
        synchronized(this.consumersMonitor) {
            if (this.consumers != null && this.maxConcurrentConsumers != null && this.consumers.size() < this.maxConcurrentConsumers) {
                long now = System.currentTimeMillis();
                if (this.lastConsumerStarted + this.startConsumerMinInterval < now) {
                    this.addAndStartConsumers(1);
                    this.lastConsumerStarted = now;
                }
            }

        }
    }

    private void considerStoppingAConsumer(BlockingQueueConsumer consumer) {
        synchronized(this.consumersMonitor) {
            if (this.consumers != null && this.consumers.size() > this.concurrentConsumers) {
                long now = System.currentTimeMillis();
                if (this.lastConsumerStopped + this.stopConsumerMinInterval < now) {
                    consumer.basicCancel(true);
                    this.consumers.remove(consumer);
                    if (this.logger.isDebugEnabled()) {
                        this.logger.debug("Idle consumer terminating: " + consumer);
                    }

                    this.lastConsumerStopped = now;
                }
            }

        }
    }

    private void queuesChanged() {
        synchronized(this.consumersMonitor) {
            if (this.consumers != null) {
                int count = 0;

                for(Iterator consumerIterator = this.consumers.iterator(); consumerIterator.hasNext(); ++count) {
                    BlockingQueueConsumer consumer = (BlockingQueueConsumer)consumerIterator.next();
                    if (this.logger.isDebugEnabled()) {
                        this.logger.debug("Queues changed; stopping consumer: " + consumer);
                    }

                    consumer.basicCancel(true);
                    consumerIterator.remove();
                }

                try {
                    this.cancellationLock.await(this.getShutdownTimeout(), TimeUnit.MILLISECONDS);
                } catch (InterruptedException var6) {
                    Thread.currentThread().interrupt();
                }

                this.addAndStartConsumers(count);
            }

        }
    }

    protected BlockingQueueConsumer createBlockingQueueConsumer() {
        String[] queues = this.getQueueNames();
        int actualPrefetchCount = this.getPrefetchCount() > this.batchSize ? this.getPrefetchCount() : this.batchSize;
        BlockingQueueConsumer consumer = new BlockingQueueConsumer(this.getConnectionFactory(), this.getMessagePropertiesConverter(), this.cancellationLock, this.getAcknowledgeMode(), this.isChannelTransacted(), actualPrefetchCount, this.isDefaultRequeueRejected(), this.getConsumerArguments(), this.isNoLocal(), this.isExclusive(), queues);
        consumer.setGlobalQos(this.isGlobalQos());
        consumer.setMissingQueuePublisher(this::publishMissingQueueEvent);
        if (this.declarationRetries != null) {
            consumer.setDeclarationRetries(this.declarationRetries);
        }

        if (this.getFailedDeclarationRetryInterval() > 0L) {
            consumer.setFailedDeclarationRetryInterval(this.getFailedDeclarationRetryInterval());
        }

        if (this.retryDeclarationInterval != null) {
            consumer.setRetryDeclarationInterval(this.retryDeclarationInterval);
        }

        ConsumerTagStrategy consumerTagStrategy = this.getConsumerTagStrategy();
        if (consumerTagStrategy != null) {
            consumer.setTagStrategy(consumerTagStrategy);
        }

        consumer.setBackOffExecution(this.getRecoveryBackOff().start());
        consumer.setShutdownTimeout(this.getShutdownTimeout());
        consumer.setApplicationEventPublisher(this.getApplicationEventPublisher());
        return consumer;
    }

    private void restart(BlockingQueueConsumer oldConsumer) {
        BlockingQueueConsumer consumer = oldConsumer;
        synchronized(this.consumersMonitor) {
            if (this.consumers != null) {
                try {
                    consumer.stop();
                    this.cancellationLock.release(consumer);
                    this.consumers.remove(consumer);
                    if (!this.isActive()) {
                        return;
                    }

                    BlockingQueueConsumer newConsumer = this.createBlockingQueueConsumer();
                    newConsumer.setBackOffExecution(consumer.getBackOffExecution());
                    consumer = newConsumer;
                    this.consumers.add(newConsumer);
                    if (this.getApplicationEventPublisher() != null) {
                        this.getApplicationEventPublisher().publishEvent(new AsyncConsumerRestartedEvent(this, oldConsumer, newConsumer));
                    }
                } catch (RuntimeException var6) {
                    this.logger.warn("Consumer failed irretrievably on restart. " + var6.getClass() + ": " + var6.getMessage());
                    throw var6;
                }

                this.getTaskExecutor().execute(new SimpleMessageListenerContainer.AsyncMessageProcessingConsumer(consumer));
            }

        }
    }

    private boolean receiveAndExecute(BlockingQueueConsumer consumer) throws Exception {
        PlatformTransactionManager transactionManager = this.getTransactionManager();
        if (transactionManager != null) {
            try {
                if (this.transactionTemplate == null) {
                    this.transactionTemplate = new TransactionTemplate(transactionManager, this.getTransactionAttribute());
                }

                return (Boolean)this.transactionTemplate.execute((status) -> {
                    RabbitResourceHolder resourceHolder = ConnectionFactoryUtils.bindResourceToTransaction(new RabbitResourceHolder(consumer.getChannel(), false), this.getConnectionFactory(), true);

                    try {
                        return this.doReceiveAndExecute(consumer);
                    } catch (RuntimeException var5) {
                        this.prepareHolderForRollback(resourceHolder, var5);
                        throw var5;
                    } catch (Exception var6) {
                        throw new WrappedTransactionException(var6);
                    }
                });
            } catch (WrappedTransactionException var4) {
                throw (Exception)var4.getCause();
            }
        } else {
            return this.doReceiveAndExecute(consumer);
        }
    }

    private boolean doReceiveAndExecute(BlockingQueueConsumer consumer) throws Exception {
        Channel channel = consumer.getChannel();
        List<Message> messages = null;
        long deliveryTag = 0L;

        for(int i = 0; i < this.batchSize; ++i) {
            this.logger.trace("Waiting for message from consumer.");
            Message message = consumer.nextMessage(this.receiveTimeout);
            if (message == null) {
                break;
            }

            if (this.consumerBatchEnabled) {
                Collection<MessagePostProcessor> afterReceivePostProcessors = this.getAfterReceivePostProcessors();
                if (afterReceivePostProcessors != null) {
                    Message original = message;
                    deliveryTag = message.getMessageProperties().getDeliveryTag();
                    Iterator var10 = this.getAfterReceivePostProcessors().iterator();

                    while(var10.hasNext()) {
                        MessagePostProcessor processor = (MessagePostProcessor)var10.next();
                        message = processor.postProcessMessage(message);
                        if (message == null) {
                            if (this.logger.isDebugEnabled()) {
                                this.logger.debug("Message Post Processor returned 'null', discarding message " + original);
                            }
                            break;
                        }
                    }
                }

                if (message != null) {
                    if (messages == null) {
                        messages = new ArrayList(this.batchSize);
                    }

                    final var myMessages = messages;
                    if (this.isDeBatchingEnabled() && this.getBatchingStrategy().canDebatch(message.getMessageProperties())) {
                        this.getBatchingStrategy().deBatch(message, (fragment) -> {
                            myMessages.add(fragment);
                        });
                    } else {
                        ((List)messages).add(message);
                    }
                }
            } else {
                messages = this.debatch(message);
                if (messages != null) {
                    break;
                }

                try {
                    this.executeListener(channel, message);
                } catch (ImmediateAcknowledgeAmqpException var12) {
                    if (this.logger.isDebugEnabled()) {
                        this.logger.debug("User requested ack for failed delivery '" + var12.getMessage() + "': " + message.getMessageProperties().getDeliveryTag());
                    }
                    break;
                } catch (Exception var13) {
                    if (this.causeChainHasImmediateAcknowledgeAmqpException(var13)) {
                        if (this.logger.isDebugEnabled()) {
                            this.logger.debug("User requested ack for failed delivery: " + message.getMessageProperties().getDeliveryTag());
                        }
                    } else {
                        long tagToRollback = this.isAsyncReplies() ? message.getMessageProperties().getDeliveryTag() : -1L;
                        if (this.getTransactionManager() == null) {
                            consumer.rollbackOnExceptionIfNecessary(var13, tagToRollback);
                            throw var13;
                        }

                        if (this.getTransactionAttribute().rollbackOn(var13)) {
                            RabbitResourceHolder resourceHolder = (RabbitResourceHolder)TransactionSynchronizationManager.getResource(this.getConnectionFactory());
                            if (resourceHolder != null) {
                                consumer.clearDeliveryTags();
                            } else {
                                consumer.rollbackOnExceptionIfNecessary(var13, tagToRollback);
                            }

                            throw var13;
                        }

                        if (this.logger.isDebugEnabled()) {
                            this.logger.debug("No rollback for " + var13);
                        }
                    }
                    break;
                }
            }
        }

        if (messages != null) {
            this.executeWithList(channel, (List)messages, deliveryTag, consumer);
        }

        return consumer.commitIfNecessary(this.isChannelLocallyTransacted());
    }

    private void executeWithList(Channel channel, List<Message> messages, long deliveryTag, BlockingQueueConsumer consumer) {
        try {
            this.executeListener(channel, messages);
        } catch (ImmediateAcknowledgeAmqpException var8) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("User requested ack for failed delivery '" + var8.getMessage() + "' (last in batch): " + deliveryTag);
            }

            return;
        } catch (Exception var9) {
            if (this.causeChainHasImmediateAcknowledgeAmqpException(var9)) {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("User requested ack for failed delivery (last in batch): " + deliveryTag);
                }

                return;
            }

            if (this.getTransactionManager() == null) {
                consumer.rollbackOnExceptionIfNecessary(var9);
                throw var9;
            }

            if (this.getTransactionAttribute().rollbackOn(var9)) {
                RabbitResourceHolder resourceHolder = (RabbitResourceHolder)TransactionSynchronizationManager.getResource(this.getConnectionFactory());
                if (resourceHolder != null) {
                    consumer.clearDeliveryTags();
                } else {
                    consumer.rollbackOnExceptionIfNecessary(var9);
                }

                throw var9;
            }

            if (this.logger.isDebugEnabled()) {
                this.logger.debug("No rollback for " + var9);
            }
        }

    }

    protected void handleStartupFailure(BackOffExecution backOffExecution) {
        long recoveryInterval = backOffExecution.nextBackOff();
        if (-1L == recoveryInterval) {
            synchronized(this) {
                if (this.isActive()) {
                    this.logger.warn("stopping container - restart recovery attempts exhausted");
                    this.stop();
                }

            }
        } else {
            try {
                if (this.logger.isDebugEnabled() && this.isActive()) {
                    this.logger.debug("Recovering consumer in " + recoveryInterval + " ms.");
                }

                long timeout = System.currentTimeMillis() + recoveryInterval;

                while(this.isActive() && System.currentTimeMillis() < timeout) {
                    Thread.sleep(200L);
                }

            } catch (InterruptedException var7) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Irrecoverable interruption on consumer restart", var7);
            }
        }
    }

    protected void publishConsumerFailedEvent(String reason, boolean fatal, @Nullable Throwable t) {
        if (fatal && this.isRunning()) {
            try {
                this.abortEvents.put(new ListenerContainerConsumerFailedEvent(this, reason, t, fatal));
            } catch (InterruptedException var5) {
                Thread.currentThread().interrupt();
            }
        } else {
            super.publishConsumerFailedEvent(reason, fatal, t);
        }

    }

    public String toString() {
        return "MySimpleMessageListenerContainer " + (this.getBeanName() != null ? "(" + this.getBeanName() + ") " : "") + "[concurrentConsumers=" + this.concurrentConsumers + (this.maxConcurrentConsumers != null ? ", maxConcurrentConsumers=" + this.maxConcurrentConsumers : "") + ", queueNames=" + Arrays.toString(this.getQueueNames()) + "]";
    }

    private final class AsyncMessageProcessingConsumer implements Runnable {
        private static final int ABORT_EVENT_WAIT_SECONDS = 5;
        private final BlockingQueueConsumer consumer;
        private final CountDownLatch start;
        private volatile FatalListenerStartupException startupException;
        private int consecutiveIdles;
        private int consecutiveMessages;

        AsyncMessageProcessingConsumer(BlockingQueueConsumer consumer) {
            this.consumer = consumer;
            this.start = new CountDownLatch(1);
        }

        private FatalListenerStartupException getStartupException() throws InterruptedException {
            if (!this.start.await(SimpleMessageListenerContainer.this.consumerStartTimeout, TimeUnit.MILLISECONDS)) {
                SimpleMessageListenerContainer.this.logger.error("Consumer failed to start in " + SimpleMessageListenerContainer.this.consumerStartTimeout + " milliseconds; does the task executor have enough threads to support the container concurrency?");
            }

            return this.startupException;
        }

        public void run() {
            if (SimpleMessageListenerContainer.this.isActive()) {
                boolean aborted = false;
                this.consumer.setLocallyTransacted(SimpleMessageListenerContainer.this.isChannelLocallyTransacted());
                String routingLookupKey = SimpleMessageListenerContainer.this.getRoutingLookupKey();
                if (routingLookupKey != null) {
                    SimpleResourceHolder.bind(SimpleMessageListenerContainer.this.getRoutingConnectionFactory(), routingLookupKey);
                }

                if (this.consumer.getQueueCount() < 1) {
                    if (SimpleMessageListenerContainer.this.logger.isDebugEnabled()) {
                        SimpleMessageListenerContainer.this.logger.debug("Consumer stopping; no queues for " + this.consumer);
                    }

                    SimpleMessageListenerContainer.this.cancellationLock.release(this.consumer);
                    if (SimpleMessageListenerContainer.this.getApplicationEventPublisher() != null) {
                        SimpleMessageListenerContainer.this.getApplicationEventPublisher().publishEvent(new AsyncConsumerStoppedEvent(SimpleMessageListenerContainer.this, this.consumer));
                    }

                    this.start.countDown();
                } else {
                    try {
                        this.initialize();

                        while(SimpleMessageListenerContainer.this.isActive(this.consumer) || this.consumer.hasDelivery() || !this.consumer.cancelled()) {
                            this.mainLoop();
                        }
                    } catch (InterruptedException var15) {
                        SimpleMessageListenerContainer.this.logger.debug("Consumer thread interrupted, processing stopped.");
                        Thread.currentThread().interrupt();
                        aborted = true;
                        SimpleMessageListenerContainer.this.publishConsumerFailedEvent("Consumer thread interrupted, processing stopped", true, var15);
                    } catch (QueuesNotAvailableException var16) {
                        SimpleMessageListenerContainer.this.logger.error("Consumer threw missing queues exception, fatal=" + SimpleMessageListenerContainer.this.isMissingQueuesFatal(), var16);
                        if (SimpleMessageListenerContainer.this.isMissingQueuesFatal()) {
                            this.startupException = var16;
                            aborted = true;
                        }

                        SimpleMessageListenerContainer.this.publishConsumerFailedEvent("Consumer queue(s) not available", aborted, var16);
                    } catch (FatalListenerStartupException var17) {
                        SimpleMessageListenerContainer.this.logger.error("Consumer received fatal exception on startup", var17);
                        this.startupException = var17;
                        aborted = true;
                        SimpleMessageListenerContainer.this.publishConsumerFailedEvent("Consumer received fatal exception on startup", true, var17);
                    } catch (FatalListenerExecutionException var18) {
                        SimpleMessageListenerContainer.this.logger.error("Consumer received fatal exception during processing", var18);
                        aborted = true;
                        SimpleMessageListenerContainer.this.publishConsumerFailedEvent("Consumer received fatal exception during processing", true, var18);
                    } catch (PossibleAuthenticationFailureException var19) {
                        SimpleMessageListenerContainer.this.logger.error("Consumer received fatal=" + SimpleMessageListenerContainer.this.isPossibleAuthenticationFailureFatal() + " exception during processing", var19);
                        if (SimpleMessageListenerContainer.this.isPossibleAuthenticationFailureFatal()) {
                            this.startupException = new FatalListenerStartupException("Authentication failure", new AmqpAuthenticationException(var19));
                            aborted = true;
                        }

                        SimpleMessageListenerContainer.this.publishConsumerFailedEvent("Consumer received PossibleAuthenticationFailure during startup", aborted, var19);
                    } catch (ShutdownSignalException var20) {
                        if (RabbitUtils.isNormalShutdown(var20)) {
                            if (SimpleMessageListenerContainer.this.logger.isDebugEnabled()) {
                                SimpleMessageListenerContainer.this.logger.debug("Consumer received Shutdown Signal, processing stopped: " + var20.getMessage());
                            }
                        } else {
                            this.logConsumerException(var20);
                        }
                    } catch (AmqpIOException var21) {
                        if (var21.getCause() instanceof IOException && var21.getCause().getCause() instanceof ShutdownSignalException && var21.getCause().getCause().getMessage().contains("in exclusive use")) {
                            SimpleMessageListenerContainer.this.getExclusiveConsumerExceptionLogger().log(SimpleMessageListenerContainer.this.logger, "Exclusive consumer failure", var21.getCause().getCause());
                            SimpleMessageListenerContainer.this.publishConsumerFailedEvent("Consumer raised exception, attempting restart", false, var21);
                        } else {
                            this.logConsumerException(var21);
                        }
                    } catch (Error var22) {
                        SimpleMessageListenerContainer.this.logger.error("Consumer thread error, thread abort.", var22);
                        SimpleMessageListenerContainer.this.publishConsumerFailedEvent("Consumer threw an Error", true, var22);
                        SimpleMessageListenerContainer.this.getJavaLangErrorHandler().handle(var22);
                        aborted = true;
                    } catch (Throwable var23) {
                        if (SimpleMessageListenerContainer.this.isActive()) {
                            this.logConsumerException(var23);
                        }
                    } finally {
                        if (SimpleMessageListenerContainer.this.getTransactionManager() != null) {
                            ConsumerChannelRegistry.unRegisterConsumerChannel();
                        }

                    }

                    this.start.countDown();
                    this.killOrRestart(aborted);
                    if (routingLookupKey != null) {
                        SimpleResourceHolder.unbind(SimpleMessageListenerContainer.this.getRoutingConnectionFactory());
                    }

                }
            }
        }

        private void mainLoop() throws Exception {
            try {
                boolean receivedOk = SimpleMessageListenerContainer.this.receiveAndExecute(this.consumer);
                if (SimpleMessageListenerContainer.this.maxConcurrentConsumers != null) {
                    this.checkAdjust(receivedOk);
                }

                long idleEventInterval = SimpleMessageListenerContainer.this.getIdleEventInterval();
                if (idleEventInterval > 0L) {
                    if (receivedOk) {
                        SimpleMessageListenerContainer.this.updateLastReceive();
                    } else {
                        long now = System.currentTimeMillis();
                        long lastAlertAt = SimpleMessageListenerContainer.this.lastNoMessageAlert.get();
                        long lastReceive = SimpleMessageListenerContainer.this.getLastReceive();
                        if (now > lastReceive + idleEventInterval && now > lastAlertAt + idleEventInterval && SimpleMessageListenerContainer.this.lastNoMessageAlert.compareAndSet(lastAlertAt, now)) {
                            SimpleMessageListenerContainer.this.publishIdleContainerEvent(now - lastReceive);
                        }
                    }
                }
            } catch (ListenerExecutionFailedException var10) {
                if (var10.getCause() instanceof NoSuchMethodException) {
                    throw new FatalListenerExecutionException("Invalid listener", var10);
                }
            } catch (AmqpRejectAndDontRequeueException var11) {
            }

        }

        private void checkAdjust(boolean receivedOk) {
            if (receivedOk) {
                if (SimpleMessageListenerContainer.this.isActive(this.consumer)) {
                    this.consecutiveIdles = 0;
                    if (this.consecutiveMessages++ > SimpleMessageListenerContainer.this.consecutiveActiveTrigger) {
                        SimpleMessageListenerContainer.this.considerAddingAConsumer();
                        this.consecutiveMessages = 0;
                    }
                }
            } else {
                this.consecutiveMessages = 0;
                if (this.consecutiveIdles++ > SimpleMessageListenerContainer.this.consecutiveIdleTrigger) {
                    SimpleMessageListenerContainer.this.considerStoppingAConsumer(this.consumer);
                    this.consecutiveIdles = 0;
                }
            }

        }

        private void initialize() throws Throwable {
            try {
                SimpleMessageListenerContainer.this.redeclareElementsIfNecessary();
                this.consumer.start();
                this.start.countDown();
            } catch (QueuesNotAvailableException var3) {
                if (SimpleMessageListenerContainer.this.isMissingQueuesFatal()) {
                    throw var3;
                }

                this.start.countDown();
                SimpleMessageListenerContainer.this.handleStartupFailure(this.consumer.getBackOffExecution());
                throw var3;
            } catch (FatalListenerStartupException var4) {
                if (SimpleMessageListenerContainer.this.isPossibleAuthenticationFailureFatal()) {
                    throw var4;
                }

                Throwable possibleAuthException = var4.getCause().getCause();
                if (!(possibleAuthException instanceof PossibleAuthenticationFailureException)) {
                    throw var4;
                }

                this.start.countDown();
                SimpleMessageListenerContainer.this.handleStartupFailure(this.consumer.getBackOffExecution());
                throw possibleAuthException;
            } catch (Throwable var5) {
                this.start.countDown();
                SimpleMessageListenerContainer.this.handleStartupFailure(this.consumer.getBackOffExecution());
                throw var5;
            }

            if (SimpleMessageListenerContainer.this.getTransactionManager() != null) {
                ConsumerChannelRegistry.registerConsumerChannel(this.consumer.getChannel(), SimpleMessageListenerContainer.this.getConnectionFactory());
            }

        }

        private void killOrRestart(boolean aborted) {
            if (SimpleMessageListenerContainer.this.isActive(this.consumer) && !aborted) {
                SimpleMessageListenerContainer.this.logger.info("Restarting " + this.consumer);
                SimpleMessageListenerContainer.this.restart(this.consumer);
            } else {
                SimpleMessageListenerContainer.this.logger.debug("Cancelling " + this.consumer);

                try {
                    this.consumer.stop();
                    SimpleMessageListenerContainer.this.cancellationLock.release(this.consumer);
                    if (SimpleMessageListenerContainer.this.getApplicationEventPublisher() != null) {
                        SimpleMessageListenerContainer.this.getApplicationEventPublisher().publishEvent(new AsyncConsumerStoppedEvent(SimpleMessageListenerContainer.this, this.consumer));
                    }
                } catch (AmqpException var5) {
                    SimpleMessageListenerContainer.this.logger.info("Could not cancel message consumer", var5);
                }

                if (aborted && SimpleMessageListenerContainer.this.containerStoppingForAbort.compareAndSet(null, Thread.currentThread())) {
                    SimpleMessageListenerContainer.this.logger.error("Stopping container from aborted consumer");
                    SimpleMessageListenerContainer.this.stop();
                    SimpleMessageListenerContainer.this.containerStoppingForAbort.set(null);
                    ListenerContainerConsumerFailedEvent event = null;

                    do {
                        try {
                            event = (ListenerContainerConsumerFailedEvent) SimpleMessageListenerContainer.this.abortEvents.poll(5L, TimeUnit.SECONDS);
                            if (event != null) {
                                SimpleMessageListenerContainer.this.publishConsumerFailedEvent(event.getReason(), event.isFatal(), event.getThrowable());
                            }
                        } catch (InterruptedException var4) {
                            Thread.currentThread().interrupt();
                        }
                    } while(event != null);
                }
            }

        }

        private void logConsumerException(Throwable t) {
            if (SimpleMessageListenerContainer.this.logger.isDebugEnabled() || !(t instanceof AmqpConnectException) && !(t instanceof ConsumerCancelledException)) {
                SimpleMessageListenerContainer.this.logger.debug("Consumer raised exception, processing can restart if the connection factory supports it", t);
            } else if (t instanceof ConsumerCancelledException && this.consumer.isNormalCancel()) {
                if (SimpleMessageListenerContainer.this.logger.isDebugEnabled()) {
                    SimpleMessageListenerContainer.this.logger.debug("Consumer raised exception, processing can restart if the connection factory supports it. Exception summary: " + t);
                }
            } else if (SimpleMessageListenerContainer.this.logger.isWarnEnabled()) {
                SimpleMessageListenerContainer.this.logger.warn("Consumer raised exception, processing can restart if the connection factory supports it. Exception summary: " + t);
            }

            SimpleMessageListenerContainer.this.publishConsumerFailedEvent("Consumer raised exception, attempting restart", false, t);
        }
    }
}
