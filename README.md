# java-blueprint
Blueprint to demonstrate and experiment with async design in java

##Prerequisites

RabbitMQ running with its standard ports exposed
Use the docker container with management console (rabbitmq:3-management) to see management console on http://127.0.0.1:15672/
See https://www.rabbitmq.com/management.html#permissions to create a management user and giving them access to the console

#Flows
#Basic Job Creator and Performer

scheduler.BasicJobCron calls processor.BasicJobSupplier to put a BasicJob (created by usecase.CreateBasicJob) in queue Blueprint.Scheduled.BasicJobs every second with a TTL of 2 seconds

DroppableJobExecutor<BasicJob> reads from queue Blueprint.Scheduled.BasicJobs and calls usecase.PerformBasicJob which takes 4 seconds to process one BasicJob.

This would normally result in queue Blueprint.Scheduled.BasicJobs filling up with unprocessed BasicJobs. But BasicJobs whose TTL has expired are not processed, they are dropped. An IPerformDroppableWork has the opportunity to perform some (very fast) action when a job is to be dropped. 

CRON every 1s => BasicJobSupplier -> Blueprint.Scheduled.BasicJobs -> DroppableExecutor<BasicJob> => log
