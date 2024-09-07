package io.warehouse.sensors;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

import io.warehouse.sensors.config.props.WarehousePropertiesConfig;
import io.warehouse.sensors.processors.UdpMessageToJsponProcessor;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SensorsManagementRouteConfig extends RouteBuilder {

    private static final String UDP_MESSAGE_CHANNEL = "direct:udpMessageChannel";
    private static final String TEMPERATURE_NATS_SUBJECT_CHANNEL = "nats:temperature";
    private static final String HUMIDITY_NATS_SUBJECT_CHANNEL = "nats:humidity";
    private static final String ERROR_NATS_SUBJECT_CHANNEL = "nats:error";

    private final WarehousePropertiesConfig config;
    private final UdpMessageToJsponProcessor processor;

    public SensorsManagementRouteConfig(WarehousePropertiesConfig config, UdpMessageToJsponProcessor processor) {
        this.config = config;
        this.processor = processor;
    }
    
    @Override
    public void configure() throws Exception {
     
        config.sensors().forEach(sensor -> 
            from("netty:udp://0.0.0.0:${sensor.port}?keepAlive=true")
                .setHeader("sensor_id", constant(sensor.id()))
                .to(UDP_MESSAGE_CHANNEL)
        );

        from(UDP_MESSAGE_CHANNEL)
            .log("Received message: ${body}")
            .process(processor::process)
            .marshal().json(JsonLibrary.Jackson)
            .choice()
                .when(simple("${headers.sensor_id} == 't1'"))
                    .to(TEMPERATURE_NATS_SUBJECT_CHANNEL)
                .when(simple("${headers.sensor_id} == 'h1'"))
                    .to(HUMIDITY_NATS_SUBJECT_CHANNEL)
                .otherwise()
                    .to(ERROR_NATS_SUBJECT_CHANNEL);
        }
}
