package com.loudsight.useful.service.aeron;

import io.aeron.Aeron;
import io.aeron.driver.MediaDriver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
public class AeronDispatcherTestTestConfig {

    @DependsOn("mediaDriver")
    @Bean
    Aeron aeron() {
        return Aeron.connect();
    }

    @Bean
    MediaDriver mediaDriver() {
        return MediaDriver.launch();
    }
}
