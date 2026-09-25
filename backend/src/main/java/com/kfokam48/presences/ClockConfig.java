package com.kfokam48.presences;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ClockConfig {

    /** Clock injectable : permet de tester l'expiration du code (RG1) sans attendre. */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
