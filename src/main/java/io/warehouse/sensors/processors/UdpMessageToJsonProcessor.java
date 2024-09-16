package io.warehouse.sensors.processors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import io.warehouse.sensors.exception.SensorMessageValidationException;
import io.warehouse.sensors.model.SensorMessage;
import io.warehouse.sensors.validator.SensorMessageValidator;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UdpMessageToJsonProcessor implements Processor {
    private static final String REGEX_PATTERN = "^sensor_id=([a-zA-Z0-9]{2});\\s*value=(\\d{2})$";
    private static final Pattern MESSAGE_PATTERN = Pattern.compile(REGEX_PATTERN);

    private final SensorMessageValidator validator;


    public UdpMessageToJsonProcessor(SensorMessageValidator validator) {
        this.validator = validator;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        String body = exchange.getIn().getBody(String.class);
        exchange.getIn().setBody(validateAndConvert(body));
    }

    public SensorMessage validateAndConvert(String message) {
        String normalizedMessage = normalizeMessage(message);
        Matcher matcher = MESSAGE_PATTERN.matcher(normalizedMessage);

        if (matcher.matches()) {
            SensorMessage sensorMessage = new SensorMessage(matcher.group(1), Integer.parseInt(matcher.group(2)));
            validator.validate(sensorMessage);
            return sensorMessage;
        }

        throw new SensorMessageValidationException(String.format("Message %s has invalid format", message));
    }

    private String normalizeMessage(String message) {
        return message
                .replaceAll("[\n\r]", "")
                .replace(" ", "");
    }
}