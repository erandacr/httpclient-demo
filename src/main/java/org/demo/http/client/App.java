package org.demo.http.client;

import java.net.URISyntaxException;
import org.demo.http.client.http.SampleHttpGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class App implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    @Autowired
    private SampleHttpGateway sampleHttpGateway;

    public static void main(String[] args) {
        logger.info("App started.....");

        SpringApplication.run(App.class, args);
    }

    @Override
    public void run(String... args) {
        try {
            logger.info(sampleHttpGateway.getContent());
        } catch (URISyntaxException e) {
            logger.error("error while building the uri", e);
        }
    }
}

