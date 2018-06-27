package com.tenforce.consent_management.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenforce.consent_management.compliance.ComplianceChecker;
import com.tenforce.consent_management.compliance.ComplianceService;
import com.tenforce.consent_management.config.Configuration;
import com.tenforce.consent_management.log.ApplicationLog;
import kafka.utils.ShutdownableThread;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
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
    private ComplianceService complianceService = null;

    public ApplicationLogConsumer(String topic) {
        super("ApplicationLogConsumer", false);
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, Configuration.getKafkaURLList());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, Configuration.getKafkaClientID());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

        consumer = new KafkaConsumer<String, String>(props);
        this.topic = topic;
        consumer.subscribe(Collections.singletonList(this.topic));
    }

    @Override
    public void doWork() {
        ConsumerRecords<String, String> records = consumer.poll(1000);
        for (ConsumerRecord<String, String> record : records) {
            log.info("Processing (topic: {}, partition: {}, offset: {})", this.topic, record.partition(), record.offset());
            if(this.checkedComplianceLogProducer != null) {
                ObjectMapper mapper = new ObjectMapper();
                try {

                    ApplicationLog alog= mapper.readValue(record.value(), ApplicationLog.class);
                    String inMemoryConsentClass = alog.generateOWLConsentClass();
                    boolean hasConsent = false;
                    if(this.complianceService != null) {
                        try {
                            this.complianceService.instantiateComplianceChecker();
                            hasConsent = this.complianceService.hasConsent(alog.getUserID(),
                                    ComplianceChecker.getDataControllerPolicyClassName("InMemory"),
                                    inMemoryConsentClass);
                        } catch (OWLOntologyCreationException e) {
                            e.printStackTrace();
                        }
                    }
                    alog.setHasConsent(hasConsent);
                    this.checkedComplianceLogProducer.sendMessage(null, mapper.writeValueAsString(alog));
                } catch (IOException e) {
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

    public void setComplianceService(ComplianceService complianceService) {
        this.complianceService = complianceService;
    }
}