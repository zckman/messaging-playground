include .env

.PHONY: up stop down
up: docker-compose-up create-topics
stop: docker-compose-stop
down: docker-compose-down

.PHONY: docker-compose-up docker-compose-down docker-compose-down
docker-compose-up:
	docker-compose up -d
docker-compose-stop:
	docker-compose stop
docker-compose-down:
	docker-compose down

.PHONY: logs
logs: docker-compose logs

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
	docker-compose exec kafka \
	kafka-topics --create --topic SmartDevices \
	--partitions 1 \
	--replication-factor 1 \
	--if-not-exists \
	--bootstrap-server ${KAFKA_BOOTSTRAP_SERVERS} \
	--config cleanup.policy=compact

create-sensors-topic:
	docker-compose exec kafka \
	kafka-topics --create --topic Sensors \
	--partitions 1 \
	--replication-factor 1 \
	--if-not-exists \
	--bootstrap-server ${KAFKA_BOOTSTRAP_SERVERS} \
	--config retention.ms=86400000