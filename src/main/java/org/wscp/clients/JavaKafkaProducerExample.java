package org.wscp.clients;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class JavaKafkaProducerExample {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        String server = "localhost:9092";
        String topicName = "ow.tasks.in";

        final Properties props = new Properties();

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                server);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                LongSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class.getName());

        props.put("acks", "all"); // Необходимый параметр, чтобы даже при неработающих consumer'ах получить сообщение

        final Producer<Long, String> producer =
                new KafkaProducer<>(props);

        RecordMetadata recordMetadata = (RecordMetadata) producer.send(new ProducerRecord(topicName, "example message NEW 4")).get();
        if (recordMetadata.hasOffset())
            System.out.println("recordMetadata hash: "+ recordMetadata.hashCode());
            System.out.println("Message sent successfully");

        producer.close();
    }

}