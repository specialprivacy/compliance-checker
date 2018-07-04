package com.tenforce.consent_management;

import com.tenforce.consent_management.config.Configuration;
import com.tenforce.consent_management.kafka.ApplicationLogConsumer;
import com.tenforce.consent_management.kafka.CheckedComplianceLogProducer;
import com.tenforce.consent_management.kafka.PolicyConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    // default logger
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    private static PolicyConsumer policyConsumer = null;
    private static ApplicationLogConsumer applicationLogConsumer = null;
    private static CheckedComplianceLogProducer checkedComplianceLogProducer = null;

    public static void main(String[] args) {
        log.info("Compliance checker starting up");
        try {
            policyConsumer = new PolicyConsumer(Configuration.getKafkaTopicPolicy());
            checkedComplianceLogProducer = new CheckedComplianceLogProducer(Configuration.getKafkaTopicConsent(), false);
            applicationLogConsumer = new ApplicationLogConsumer(Configuration.getKafkaTopicAccess());
            applicationLogConsumer.setCheckedComplianceLogProducer(checkedComplianceLogProducer);

            ExecutorService executor = Executors.newFixedThreadPool(2);
            executor.submit(policyConsumer);
            executor.submit(applicationLogConsumer);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("Received request to stop. Gracefully terminating all kafka clients");
                applicationLogConsumer.shutdown();
                checkedComplianceLogProducer.interrupt();
                policyConsumer.shutdown();
                executor.shutdown();
                try {
                    if (executor.awaitTermination(5000, TimeUnit.MILLISECONDS)) {
                        log.info("Done stopping kafka clients.");
                    } else {
                        log.info("Done stopping kafka clients. Some did not shut down gracefully");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }));
        } catch (Exception e) {
            log.error("Failed to initialize services");
            e.printStackTrace();
            Runtime.getRuntime().exit(1);
        }
    }
}
