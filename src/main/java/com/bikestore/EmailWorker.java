package com.bikestore;

import com.rabbitmq.client.*;
import java.util.Map;

public class EmailWorker {
    public static void main(String[] args) throws Exception {
        Connection conn = Common.connect();
        Channel ch = conn.createChannel();
        Common.setup(ch);
        ch.basicQos(1);
        Common.log("SYSTEM", "EmailWorker esperando pagos aprobados...");
        ch.basicConsume(Common.PAID, false, (tag, delivery) -> {
            long deliveryTag = delivery.getEnvelope().getDeliveryTag();
            try {
                Map<String,Object> order = Common.decode(delivery.getBody());
                String id = order.get("pedidoId").toString();
                if ("PAID".equals(order.get("paymentStatus"))) {
                    Common.log(id, "Email de confirmacion SIMULADO para " + order.get("email"));
                } else {
                    Common.log(id, "Email omitido: pago no aprobado");
                }
                ch.basicAck(deliveryTag, false);
            } catch (Exception e) {
                Common.log("UNKNOWN", "Error de email: " + e.getMessage());
                ch.basicNack(deliveryTag, false, true);
            }
        }, tag -> {});
    }
}
