# BikeStore Async v1.0

Demo Java 17 + Maven + RabbitMQ. `OrderProducer` publica un pedido JSON; `PaymentWorker` simula un cobro con 50% de fallos y hasta 3 reintentos; `EmailWorker` simula el correo solo para PAID; los fallos definitivos llegan a `orders.dlq`.

## Requisitos
Java JDK 17+, Maven 3.9+, Docker Desktop con Compose.

## Ejecutar

```bash
docker compose up -d
mvn compile
```

En tres terminales independientes, desde esta carpeta:

```bash
mvn exec:java -Dexec.mainClass=com.bikestore.PaymentWorker
mvn exec:java -Dexec.mainClass=com.bikestore.EmailWorker
mvn exec:java -Dexec.mainClass=com.bikestore.OrderProducer
```

Repetir el comando OrderProducer para generar más pedidos. RabbitMQ: http://localhost:15672 (guest/guest), pestaña Queues and Streams.

## Notas
- Esta demo simula una API de pedidos mediante un ejecutable Java; **no expone un endpoint HTTP**.
- El email se simula en consola; no se envía correo real.
- Los reintentos son inmediatos, sin retraso; en producción usar TTL/backoff y DLX.
- La publicación y el ack no son atómicos. Para producción se requieren publisher confirms, idempotencia y manejo de duplicados.
- La DLQ se implementa con publicación explícita a `orders.dlq`, no con un dead-letter exchange configurado en el bróker.
