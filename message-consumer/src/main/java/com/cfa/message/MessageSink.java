package com.cfa.message;

import com.cfa.commons.Consts;
import com.google.common.base.Throwables;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * <br/><br/>Created by victor on 3/11/15.
 */
public class MessageSink {
    private final String topic = Consts.KAFKA_TOPIC;
    private final KafkaProducer<String, String> producer;

    public MessageSink(String brokerList) {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", brokerList);
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("acks", "1");

        producer = new KafkaProducer<>(properties);
    }

    public void send(String json) {
        Future<RecordMetadata> future = producer.send(new ProducerRecord<String, String>(topic, json));
        try {
            future.get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            //TODO: handle exceptions some way
            throw Throwables.propagate(e);
        }
    }
}
