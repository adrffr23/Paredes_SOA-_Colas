package com.bikestore;

import com.rabbitmq.client.*;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class PaymentWorker {
    public static void main(String[] args) throws Exception {
        Connection conn = Common.connect();
        Channel ch = conn.createChannel();
        Common.setup(ch);
        ch.basicQos(1);
        Common.log("SYSTEM", "PaymentWorker esperando pedidos...");
        ch.basicConsume(Common.ORDERS, false, (tag, delivery) -> {
            long deliveryTag = delivery.getEnvelope().getDeliveryTag();
            try {
                Map<String,Object> order = Common.decode(delivery.getBody());
                String id = order.get("pedidoId").toString();
                int attempt = ((Number) order.get("attempt")).intValue();
                Common.log(id, "Intento de cobro " + (attempt + 1) + "/4");
                boolean success = ThreadLocalRandom.current().nextBoolean();
                if (success) {
                    order.put("paymentStatus", "PAID");
                    Common.publish(ch, Common.PAID, order);
                    Common.log(id, "paymentStatus=PAID; evento enviado a EmailWorker");
                } else if (attempt < 3) {
                    order.put("attempt", attempt + 1);
                    Common.publish(ch, Common.ORDERS, order);
                    Common.log(id, "Pago fallido; reintento " + (attempt + 1) + "/3 en cola");
                } else {
                    order.put("paymentStatus", "FAILED");
                    Common.publish(ch, Common.DLQ, order);
                    Common.log(id, "Pago fallido tras 3 reintentos; enviado a DLQ");
                }
                ch.basicAck(deliveryTag, false);
            } catch (Exception e) {
                Common.log("UNKNOWN", "Error procesando: " + e.getMessage());
                ch.basicNack(deliveryTag, false, true);
            }
        }, tag -> {});
    }
}
