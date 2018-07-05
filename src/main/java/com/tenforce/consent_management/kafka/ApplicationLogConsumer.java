package com.tenforce.consent_management.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenforce.consent_management.compliance.ComplianceChecker;
import com.tenforce.consent_management.compliance.HermiTReasonerFactory;
import com.tenforce.consent_management.config.Configuration;
import com.tenforce.consent_management.consent.PolicyStore;
import com.tenforce.consent_management.log.ApplicationLog;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.*;
import org.jetbrains.annotations.NotNull;
import org.rocksdb.RocksDBException;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by langens-jonathan on 4/25/18. This is basic Kafka consumer that listens to the
 * application logs consumerTopic and takes them of one by one.
 *
 * After that it checks the consent for every access.
 */
public class ApplicationLogConsumer  extends BaseConsumer {
    private final KafkaProducer<String, String> producer;
    private final String producerTopic;
    private static final Logger log = LoggerFactory.getLogger(ApplicationLogConsumer.class);

    private final ComplianceChecker complianceChecker;
    private final PolicyStore policyStore = PolicyStore.getInstance();

    public ApplicationLogConsumer(@NotNull Configuration config) throws RocksDBException, OWLOntologyCreationException {
        super(config.getKafkaTopicAccess(), getConsumerProperties(config));

        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getKafkaURLList());
        producerProps.put(ProducerConfig.CLIENT_ID_CONFIG, config.getKafkaClientID());
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

        producer = new KafkaProducer<>(producerProps);
        this.producerTopic = config.getKafkaTopicConsent();
        complianceChecker = new ComplianceChecker(new HermiTReasonerFactory(), config.getRulesDirectory());
    }

    private static Properties getConsumerProperties(@NotNull Configuration config) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getKafkaURLList());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, config.getKafkaClientID());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        return props;
    }

    @Override
    protected void processRecord(ConsumerRecord<String, String> record) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            ApplicationLog alog= mapper.readValue(record.value(), ApplicationLog.class);
            OWLClassExpression logClass = alog.toOWL();
            OWLClassExpression policyClass = policyStore.getPolicy(alog.getUserID());
            alog.setHasConsent(null != policyClass && complianceChecker.hasConsent(logClass, policyClass));
            producer.send(
                    new ProducerRecord<>(producerTopic, alog.getEventID(), mapper.writeValueAsString(alog)),
                    (recordMetadata, e) -> {
                        if (null != e) {
                            // TODO: we should die in this case especially once we ask kafka to retry
                            log.error("Failed to write record {}", e);
                            return;
                        }
                        // TODO: we should manually commit our offset here
                        log.debug("Successfully written record (topic: {}, partition: {}, offset: {})", recordMetadata.topic(), recordMetadata.partition(), recordMetadata.offset());
                    }
            );
        } catch (IOException e) {
            log.error("Failed to parse kafka message");
            e.printStackTrace();
        } catch (RocksDBException e) {
            // TODO: we should probably die here, because without rocksdb we cannot successfully process the message
            log.error("Failed to read policy from rocksdb");
            e.printStackTrace();
        }
    }

    @Override
    protected void onShutdown() {
        log.info("Closing kafka producer");
        producer.close();
        log.info("Done closing kafka producer");
    }
}