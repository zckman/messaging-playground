# Messaging Experimentation Playground

The goal of this project is to create a messaging experimentation playground for testing out different messaging protocols and technologies, such as [ReactiveX](https://reactivex.io/). The playground will consist of a message broker running in a [Docker](https://www.docker.com/) container, as well as simulated smart home devices that will produce messages. There will also be a consuming client to receive and process the messages.

## Running the Playground

To run the messaging playground, you will need to have [Docker](https://www.docker.com/) installed on your system. Once you have Docker set up, you can start the playground using the following command:

```
docker-compose up
```


This will start the [message broker](https://en.wikipedia.org/wiki/Message_broker) and any necessary dependencies. The simulated smart home devices and consuming client will need to be started separately.

## Interacting with the Playground

You can interact with the messaging playground using any client that supports the desired messaging protocol. For example, you can use the [Kafka](https://kafka.apache.org/) command-line client to produce and consume messages from the message broker if it is running Apache Kafka.

## Future Development

In the future, I plan to add support for additional messaging protocol.  
This project is purely for experimentation and learning purposes and is not intended for practical use.
