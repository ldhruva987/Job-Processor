# Job-Processor
Distributed Task Queue &amp; Job Processor 

Design Trade-off: "I chose Database-as-a-Queue using SKIP LOCKED for simplicity and strong consistency in a 10-hour prototype, rather than a separate message broker like RabbitMQ".
Idempotency: "Implemented via a unique constraint on the idempotency_key to prevent duplicate processing at the database level".
Auto-scaling (Stretch): "To auto-scale, we could monitor the number of PENDING jobs. If the count exceeds a threshold, we could trigger the deployment of additional JobWorker instances in Kubernetes".


# How to start:

Run `docker ps` and `mvn spring-boot:run`

open link in any browser http://localhost:8081/index.html