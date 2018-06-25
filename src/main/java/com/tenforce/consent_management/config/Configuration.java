package com.tenforce.consent_management.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Created by langens-jonathan on 4/10/18. This file is only here to facilitate
 * getting configuration settings. It lazy loads settings and loads them only once.
 * Wouter: it should probably eagerly load configuration. Turning this into a singleton class
 * which loads config at system startup and then passes the config into the relevant components is
 * a pattern I prefer more.
 */
public class Configuration {
    private static boolean instantiated = false;
    private static Logger log = LoggerFactory.getLogger(Configuration.class);

    // the base of the policy class name
    private static String policyClassBase;

    // the directory where the rule files are to be stored
    private static String rulesDirectory;

    // everything kafka
    private static String kafkaURLList;
    private static String kafkaClientID;
    private static String kafkaTopicPolicy;
    private static String kafkaTopicAccess;
    private static String kafkaTopicConsent;

    // the names of the environment variables
    private static String POLICY_CLASS_BASE = "POLICY_CLASS_BASE";
    private static String RULES_DIRECTORY = "RULES_DIRECTORY";
    private static String KAFKA_URL_LIST = "KAFKA_URL_LIST";
    private static String KAFKA_CLIENT_ID = "KAFKA_CLIENT_ID";
    private static String KAFKA_TOPIC_POLICY = "KAFKA_TOPIC_POLICY";
    private static String KAFKA_TOPIC_ACCESS = "KAFKA_TOPIC_ACCESS";
    private static String KAFKA_TOPIC_CONSENT = "KAFKA_TOPIC_CONSENT";

    // instantiates the variables
    // should include validity checking
    private static void instantiate() {
        if(Configuration.instantiated) return;
        Configuration.instantiatePolicyClassBase();
        Configuration.instantiateRulesDirectory();
        Configuration.instantiateKafkaURLList();
        Configuration.instantiateKafkaClientID();
        Configuration.instantiateKafkaTopicAccess();
        Configuration.instantiateKafkaTopicConsent();
        Configuration.instantiateKafkaTopicPolicy();
        Configuration.instantiated = true;
        log.info("Configuration", "policyClassBase: " + policyClassBase);
        log.info("Configuration", "rulesDirectory: " + rulesDirectory);
        log.info("Configuration", "kafkaURLList: " + kafkaURLList);
        log.info("Configuration", "kafkaClientID: " + kafkaClientID);
        log.info("Configuration", "kafkaTopicPolicy: " + kafkaTopicPolicy);
        log.info("Configuration", "kafkaTopicAccess: " + kafkaTopicAccess);
        log.info("Configuration", "kafkaTopicConsent: " + kafkaTopicConsent);
    }

    private static void instantiatePolicyClassBase() {
        if(System.getenv().containsKey(Configuration.POLICY_CLASS_BASE)) {
            Configuration.policyClassBase = System.getenv(Configuration.POLICY_CLASS_BASE);
        } else {
            Configuration.policyClassBase = "http://www.semanticweb.org/langens-jonathan/ontologies/2018/2/untitled-ontology-16#";
        }
    }

    private static void instantiateRulesDirectory() {
        if(System.getenv().containsKey(Configuration.RULES_DIRECTORY)) {
            Configuration.rulesDirectory = System.getenv(Configuration.RULES_DIRECTORY);
        } else {
            Configuration.rulesDirectory = "/policies";
        }
    }

    private static void instantiateKafkaURLList() {
        if(System.getenv().containsKey(Configuration.KAFKA_URL_LIST)) {
            Configuration.kafkaURLList = System.getenv(Configuration.KAFKA_URL_LIST);
        } else {
            Configuration.kafkaURLList = "";
        }
    }

    private static void instantiateKafkaClientID() {
        if(System.getenv().containsKey(Configuration.KAFKA_CLIENT_ID)) {
            Configuration.kafkaClientID = System.getenv(Configuration.KAFKA_CLIENT_ID);
        } else {
            Configuration.kafkaClientID = "";
        }
    }

    private static void instantiateKafkaTopicPolicy() {
        if(System.getenv().containsKey(Configuration.KAFKA_TOPIC_POLICY)) {
            Configuration.kafkaTopicPolicy = System.getenv(Configuration.KAFKA_TOPIC_POLICY);
        } else {
            Configuration.kafkaTopicPolicy = "full-policies";
        }
    }

    private static void instantiateKafkaTopicAccess() {
        if(System.getenv().containsKey(Configuration.KAFKA_TOPIC_ACCESS)) {
            Configuration.kafkaTopicAccess = System.getenv(Configuration.KAFKA_TOPIC_ACCESS);
        } else {
            Configuration.kafkaTopicAccess = "accesses";
        }
    }

    private static void instantiateKafkaTopicConsent() {
        if(System.getenv().containsKey(Configuration.KAFKA_TOPIC_CONSENT)) {
            Configuration.kafkaTopicConsent = System.getenv(Configuration.KAFKA_TOPIC_CONSENT);
        } else {
            Configuration.kafkaTopicConsent = "consents";
        }
    }

    // getters, all lazy
    public static String getPolicyClassBase() {
        Configuration.instantiate();
        return Configuration.policyClassBase;
    }

    public static String getRulesDirectory() {
        Configuration.instantiate();
        return Configuration.rulesDirectory;
    }
    public static String getKafkaURLList() {
        Configuration.instantiate();
        return Configuration.kafkaURLList;
    }

    public static String getKafkaClientID() {
        Configuration.instantiate();
        return Configuration.kafkaClientID;
    }

    public static String getKafkaTopicPolicy() {
        Configuration.instantiate();
        return Configuration.kafkaTopicPolicy;
    }

    public static String getKafkaTopicAccess() {
        Configuration.instantiate();
        return Configuration.kafkaTopicAccess;
    }

    public static String getKafkaTopicConsent() {
        Configuration.instantiate();
        return Configuration.kafkaTopicConsent;
    }
}
