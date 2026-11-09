package com.disasterrelief;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * MainApp is the primary entry point for the Disaster Relief Supply & Emergency Medical Logistics
 * Dispatch System (DREMLDS).
 * Fulfills the exact academic assignment naming specification.
 */
@SpringBootApplication
public class MainApp {

    private static final Logger logger = LoggerFactory.getLogger(MainApp.class);

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(MainApp.class);
        Environment env = app.run(args).getEnvironment();
        logApplicationStartup(env);
    }

    private static void logApplicationStartup(Environment env) {
        String protocol = "http";
        if (env.getProperty("server.ssl.key-store") != null) {
            protocol = "https";
        }
        String serverPort = env.getProperty("server.port", "8080");
        String contextPath = env.getProperty("server.servlet.context-path", "/");
        String hostAddress = "localhost";
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            logger.warn("The host name could not be determined, using `localhost` as fallback");
        }

        logger.info("""
                
                ----------------------------------------------------------------------------------------
                \tDISASTER RELIEF SUPPLY & EMERGENCY MEDICAL LOGISTICS DISPATCH SYSTEM (DREMLDS)
                \tApplication version: 1.0.0
                \tActive Profile(s):   {}
                \tLocal Command Center:{}://localhost:{}{}
                \tExternal Network:    {}://{}:{}{}
                \tAPI Documentation:   {}://localhost:{}{}/swagger-ui.html
                \tDatabase Host:       {} (Port: {})
                \tDDUS Weights:        Urgency={}, Pop={}, Terrain={}
                ----------------------------------------------------------------------------------------""",
                env.getActiveProfiles().length == 0 ? "default (local)" : String.join(", ", env.getActiveProfiles()),
                protocol, serverPort, contextPath,
                protocol, hostAddress, serverPort, contextPath,
                protocol, serverPort, contextPath,
                env.getProperty("DB_HOST", "localhost"),
                env.getProperty("DB_PORT", "3306"),
                env.getProperty("app.ddus.weight-urgency", "35.0"),
                env.getProperty("app.ddus.weight-population", "35.0"),
                env.getProperty("app.ddus.weight-terrain", "30.0")
        );
    }
}

