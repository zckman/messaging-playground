include .env

.PHONY: up stop down build
up: docker-compose-up
stop: docker-compose-stop
down: docker-compose-down
build: docker-compose-build

.PHONY: docker-compose-up docker-compose-down docker-compose-down docker-compose-build
docker-compose-up:
	docker-compose up -d
docker-compose-stop:
	docker-compose stop
docker-compose-down:
	docker-compose down
docker-compose-build:
	docker-compose build

.PHONY: logs
logs:
	docker-compose logs

.PHONY: kafka
kafka: kafka-ready kafka-create-topics	

.PHONY: kafka-ready
kafka-ready:
	sleep 10s

.PHONY: kafka-create-topics
kafka-create-topics: kafka-create-device-topic kafka-create-sensors-topic

.PHONY: kafka-create-device-topics
kafka-create-device-topic:
	@echo "*** Ensuring topic \"${TOPIC_SMART_DEVICES}\" exists"
	@docker-compose exec kafka \
	kafka-topics --create --topic ${TOPIC_SMART_DEVICES} \
	--partitions 1 \
	--replication-factor 1 \
	--if-not-exists \
	--bootstrap-server ${KAFKA_BOOTSTRAP_SERVERS} \
	--config cleanup.policy=compact

.PHONY: kafka-create-sensors-topic
kafka-create-sensors-topic:
	@echo "*** Ensuring topic \"${TOPIC_SENSORS}\" exists"
	@docker-compose exec kafka \
	kafka-topics --create --topic ${TOPIC_SENSORS} \
	--partitions 1 \
	--replication-factor 1 \
	--if-not-exists \
	--bootstrap-server ${KAFKA_BOOTSTRAP_SERVERS} \
	--config retention.ms=86400000

.PHONY: kafka-listen-topic
kafka-listen-topic:
	@echo "*** Listening to topic \"${TOPIC}\""
	@docker-compose exec kafka \
	kafka-console-consumer --bootstrap-server ${KAFKA_BOOTSTRAP_SERVERS} --topic ${TOPIC}

.PHONY: kafka-show-topic
kafka-show-topic:
	@echo "*** Listing topic \"${TOPIC}\""
	@echo "    Note: There will be a timeout exception. This is normal."
	@docker-compose exec kafka \
	kafka-console-consumer --bootstrap-server ${KAFKA_BOOTSTRAP_SERVERS} --topic ${TOPIC} --from-beginning --timeout-ms 5000

:PHONY: observe
kafka-observe:
	@make kafka-show-topic TOPIC=${TOPIC_SMART_DEVICES}
	@make kafka-listen-topic TOPIC=${TOPIC_SENSORS}