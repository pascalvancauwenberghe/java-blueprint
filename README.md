# java-blueprint
Blueprint to demonstrate and experiment with async design in java

## Prerequisites

RabbitMQ running with its standard ports exposed
Use the docker container with management console (rabbitmq:3-management) to see management console on http://127.0.0.1:15672/
See https://www.rabbitmq.com/management.html#permissions to create a management user and giving them access to the console

# Flows
## Basic Job Creator and Performer

scheduler.BasicJobCron calls processor.BasicJobSupplier to put a BasicJob (created by usecase.CreateBasicJob) in queue Blueprint.Scheduled.BasicJobs every second with a TTL of 2 seconds

DroppableJobExecutor<BasicJob> reads from queue Blueprint.Scheduled.BasicJobs and calls usecase.PerformBasicJob which takes 4 seconds to process one BasicJob.

This would normally result in queue Blueprint.Scheduled.BasicJobs filling up with unprocessed BasicJobs. But BasicJobs whose TTL has expired are not processed, they are dropped. An IPerformDroppableWork has the opportunity to perform some (very fast) action when a job is to be dropped. 

CRON every 1s => BasicJobSupplier -> Blueprint.Scheduled.BasicJobs -> DroppableExecutor<BasicJob> => log

## Persistent Job Creator and Performer

All PersistentJobs have the following properties
* counter = incremented value. Depending on the counter value, the job gets different properties
    - counter is odd => message is persistent and gets retried
    - counter is event => message is transient and will get dropped without processing if expiresAt > now
    - counter last digit is 1 => message processing will fail and get retried
* persistent = must be performed or can be dropped if it sits too long in the queue
* expiresAt = timestamp after which transient (non persistent) messages are dropped without processing, to catch up with backlog

### Retry strategy (see PersistentJobProcessor):

* If !persistent and now > expiresAt, drop the expired message
* Retry processing 3 times, with exponential backoff between tries from 2 to 10 seconds
    * max-attempts: 3
    * backOffInitialInterval: 2000
    * backOffMaxInterval: 10000
* After 3 tries, put the message on the DLQ
    * autobindDlq: true 
* After 5 seconds, bring the message back from DLQ to queue for processing
    * dlqTtl: 3000 
    * dlqDeadLetterExchange:
* After 3 "resurrections" from DLQ to queue and error is logged and the message is dropped
    * See handling of "x-death" header  

# Queue cleanup

The queues and DLQ are removed automatically after 5 minutes idle
* expires: 300000
* dlqExpires: 300000