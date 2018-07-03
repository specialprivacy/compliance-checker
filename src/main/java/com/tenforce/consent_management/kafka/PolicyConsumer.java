package com.tenforce.consent_management.kafka;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenforce.consent_management.config.Configuration;
import com.tenforce.consent_management.consent.Policy;
import com.tenforce.consent_management.consent.PolicyStore;
import kafka.utils.ShutdownableThread;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Properties;
import java.util.UUID;

/**
 * Created by langens-jonathan on 3/28/18.
 *
 * Basic consumer, reads the data subject policies kafka topic and writes each of them
 * to the correct OWL file.
 */
public class PolicyConsumer extends ShutdownableThread {
    private final KafkaConsumer<String, String> consumer;
    private final String topic;
    private final PolicyStore policyStore = PolicyStore.getInstance();
    private static final Logger log = LoggerFactory.getLogger(PolicyConsumer.class);

    public PolicyConsumer(String topic) throws RocksDBException {
        super("KafkaPolicyConsumer", false);
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, Configuration.getKafkaURLList());

        // Every instance of the compliance checker should consume all of the user policies in the current setup
        props.put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

        consumer = new KafkaConsumer<>(props);
        this.topic = topic;
        consumer.subscribe(Collections.singletonList(this.topic));
    }

    @Override
    public void doWork() {
        ConsumerRecords<String, String> records = consumer.poll(1000);
        for (ConsumerRecord<String, String> record : records) {
            log.info("Processing (topic: {}, partition: {}, offset: {})", this.topic, record.partition(), record.offset());
            ObjectMapper mapper = new ObjectMapper();
            try {
                Policy postedPolicy = mapper.readValue(record.value(), Policy.class);
                policyStore.updatePolicy(record.key(), postedPolicy.toOWL());
            } catch (IOException e) {
                log.error("Failed to parse kafka message");
                e.printStackTrace();
            } catch (RocksDBException e) {
                log.error("Failed to write policy to rocksdb");
                e.printStackTrace();
            }
        }
    }

    @Override
    public String name() {
        return null;
    }

    @Override
    public boolean isInterruptible() {
        return false;
    }
}