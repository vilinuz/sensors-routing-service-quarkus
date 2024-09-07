package io.warehouse.sensors.validator;

import io.warehouse.sensors.config.props.WarehousePropertiesConfig.SensorPropertiesConfig;
import io.warehouse.sensors.config.props.WarehousePropertiesConfig;
import io.warehouse.sensors.exception.SensorMessageValidationException;
import io.warehouse.sensors.model.SensorMessage;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SensorMessageValidator {
    private final WarehousePropertiesConfig config;

    public SensorMessageValidator(WarehousePropertiesConfig config) {
        this.config = config;
    }

    public String validate(final SensorMessage message) {
        return config.sensors().stream()
                .map(SensorPropertiesConfig::id)
                .filter(id -> id.equals(message.id()))
                .findAny()
                .orElseThrow(() -> new SensorMessageValidationException(
                    String.format("Message %s has invalid id '%s'", message, message.id())));
    }
}
