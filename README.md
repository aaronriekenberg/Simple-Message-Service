Simple Message Service
======================

Simple Message Service (SMS) is a simple, high performance message service loosely based on a few ideas from JMS.

* SMS supports topics only (no queues).  SMS topics have semantics similar to JMS.
* A broker accepts TCP connections from clients and routes messages between them.
* A high-level Java API is provided (SMSConnection) to connect to the broker, subscribe to topics, and send messages to topics.  The API is able to automatically reconnect to the broker if the TCP connection is lost.
* No attempts are made at reliability beyond what is provided by the TCP connections between the broker and clients.  For many problems this is sufficient.
* No form of persistence to disk or durability is provided.
* Netty is used to manage TCP connections in the client API (SMSConnection) and in the broker.  This allows the broker code to be tiny: Netty's ChannelGroup provides the basic function of a topic out of the box.
* Google Protocol Buffers are used to define the message format between the client api and the broker.  This means messages have very little overhead above their payload.  It also means writing a non-Java client api should be easy.
* SMS is fast due to the above decisions.  A Core i5 2500K can easily handle a broker, 30 writers each writing 5KB messages to different topics at 1000 messages/second, and 30 readers each subscribing to a different topic.
