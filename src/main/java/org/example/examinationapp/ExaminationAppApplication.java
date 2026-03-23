package org.example.examinationapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class ExaminationAppApplication {

    private static final Logger log = LoggerFactory.getLogger(ExaminationAppApplication.class);

    @Value("${server.port:8080}")
    private String port;

    public static void main(String[] args) {
        SpringApplication.run(ExaminationAppApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        log.info("Swagger UI: http://localhost:{}/swagger-ui/index.html", port);
    }
}