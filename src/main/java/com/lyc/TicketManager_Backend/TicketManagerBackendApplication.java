package com.lyc.TicketManager_Backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class TicketManagerBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketManagerBackendApplication.class, args);
    }

}
