package com.tenforce.consent_management.kafka;

import com.tenforce.consent_management.config.Configuration;
import org.apache.kafka.clients.producer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Created by langens-jonathan on 4/25/18.
 *
 * Basic Kafka producer that allows to write an checked-application-log to
 * the corresponding kafka topic.
 */
public class CheckedComplianceLogProducer extends Thread {
    private final static Logger log = LoggerFactory.getLogger(CheckedComplianceLogProducer.class);
    private KafkaProducer<String, String> producer;
    private String topic;
    private Boolean isAsync;

    public CheckedComplianceLogProducer(String topic, Boolean isAsync) {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, Configuration.getKafkaURLList());
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, Configuration.getKafkaClientID());
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        producer = new KafkaProducer<String, String>(properties);
        this.topic = topic;
        this.isAsync = isAsync;
    }

    public void run() {
    }

    public void sendMessage(String key, String value) {
        if (isAsync) {
            producer.send(
                    new ProducerRecord<String, String>(topic, key, value),
                    new ProducerCallBack(key, value));
        } else {
            try {
                producer.send(new ProducerRecord<String, String>(topic, key, value));
                log.info("Sent message: (key: {}, value: {})", key, value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

class ProducerCallBack implements Callback {
    private static final Logger log = LoggerFactory.getLogger(ProducerCallBack.class);
    private String key;
    private String value;

    public ProducerCallBack(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public void onCompletion(RecordMetadata metadata, Exception exception) {
        if (metadata != null) {
            log.info("Sent message: (key: {}, value: {})", key, value);
        } else {
            exception.printStackTrace();
        }
    }

}
