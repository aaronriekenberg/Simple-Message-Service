Simple Message Service
======================

Simple Message Service (SMS) is a simple, high performance message service loosely based on a few ideas from JMS.

* SMS supports topics only (no queues).  SMS topics have semantics similar to JMS.
* A broker accepts TCP connections from clients and routes messages between them.
* A high-level Java API is provided (SMSConnection) to connect to the broker, subscribe to topics, and send messages to topics.  The API is able to automatically reconnect to the broker if the TCP connection is lost.
* No attempts are made at reliability beyond what is provided by the TCP connections between the broker and clients.  If the connection is up, messages will be delivered in order without loss.  If a connection goes down, messages will get lost.  For many problems this is sufficient.
* No form of persistence to disk or durability is provided.
* Netty is used to manage TCP connections in the client API (SMSConnection) and in the Java broker (sms-broker).  This allows the broker code to be tiny: Netty's ChannelGroup provides the basic function of a topic out of the box.  Netty gives the broker a fixed size thread pool that defaults to 2x the number of processors.
* Google Protocol Buffers are used to define the message format between the client api and the broker.  This means messages have very little overhead above their payload.  It also means writing a non-Java client or server is easy.
* SMS is fast due to the above decisions.  
    * A Core i5 2500K can handle a Java broker, 50 writers each writing 5KB messages to different topics at 1000 messages/second, and 50 readers each subscribing to one of these topics.  During this test the SMS broker is using a pool of 8 threads and uses < 150 MB of heap space.  
    * A test of the same size with ActiveMQ creates ~175 broker threads and the broker uses > 500 MB of heap space (using the NIO transport and non-persistent producers).
* A C++11 version of the broker is included in the sms-broker-cpp directory.  The C++ broker is implemented using Boost ASIO and a fixed-size thread pool that defaults to 2x the number of processors.  In general the C++ broker has significantly better performance than the Java broker.
    * The same Core i5 2500k can handle a C++ broker, 50 Java writers each writing 5KB messages to different topics at 1000 messages/second, and 3 Java readers subscribing to each topic (150 total readers).
