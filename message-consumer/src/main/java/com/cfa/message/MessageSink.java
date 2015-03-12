package com.cfa.message;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

/**
 * <br/><br/>Created by victor on 3/11/15.
 */
public class MessageSink {

    private final KafkaProducer<String, String> producer;
    //TODO: make this configurable
    private final String topic = "messages";

    public MessageSink(String brokerList) {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", brokerList);
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("acks", "1");

        producer = new KafkaProducer<>(properties);
    }

    public void send(String json) {
        producer.send(new ProducerRecord<>(topic, json));
    }
}
