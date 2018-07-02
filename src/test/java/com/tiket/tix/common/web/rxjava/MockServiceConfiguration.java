package com.tiket.tix.common.web.rxjava;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.mockito.Mockito.mock;

/**
 * Additional {@link Configuration} for configuring mock service objects, for testing purpose.
 *
 * @author zakyalvan
 */
@Configuration
public class MockServiceConfiguration {
    @Bean
    SampleApplication.RegistrationService registrationService() {
        return mock(SampleApplication.RegistrationService.class);
    }
}
