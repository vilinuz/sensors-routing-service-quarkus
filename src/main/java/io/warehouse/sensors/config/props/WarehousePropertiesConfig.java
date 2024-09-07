package io.warehouse.sensors.config.props;

import java.util.List;

import io.smallrye.config.ConfigMapping;


@ConfigMapping(prefix = "warehouse")
public interface WarehousePropertiesConfig {
    List<SensorPropertiesConfig> sensors();

    public interface SensorPropertiesConfig {
        String id();
        String subject();
        int port();
        int threshold();
    }
    
}
