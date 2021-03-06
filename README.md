Simple Message Service
======================

Simple Message Service (SMS) is a simple, high performance message service loosely based on a few ideas from JMS.

* SMS supports topics only (no queues).  SMS topics have semantics similar to JMS.
* A broker accepts TCP or Unix Domain connections from clients and routes messages between them.
* A high-level Java API is provided to connect to the broker, subscribe to topics, and send messages to topics.  The API is able to automatically reconnect to the broker if the connection is lost.  SMSTCPConnection supports TCP connections, and SMSUnixConnection supports Unix Domain connections.
* No attempts are made at reliability beyond what is provided by the connections between the broker and clients.  If the connection is up, messages will be delivered in order without loss.  If a connection goes down, messages will get lost.  For many problems this is sufficient.
* No form of persistence to disk or durability is provided.
* Netty is used to manage connections in the client API and in the broker.  This allows the broker code to be tiny: Netty's ChannelGroup provides the basic function of a topic out of the box.  Netty gives the broker a fixed size thread pool that defaults to 2x the number of processors.
* Google Protocol Buffers are used to define the message format between the client API and the broker.  This means messages have very little overhead above their payload.  It also means writing a non-Java client or server is easy.
* Unix Domain socket connections have significantly less CPU overhead than TCP connections.  Unix Domain socket connects only work on Linux, and only support local connections between the API and Broker.  TCP connections work on all platforms and support remote network connections.
* SMS is fast due to the above decisions.  
    * A Core i5 2500K can handle a Java SMS broker, 50 TCP writers each writing 5KB messages to different topics at 1000 messages/second, and 50 TCP readers each subscribing to one of these topics.  During this test the Java SMS broker is using a pool of 8 threads and uses < 150 MB of heap space.  
