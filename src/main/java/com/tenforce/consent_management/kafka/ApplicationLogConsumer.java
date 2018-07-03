package com.tenforce.consent_management.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenforce.consent_management.compliance.ComplianceChecker;
import com.tenforce.consent_management.compliance.HermiTReasonerFactory;
import com.tenforce.consent_management.config.Configuration;
import com.tenforce.consent_management.consent.PolicyStore;
import com.tenforce.consent_management.log.ApplicationLog;
import kafka.utils.ShutdownableThread;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
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
 * application logs topic and takes them of one by one.
 *
 * After that it checks the consent for every access.
 */
public class ApplicationLogConsumer  extends ShutdownableThread {
    private final KafkaConsumer<String, String> consumer;
    private final String topic;
    private static final Logger log = LoggerFactory.getLogger(ApplicationLogConsumer.class);

    private CheckedComplianceLogProducer checkedComplianceLogProducer = null;
    private final ComplianceChecker complianceChecker = new ComplianceChecker(
            new HermiTReasonerFactory(),
            Configuration.getRulesDirectory()
    );
    private final PolicyStore policyStore = PolicyStore.getInstance();

    public ApplicationLogConsumer(String topic) throws RocksDBException, OWLOntologyCreationException {
        super("ApplicationLogConsumer", false);
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, Configuration.getKafkaURLList());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, Configuration.getKafkaClientID());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
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
            log.debug("Processing (topic: {}, partition: {}, offset: {})", this.topic, record.partition(), record.offset());
            if(this.checkedComplianceLogProducer != null) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    ApplicationLog alog= mapper.readValue(record.value(), ApplicationLog.class);
                    OWLClassExpression logClass = alog.toOWL();
                    OWLClassExpression policyClass = policyStore.getPolicy(alog.getUserID());
                    alog.setHasConsent(policyClass != null && complianceChecker.hasConsent(logClass, policyClass));
                    this.checkedComplianceLogProducer.sendMessage(alog.getEventID(), mapper.writeValueAsString(alog));
                } catch (IOException e) {
                    log.error("Failed to parse kafka message");
                    e.printStackTrace();
                } catch (RocksDBException e) {
                    log.error("Failed to read policy from rocksdb");
                    e.printStackTrace();
                }
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

    public void setCheckedComplianceLogProducer(CheckedComplianceLogProducer checkedComplianceLogProducer) {
        this.checkedComplianceLogProducer = checkedComplianceLogProducer;
    }
}