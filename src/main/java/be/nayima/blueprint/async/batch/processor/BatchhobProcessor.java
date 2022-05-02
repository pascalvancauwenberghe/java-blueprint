package be.nayima.blueprint.async.batch.processor;

import be.nayima.blueprint.async.batch.message.BatchJob;
import be.nayima.blueprint.async.batch.message.BatchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BatchhobProcessor {

    public Message<BatchResult> process(List<BatchJob> messages) {
        log.info("Received batch of {} messages ", messages.size());
        for (var message : messages) {
            log.info("Msg #{} created at {}", message.getCounter(), message.getCreatedOn());
        }
        var result = new BatchResult(messages.size());
        return MessageBuilder.withPayload(result).build();
    }

}


