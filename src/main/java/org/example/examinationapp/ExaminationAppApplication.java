package org.example.examinationapp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.awt.*;
import java.net.URI;

@SpringBootApplication
public class ExaminationAppApplication {

    // This automatically reads the port from your application.yml
    // If no port is found, it defaults to 8080
    @Value("${server.port:8080}")
    private String port;

    public static void main(String[] args) {
        // Tells Java we are NOT in a server-only (headless) mode
        // This is required to allow the app to talk to your Desktop/Monitor
        System.setProperty("java.awt.headless", "false");
        SpringApplication.run(ExaminationAppApplication.class, args);
    }

    /**
     * This method runs automatically once the Spring context is fully loaded
     * and the server (Tomcat/Hikari) is actually running.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void openSwaggerAfterStartup() {
        String url = "http://localhost:" + port + "/swagger-ui/index.html";
        String os = System.getProperty("os.name").toLowerCase();

        try {
            System.out.println("Attempting to open Swagger UI at: " + url);

            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                // Best way for Windows and macOS
                Desktop.getDesktop().browse(new URI(url));
            } else {
                // Best fallback way for Ubuntu/Linux
                Runtime runtime = Runtime.getRuntime();
                if (os.contains("nix") || os.contains("nux")) {
                    runtime.exec("xdg-open " + url);
                } else if (os.contains("win")) {
                    runtime.exec("rundll32 url.dll,FileProtocolHandler " + url);
                }
            }
        } catch (Exception e) {
            // We use System.err so the error stands out in the console in red
            System.err.println("Note: Could not open browser automatically. You can access Swagger at: " + url);
        }
    }
}