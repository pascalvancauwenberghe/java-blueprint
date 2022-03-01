package be.nayima.blueprint.async.persistent.usecase;

import be.nayima.blueprint.async.persistent.connector.CallFailedException;
import be.nayima.blueprint.async.persistent.connector.FailingParty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class FailingPartyCallerImpl implements FailingPartyCaller {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.from(ZoneOffset.UTC));
    private final FailingParty party;

    @Override
    public void call(String request, int counter) throws CallFailedException {
        party.call(request,counter);
    }
}
