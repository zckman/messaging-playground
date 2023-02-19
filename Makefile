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

