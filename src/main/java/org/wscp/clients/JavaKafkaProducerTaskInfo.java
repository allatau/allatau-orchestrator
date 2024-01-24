package org.wscp.clients;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class JavaKafkaProducerTaskInfo {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        String server = "localhost:9092";
        String topicName = "ow.tasks.out";

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

        HashMap<String, String> taskInfoMap = new HashMap<String, String>();
        taskInfoMap.put("id", "a81dddd8-4dc9-11ed-bdc3-0242ac120002");
        taskInfoMap.put("status", "running");

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String taskInfo = gson.toJson(taskInfoMap);


        RecordMetadata recordMetadata = (RecordMetadata) producer.send(new ProducerRecord(topicName, taskInfo)).get();
        if (recordMetadata.hasOffset())
            System.out.println("recordMetadata hash: "+ recordMetadata.hashCode());
            System.out.println("Message sent successfully");

        producer.close();
    }

}