package com.tenforce.consent_management.kafka;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenforce.consent_management.config.Configuration;
import com.tenforce.consent_management.consent.Policy;
import com.tenforce.consent_management.consent.PolicyStore;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jetbrains.annotations.NotNull;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

/**
 * Created by langens-jonathan on 3/28/18.
 *
 * Basic consumer, reads the data subject policies kafka topic, transforms them into OWLAPI structures and saves those
 * in an embedded rocksdb instance
 */
public class PolicyConsumer extends BaseConsumer {
    private final PolicyStore policyStore = PolicyStore.getInstance();
    private static final Logger log = LoggerFactory.getLogger(PolicyConsumer.class);

    public PolicyConsumer(@NotNull Configuration config) throws RocksDBException {
        super(config.getKafkaTopicPolicy(), getConsumerProperties(config));
    }

    private static Properties getConsumerProperties(@NotNull Configuration config) {
        Properties props = new Properties();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getKafkaURLList());
        // Every instance of the compliance checker should consume all of the user policies in the current setup
        props.put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        return props;
    }

    @Override
    protected void processRecord(ConsumerRecord<String, String> record) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            Policy postedPolicy = mapper.readValue(record.value(), Policy.class);
            policyStore.updatePolicy(record.key(), postedPolicy.toOWL());
        } catch (IOException e) {
            log.error("Failed to parse kafka message");
            e.printStackTrace();
        } catch (RocksDBException e) {
            // TODO: we should probably die here, without rocksdb messages we cannot do correct compliance checks
            log.error("Failed to write policy to rocksdb");
            e.printStackTrace();
        }
    }
}