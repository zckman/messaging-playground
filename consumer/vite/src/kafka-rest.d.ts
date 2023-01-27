declare module 'kafka-rest' {
    export function client(config: ClientConfig)
    export interface Message {
        key: string | null;
        value: any;
        partition: number | null;
    }
    export interface ClientConfig {
        url?: string;
        version?: number;
        headers?: any;
        timeout?: number;
    }
    export class Client {
        constructor(config: ClientConfig);
        config: ClientConfig;
        brokers: Brokers;
        topics: Topics;
        consumers: Consumers;
        broker(id: number): Broker;
        topic(name: string): Topic;
        topicPartition(name: string, id: number): TopicPartition;
        readonly SUPPORTED_VERSIONS: number[];
        readonly DEFAULTS: {
            url: string;
            version: number;
            timeout: number;
        };
        readonly Schema: typeof Schema;
        readonly BinarySchema: typeof BinarySchema;
        readonly AvroSchema: typeof AvroSchema;
        /**
        * Shorthand for Client.brokers().broker(id). Note that this does not request metadata so it can be chained to get
        * to nested resources.
        * @param id broker ID
        /
        broker(id: number): Broker;
        /*
        * Shorthand for Client.topics.topic(name). Note that this does not request metadata so it can be chained to get to
        * nested resources, e.g. Client.topic(name).partition(id).
        * @param name topic name
        /
        topic(name: string): Topic;
        /*
        * Shorthand for Client.topics.topic(name).partitions.partition(id). Note that this does not request metadata so it
        * can be used to get to nested resources and for producing messages.
        * @param name topic name
        * @param id partition ID
        /
        topicPartition(name: string, id: number): TopicPartition;
        /*
        * Shorthand for Client.consumers.group(groupName). Note that this does not start a new instance in the group
        * but rather returns the existing one, if any.
        * @param groupName consumer group name
        */
        consumer(groupName: string): Consumer;
    }

    export class Brokers {
        constructor(client: Client);
        broker(id: number): Broker;
    }

    export class Broker {
        constructor(client: Client, id: number);
    }

    export class Topics {
        constructor(client: Client);
        topic(name: string): Topic;
    }

    export class Topic {
        constructor(client: Client, name: string);
        partitions: TopicPartitions;
    }

    export class TopicPartitions {
        constructor(client: Client, topicName: string);
        partition(id: number): TopicPartition;
    }

    export class TopicPartition {
        constructor(client: Client, topicName: string, id: number);
    }

    export class Consumers {
        constructor(client: Client);
        group(groupName: string): Consumer;
    }

    export class Consumer {
        constructor(client: Client, groupName: string);
        join(opts: any, res: (err: any, res: any) => void): void;
        getPath(): string;
        toString(): string;
    }

    export class ConsumerInstance extends EventEmitter {
        client: Client;
        consumer: Consumer;
        raw: any;
        active: boolean;
        streams: ConsumerStream[];
        id: string;
        uri: string;
        subscribe(topic: string, options: any): ConsumerStream;
        shutdown(res: any): void;
    }

    export class ConsumerStream extends Readable {
        constructor(consumerInstance: ConsumerInstance, topic: string, options: any);
        _read(): void;
        pause(): this;
        resume(): this;
        setEncoding(encoding: string): this;
        read(size?: number): any;
        on(event: string | symbol, listener: (...args: any[]) => void): ConsumerStream;
        pipe<T extends Writable>(destination: T, options?: { end?: boolean; }): T;
        unpipe<T extends Writable>(destination?: T): this;
        unshift(chunk: any): void;
        wrap(oldStream: Readable): this;
        [Symbol.asyncIterator](): AsyncIterableIterator<any>;
    }


    export class Schema {
        constructor(schemaData: any);
    }

    export class BinarySchema extends Schema { }

    export class AvroSchema extends Schema { }
}
