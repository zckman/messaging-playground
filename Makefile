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

.PHONY: rabbitmq
rabbitmq: rabbitmq-ready rabbitmq-plugins rabbitmq-create-exchanges	

.PHONY: rabbitmq-ready
rabbitmq-ready:
	@echo "*** Waiting for rabbitmq startup"
	@docker-compose exec rabbitmq sh -c 'rabbitmqctl wait /var/lib/rabbitmq/mnesia/rabbit@$${HOSTNAME}.pid'
	@docker-compose exec rabbitmq rabbitmqctl await_startup
	@docker-compose exec rabbitmq rabbitmq-diagnostics is_running

.PHONY: rabbitmq-plugins
rabbitmq-plugins: rabbitmq-ready
	@docker-compose cp ./rabbitmq/config.d/30-rabbitmq_web_mqtt.conf rabbitmq:/etc/rabbitmq/conf.d/
	@docker-compose exec rabbitmq \
	rabbitmq-plugins enable rabbitmq_web_mqtt

.PHONY: rabbitmq-create-exchanges
rabbitmq-create-exchanges: rabbitmq-create-device-exchange rabbitmq-create-sensors-exchange

.PHONY: rabbitmq-create-device-exchanges
rabbitmq-create-device-exchange:
	@echo "*** Create transient topic exchange \"${TOPIC_SMART_DEVICES}\""
	@docker-compose exec rabbitmq \
	rabbitmqadmin \
	--username=${RABBITMQ_DEFAULT_USER} \
	--password=${RABBITMQ_DEFAULT_PASS} \
	declare exchange name="${TOPIC_SMART_DEVICES}" type=topic durable=false

.PHONY: rabbitmq-create-sensors-exchange
rabbitmq-create-sensors-exchange:
	@echo "*** Create transient topic exchange \"${TOPIC_SENSORS}\""
	@docker-compose exec rabbitmq \
	rabbitmqadmin \
	--username=${RABBITMQ_DEFAULT_USER} \
	--password=${RABBITMQ_DEFAULT_PASS} \
	declare exchange name="${TOPIC_SENSORS}" type=topic durable=false