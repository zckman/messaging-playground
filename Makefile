include .env

.PHONY: up stop down build
up: docker-compose-up create-topics
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

.PHONY: broker
broker:
	# Downloading a https://github.com/confluentinc/confluent-docker-utils/raw/master/confluent/docker_utils/cub.py to wait for the broker
	@docker-compose exec kafka sh -c \
	"(wget -q https://github.com/confluentinc/confluent-docker-utils/raw/master/confluent/docker_utils/cub.py -O cub.py \
	&& echo 'Waiting for broker' \
	&& python cub.py help)"

.PHONY: broker-ready
broker-ready:
	sleep 10s

.PHONY: create-topics
create-topics: broker-ready create-device-topic create-sensors-topic

.PHONY: create-device-topics
create-device-topic:
	@echo "*** Ensuring topic \"${TOPIC_SMART_DEVICES}\" exists"
	@docker-compose exec kafka \
	kafka-topics --create --topic ${TOPIC_SMART_DEVICES} \
	--partitions 1 \
	--replication-factor 1 \
	--if-not-exists \
	--bootstrap-server ${KAFKA_BOOTSTRAP_SERVERS} \
	--config cleanup.policy=compact

.PHONY: create-sensors-topic
create-sensors-topic:
	@echo "*** Ensuring topic \"${TOPIC_SENSORS}\" exists"
	@docker-compose exec kafka \
	kafka-topics --create --topic ${TOPIC_SENSORS} \
	--partitions 1 \
	--replication-factor 1 \
	--if-not-exists \
	--bootstrap-server ${KAFKA_BOOTSTRAP_SERVERS} \
	--config retention.ms=86400000

.PHONY: listen-topic
listen-topic:
	@echo "*** Listening to topic \"${TOPIC}\""
	@docker-compose exec kafka \
	kafka-console-consumer --bootstrap-server ${KAFKA_BOOTSTRAP_SERVERS} --topic ${TOPIC}

.PHONY: show-topic
show-topic:
	@echo "*** Listing topic \"${TOPIC}\""
	@echo "    Note: There will be a timeout exception. This is normal."
	@docker-compose exec kafka \
	kafka-console-consumer --bootstrap-server ${KAFKA_BOOTSTRAP_SERVERS} --topic ${TOPIC} --from-beginning --timeout-ms 5000

:PHONY: observe
observe:
	@make show-topic TOPIC=${TOPIC_SMART_DEVICES}
	@make listen-topic TOPIC=${TOPIC_SENSORS}