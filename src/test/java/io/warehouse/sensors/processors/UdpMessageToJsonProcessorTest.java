package io.warehouse.sensors.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.warehouse.sensors.exception.SensorMessageValidationException;
import io.warehouse.sensors.model.SensorMessage;
import io.warehouse.sensors.validator.SensorMessageValidator;


class UdpMessageToJsonProcessorTest {

    private SensorMessageValidator validator;
    private UdpMessageToJsonProcessor processor;

    @BeforeEach
    void setUp() {
        validator = mock(SensorMessageValidator.class);
        processor = new UdpMessageToJsonProcessor(validator);
    }

    @Test
    void testProcessValidMessage() throws Exception {
        String validMessage = "sensor_id=A1; value=42";
        SensorMessage expectedSensorMessage = new SensorMessage("A1", 42);
        Exchange exchange = mock(Exchange.class);
        Message message = mock(Message.class);
    
        when(exchange.getIn()).thenReturn(message);
        when(message.getBody(String.class)).thenReturn(validMessage);

        processor.process(exchange);

        verify(message).setBody(expectedSensorMessage);
        verify(validator).validate(expectedSensorMessage);
    }

    @Test
    void testProcessInvalidMessage() {
        String invalidMessage = "invalid_message";
        Exchange exchange = mock(Exchange.class);
        Message message = mock(Message.class);

        when(exchange.getIn()).thenReturn(message);
        when(message.getBody(String.class)).thenReturn(invalidMessage);

        assertThrows(SensorMessageValidationException.class, () -> processor.process(exchange));
    }

    @Test
    void testValidateAndConvertValidMessage() {
        String validMessage = "sensor_id=B2; value=15";
        SensorMessage expectedSensorMessage = new SensorMessage("B2", 15);

        SensorMessage result = processor.validateAndConvert(validMessage);

        assertEquals(expectedSensorMessage.id(), result.id());
        assertEquals(expectedSensorMessage.value(), result.value());
        verify(validator).validate(result);
    }

    @Test
    void testValidateAndConvertInvalidFormat() {
        String invalidMessage = "sensor=C3;value=20";

        assertThrows(SensorMessageValidationException.class, () -> processor.validateAndConvert(invalidMessage));
    }

    @Test
    void testValidateAndConvertWithWhitespace() {
        String messageWithWhitespace = "sensor_id=D4;\n value=30\r";
        SensorMessage expectedSensorMessage = new SensorMessage("D4", 30);

        SensorMessage result = processor.validateAndConvert(messageWithWhitespace);

        assertEquals(expectedSensorMessage.id(), result.id());
        assertEquals(expectedSensorMessage.value(), result.value());
        verify(validator).validate(result);
    }

    @Test
    void testValidateAndConvertWithLeadingTrailingSpaces() {
        String messageWithSpaces = "  sensor_id=E5; value=25  ";
        SensorMessage expectedSensorMessage = new SensorMessage("E5", 25);

        SensorMessage result = processor.validateAndConvert(messageWithSpaces);

        assertEquals(expectedSensorMessage.id(), result.id());
        assertEquals(expectedSensorMessage.value(), result.value());
        verify(validator).validate(result);
    }
}
