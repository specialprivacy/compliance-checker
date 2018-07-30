package com.tenforce.consent_management.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Properties;

/**
 * A simple class which handles common creation and tear down functions for both KafkaConsumers in this application.
 * This is by no means a generic abstraction and there is probably very little point putting it into its own library.
 * By making it more generic you will only end up reimplementing the actual KafkaConsumer class.
 * If at some point in the future the functionality of both Consumers diverges too much, this class should be removed.
 */
public abstract class BaseConsumer implements Runnable {
    private final KafkaConsumer<String, String> consumer;
    private final String topic;
    private static final Logger log = LoggerFactory.getLogger(BaseConsumer.class);

    BaseConsumer(String topic, Properties extraProps) {
        Properties props = new Properties();
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

        props.putAll(extraProps);

        consumer = new KafkaConsumer<>(props);
        this.topic = topic;
    }

    @Override
    public void run() {
       try {
           consumer.subscribe(Collections.singletonList(topic));

           //noinspection InfiniteLoopStatement
           while (true) {
               ConsumerRecords<String, String> records = consumer.poll(Long.MAX_VALUE);
               for (ConsumerRecord<String, String> record : records) {
                   log.info("Processing (topic: {}, partition: {}, offset: {})", this.topic, record.partition(), record.offset());
                   processRecord(record);
               }
           }
       } catch (WakeupException e) {
           // Ignore for shutdown
       } finally {
           log.info("Closing kafka consumer");
           consumer.close();
           onShutdown();
           log.info("Done closing kafka consumer");
       }
    }

    protected abstract void processRecord(ConsumerRecord<String, String> record);

    protected void onShutdown() {}

    /**
     * Call this to stop processing messages, cleanly shutdown the kafka consumer and stop the thread
     */
    public void shutdown() {
        log.info("Received request to stop");
        consumer.wakeup();
    }
}
