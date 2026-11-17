package com.bikestore;

import com.rabbitmq.client.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class OrderProducer {
    public static void main(String[] args) throws Exception {
        try (Connection conn = Common.connect(); Channel ch = conn.createChannel()) {
            Common.setup(ch);
            String id = "PED-" + UUID.randomUUID().toString().substring(0,8);
            Map<String,Object> order = new LinkedHashMap<>();
            order.put("pedidoId", id); order.put("cliente", "Cliente Demo");
            order.put("email", "cliente@ejemplo.com"); order.put("bicicleta", "Mountain Bike");
            order.put("total", 450.00); order.put("paymentStatus", "PENDING"); order.put("attempt", 0);
            Common.publish(ch, Common.ORDERS, order);
            Common.log(id, "Pedido JSON publicado en orders.queue");
        }
    }
}
