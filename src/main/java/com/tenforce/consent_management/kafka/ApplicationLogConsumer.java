package com.tenforce.consent_management.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenforce.consent_management.compliance.ComplianceChecker;
import com.tenforce.consent_management.compliance.HermiTReasonerFactory;
import com.tenforce.consent_management.config.Configuration;
import com.tenforce.consent_management.consent.PolicyStore;
import com.tenforce.consent_management.log.ApplicationLog;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.errors.WakeupException;
import org.rocksdb.RocksDBException;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Properties;

/**
 * Created by langens-jonathan on 4/25/18. This is basic Kafka consumer that listens to the
 * application logs consumerTopic and takes them of one by one.
 *
 * After that it checks the consent for every access.
 */
public class ApplicationLogConsumer  implements Runnable {
    private final KafkaConsumer<String, String> consumer;
    private final KafkaProducer<String, String> producer;
    private final String consumerTopic;
    private final String producerTopic;
    private static final Logger log = LoggerFactory.getLogger(ApplicationLogConsumer.class);

    private final ComplianceChecker complianceChecker = new ComplianceChecker(
            new HermiTReasonerFactory(),
            Configuration.getRulesDirectory()
    );
    private final PolicyStore policyStore = PolicyStore.getInstance();

    public ApplicationLogConsumer(String consumerTopic, String producerTopic) throws RocksDBException, OWLOntologyCreationException {
        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, Configuration.getKafkaURLList());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, Configuration.getKafkaClientID());
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        consumerProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, Configuration.getKafkaURLList());
        producerProps.put(ProducerConfig.CLIENT_ID_CONFIG, Configuration.getKafkaClientID());
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

        consumer = new KafkaConsumer<>(consumerProps);
        producer = new KafkaProducer<>(producerProps);
        this.consumerTopic = consumerTopic;
        this.producerTopic = producerTopic;
    }

    @Override
    public void run() {
        try {
            consumer.subscribe(Collections.singletonList(this.consumerTopic));

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(1000);
                for (ConsumerRecord<String, String> record : records) {
                    log.debug("Processing (topic: {}, partition: {}, offset: {})", this.consumerTopic, record.partition(), record.offset());
                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        ApplicationLog alog= mapper.readValue(record.value(), ApplicationLog.class);
                        OWLClassExpression logClass = alog.toOWL();
                        OWLClassExpression policyClass = policyStore.getPolicy(alog.getUserID());
                        alog.setHasConsent(policyClass != null && complianceChecker.hasConsent(logClass, policyClass));
                        producer.send(
                                new ProducerRecord<>(producerTopic, alog.getEventID(), mapper.writeValueAsString(alog)),
                                (recordMetadata, e) -> {
                                    if (null != e) {
                                        // TODO: we should die in this case especially once we ask kafka to retry
                                        log.error("Failed to write record {}", e);
                                        return;
                                    }
                                    // TODO: we should manually commit our offset here
                                    log.debug("Sucessfully written record (topic: {}, partition: {}, offset: {})", recordMetadata.topic(), recordMetadata.partition(), recordMetadata.offset());
                                }
                        );
                    } catch (IOException e) {
                        log.error("Failed to parse kafka message");
                        e.printStackTrace();
                    } catch (RocksDBException e) {
                        // TODO: we should probably die here, because without rocksdb we cannot sucessfully process the message
                        log.error("Failed to read policy from rocksdb");
                        e.printStackTrace();
                    }
                }
            }
        } catch (WakeupException e) {
            // Ignore for shutdown
        } finally {
            log.info("Closing kafka clients");
            consumer.close();
            producer.close();
            log.info("Done closing kafka clients");
        }
    }

    /**
     * Call this to stop processing messages, cleanly shutdown the kafka consumer and stop the thread
     */
    public void shutdown() {
        log.info("Received request to stop");
        consumer.wakeup();
    }
}