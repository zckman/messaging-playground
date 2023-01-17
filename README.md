# Messaging Experimentation Playground

The goal of this project is to create a messaging experimentation playground for testing out different messaging protocols and technologies, such as [ReactiveX](https://reactivex.io/). The playground will consist of a message broker running in a [Docker](https://www.docker.com/) container, as well as simulated smart home devices that will produce messages. There will also be a consuming client to receive and process the messages.

## Building the Playground

To run the messaging playground, you will need to have [Docker](https://www.docker.com/) installed on your system. Once you have Docker set up, you can build the playground using the following command:

```
docker-compose build
```

or

```
make build
```

## Running the Playground

To run the messaging playground, you will need to have [Docker](https://www.docker.com/) installed on your system. Once you have Docker set up, you can start the playground using the following command:

```
docker-compose up
```

or

```
make up
```

This will start the [message broker](https://en.wikipedia.org/wiki/Message_broker), any necessary dependencies and the simulated smart home devices.

To stop you can use `docker-compose stop` or `make stop`

## Interacting with the Playground

You can interact with the messaging playground using any client that supports the desired messaging protocol. For example, you can use the [Kafka](https://kafka.apache.org/) command-line client to produce and consume messages from the message broker if it is running Apache Kafka.

You can use `make observe` to print the current Devices from the `SmartDevices` topic and start listening for sensor readings.

### Vue.js consumer (WIP)

https://localhost:20443
It does nothing at the moment

## Future Development

In the future, I plan to build a simple client that may interact with Kafkas REST Api.  


This project is purely for experimentation and learning purposes and is not intended for practical use.
