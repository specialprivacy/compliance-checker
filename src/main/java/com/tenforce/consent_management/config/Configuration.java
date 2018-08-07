package com.tenforce.consent_management.config;

import ch.qos.logback.classic.Level;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Created by langens-jonathan on 4/10/18. This file is only here to facilitate
 * getting configuration settings. It lazy loads settings and loads them only once.
 * Wouter: it should probably eagerly load configuration. Turning this into a singleton class
 * which loads config at system startup and then passes the config into the relevant components is
 * a pattern I prefer more.
 */
public class Configuration {
    private static Logger log = LoggerFactory.getLogger(Configuration.class);

    // the directory where the rule files are to be stored
    private String rulesDirectory = "/rules";

    // everything kafka
    private String kafkaURLList = "localhost:9092";
    private String kafkaClientID = UUID.randomUUID().toString();
    private String kafkaTopicPolicy = "full-policies";
    private String kafkaTopicAccess = "accesses";
    private String kafkaTopicConsent = "consents";
    private Level loggingLevel = Level.INFO;

    // the names of the environment variables
    private static final String RULES_DIRECTORY = "RULES_DIRECTORY";
    private static final String KAFKA_URL_LIST = "KAFKA_URL_LIST";
    private static final String KAFKA_CLIENT_ID = "KAFKA_CLIENT_ID";
    private static final String KAFKA_TOPIC_POLICY = "KAFKA_TOPIC_POLICY";
    private static final String KAFKA_TOPIC_ACCESS = "KAFKA_TOPIC_ACCESS";
    private static final String KAFKA_TOPIC_CONSENT = "KAFKA_TOPIC_CONSENT";
    private static final String LOGGING_LEVEL = "LOGGING_LEVEL";

    private static final List<String> LOGGING_LEVELS = Arrays.asList("trace", "debug", "info", "warn", "error");

    public static Configuration loadFromEnvironment() {
        Configuration config = new Configuration();

        if (System.getenv().containsKey(Configuration.RULES_DIRECTORY)) {
            config.setRulesDirectory(System.getenv(Configuration.RULES_DIRECTORY));
        }

        if (System.getenv().containsKey(Configuration.KAFKA_URL_LIST)) {
            config.setKafkaURLList(System.getenv(Configuration.KAFKA_URL_LIST));
        }

        if (System.getenv().containsKey(Configuration.KAFKA_CLIENT_ID)) {
            config.setKafkaClientID(System.getenv(Configuration.KAFKA_CLIENT_ID));
        }

        if (System.getenv().containsKey(Configuration.KAFKA_TOPIC_POLICY)) {
            config.setKafkaTopicPolicy(System.getenv(Configuration.KAFKA_TOPIC_POLICY));
        }

        if (System.getenv().containsKey(Configuration.KAFKA_TOPIC_ACCESS)) {
            config.setKafkaTopicAccess(System.getenv(Configuration.KAFKA_TOPIC_ACCESS));
        }

        if (System.getenv().containsKey(Configuration.KAFKA_TOPIC_CONSENT)) {
            config.setKafkaTopicConsent(System.getenv(Configuration.KAFKA_TOPIC_CONSENT));
        }

        if (System.getenv().containsKey(Configuration.LOGGING_LEVEL)) {
            config.setLoggingLevel(System.getenv(Configuration.LOGGING_LEVEL));
        }

        return config;
    }

    @Override
    public String toString() {
        return "{\"rulesDirectory\": \"" + rulesDirectory + "\", " +
                "\"kafkaURLList\": \"" + kafkaURLList + "\", " +
                "\"kafkaClientID\": \"" + kafkaClientID + "\", " +
                "\"kafkaTopicPolicy\": \"" + kafkaTopicPolicy + "\", " +
                "\"kafkaTopicAccess\": \"" + kafkaTopicAccess + "\", " +
                "\" kafkaTopicConsent\": \"" + kafkaTopicConsent + "\"}";
    }

    // setters: this where the input validation should happen so we can ensure only correct values are written
    public void setRulesDirectory(@NotNull String rulesDirectory) {
        if ("".equals(rulesDirectory)) {
            throw new IllegalArgumentException("rulesDirectory cannot be an empty string");
        }
        this.rulesDirectory = rulesDirectory;
    }

    public void setKafkaURLList(@NotNull String kafkaURLList) {
        if ("".equals(kafkaURLList)) {
            throw new IllegalArgumentException("kafkaURLList cannot be an empty string");
        }
        this.kafkaURLList = kafkaURLList;
    }

    public void setKafkaClientID(@NotNull String kafkaClientID) {
        if ("".equals(kafkaClientID)) {
            throw new IllegalArgumentException("kafkaClientID cannot be an empty string");
        }
        this.kafkaClientID = kafkaClientID;
    }

    public void setKafkaTopicPolicy(@NotNull String kafkaTopicPolicy) {
        if ("".equals(kafkaTopicPolicy)) {
            throw new IllegalArgumentException("kafkaTopicPolicy cannot be an empty string");
        }
        this.kafkaTopicPolicy = kafkaTopicPolicy;
    }

    public void setKafkaTopicAccess(@NotNull String kafkaTopicAccess) {
        if ("".equals(kafkaTopicAccess)) {
            throw new IllegalArgumentException("kafkaTopicAccess cannot be an empty string");
        }
        this.kafkaTopicAccess = kafkaTopicAccess;
    }

    public void setKafkaTopicConsent(@NotNull String kafkaTopicConsent) {
        if ("".equals(kafkaTopicConsent)) {
            throw new IllegalArgumentException("kafkaTopicConsent cannot be an empty string");
        }
        this.kafkaTopicConsent = kafkaTopicConsent;
    }

    public void setLoggingLevel(@NotNull String level) {
        if (!LOGGING_LEVELS.contains(level.toLowerCase())) {
            throw new IllegalArgumentException("loggingLevel must be one of: " + LOGGING_LEVELS);
        }
        this.loggingLevel = Level.toLevel(level, Level.INFO);
    }

    // getters
    @NotNull
    public String getRulesDirectory() {
        return rulesDirectory;
    }

    @NotNull
    public String getKafkaURLList() {
        return kafkaURLList;
    }

    @NotNull
    public String getKafkaClientID() {
        return kafkaClientID;
    }

    @NotNull
    public String getKafkaTopicPolicy() {
        return kafkaTopicPolicy;
    }

    @NotNull
    public String getKafkaTopicAccess() {
        return kafkaTopicAccess;
    }

    @NotNull
    public String getKafkaTopicConsent() {
        return kafkaTopicConsent;
    }

    @NotNull
    public Level getLoggingLevel() {
        return loggingLevel;
    }
}
