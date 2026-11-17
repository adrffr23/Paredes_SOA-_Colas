package com.bikestore;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

public class Common {
    static final ObjectMapper JSON = new ObjectMapper();
    static final String ORDERS = "orders.queue", PAID = "payments.paid.queue", DLQ = "orders.dlq";
    static Connection connect() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost"); factory.setUsername("guest"); factory.setPassword("guest");
        return factory.newConnection();
    }
    static void setup(Channel ch) throws Exception {
        ch.queueDeclare(ORDERS, true, false, false, null);
        ch.queueDeclare(PAID, true, false, false, null);
        ch.queueDeclare(DLQ, true, false, false, null);
    }
    static void publish(Channel ch, String queue, Map<String,Object> order) throws Exception {
        ch.basicPublish("", queue, MessageProperties.PERSISTENT_TEXT_PLAIN, JSON.writeValueAsBytes(order));
    }
    @SuppressWarnings("unchecked")
    static Map<String,Object> decode(byte[] bytes) throws Exception {
        return JSON.readValue(bytes, Map.class);
    }
    static void log(Object id, String message) {
        System.out.printf("[%s] [thread=%s] pedidoId=%s | %s%n", Instant.now(), Thread.currentThread().getName(), id, message);
    }
}
