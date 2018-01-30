Intent of this project is to implement highly concurrent application for bank account transfer API. Java supports multithreading and synchronization from its first release. Java 5 has introduced concurrent package which has different utility classes for different concurrent problem then more utility classes introduced in java7 and java 8 and existing utility classes improved in java 8. Based on this changes as of java9 we can have following solution for concurrent update (bank account transfer API )we have following options:-
1.	Synchronization
2.	Synchronized Volatile
3.	ReentrantReadWriteLock
4.	StampedLock
5.	Atomic
6.	STM.
7.	Actor based locking.
8.	ReentrantLock

Synchronized is very primitive in its capabilities and has quite a few limitations..Unfortunately, synchronized makes it possible to indefinitely block to acquire a lock. This is because there’s no easy way to tell synchronized to wait for a finite time to acquire a lock. Synchronized leads to exclusive locks, and no other thread can gain access to the monitors held. This does not favor situations with multiple readers and infrequent writers. Even though the readers could run concurrently (ReentrantReadWriteLock), they’re serialized, and this results in poor concurrent performance.

In synchronized with volatile only one thread at a time can update the balance, but another thread could read the balance in the middle of an update.

The Lock framework is a compatible replacement for synchronization, which offers many features not provided by synchronized(try lock, time wait), as well as implementations offering better performance under high contention.

A ReentrantReadWriteLock maintains a pair of associated locks, one for read-only operations and one for writing. The read lock may be held simultaneously by multiple reader threads, so long as there are no writers. The write lock is exclusive, should be used when we have less frequent writes. so not ideal for this problem.

StampedLock is an alternative to using a ReentrantReadWriteLock ,it allow optimistic locking for read operations, in case of contention where we have more readers than writers, using a StampedLock can significantly improve performance , so not ideal for this solution.

Atomic variables offer a means of reducing the cost of updating "hot fields" such as statistics counters, Sequence generators, or the reference to the first node in a linked data structure. Atomic classes are designed primarily as building blocks for implementing non-blocking data structures like like ConcurrentLinkedQueue use atomic variables to directly implement wait-free algorithms, and classes like ConcurrentHashMap use ReentrantLock for locking where needed. ReentrantLock, in turn, uses atomic variables to maintain the queue of threads waiting for the lock.The compareAndSet method is not a general replacement for locking as with low to moderate contention, atomics offer better scalability with high contention, locks offer better contention avoidance because most CAS-based algorithms reacts to contention by trying again immediately, which is usually the right approach but in a high-contention environment just creates more contention. AtomicAdder and AtomicAccumlator is usually preferable to AtomicLong when multiple threads update a common sum that is used for purposes such as collecting statistics, not for fine-grained synchronization control , so not ideal for this solution.

Software Transactional Memory (Clojure, Akka ,Scala etc) works based on separation of identity from state. STM provides an explicit lock-free programming model. It allows transactions to run concurrently, and they all complete without a glitch where there are no conflicts between them. When transactions collide on write access to the same object or data, one of them is allowed to complete, and the others are automatically retried. The retries delay the execution of the colliding writers but provide for greater speed for readers and the winning writer. If we have a high rate of write collision to the same data, in the best case our writes are slow. In the worst case, our writes may fail because of too many retries. As we are expecting a lot of write contention so it is not ideal for this solution.

In the actor-based programming model, we allow only one actor to manipulate the state of an object. While the application is multithreaded, the actors themselves are single-threaded, and so there are no visibility and race condition concerns. Actors communicate with each other through messages. The unexpected failure of actors may result in starvation—one or more actors may be waiting for messages that would never arrive because of the failure. Actors do not prevent deadlocks; Actors can handle only one message request at a time. So, both action messages and messages requesting a response or state run sequentially. This can result in lower concurrency for the tasks that are interested only in reading the value. Also, we can reduce waits by designing with one-way “fire and forget” messages instead of two-way messages. Actors serve well when we can divide the problem into parts that can run quite independently and need to communicate only sporadically. If frequent interaction is required or the parts or tasks need to coordinate to form a quorum, the actor-based model is not suitable.

ReentrantLocks performance is better than synchronized in high contention and has advance feature like timed waits, tryLock, etc. transferAmount method avoided deadlock by ordering the accounts and avoided indefinite wait (starvation) by limiting the time it waits to acquire the locks.Since the monitors used are reentrant, subsequent calls to lock() within the depositAmountToAcoountWithGivenTimeOut and withdrawAmountFromAcoountWithGivenTimeout methods caused no harm and can be used independ operation of debit and credit.

AcocuntsService transferAmount is accepting the timeout as parameter which can be put in external configuration file and adjust according to expected no of concurrent threads.

AccountServiceParallelConcurrentTest.java tests the concurrent implementation by spawning configurable no of threads using concurrency utility. It has covers all the negative and positive scenario.

AccountsServiceConcurrentTestWithCompletableFutureTest test concurrent implementation using CompletableFuture and infinite stream.

AccountsServiceTest has basic happy scenario for transferAmount method.

AccountsControllerTest has the positive and negative scenario except concurrent test.

NotificationService method notifyAboutTransfer is called asynchronously to shorter execution time thread executing the transfer method and have better concurrency.

Async logging (see logback.xml configuration) is used to avoid io time for thread executing the transfer method and shorter execution time hence have better concurrency.

Swagger is used for document and testing rest API following would be the Swagger URLs :-

http://localhost:18080/swagger-resources

http://localhost:18080/v2/api-docs

http://localhost:18080/swagger-ui.html

For observable application, Actuator is used. End points provided by actuator can be used for runtime inspection of configuration details(Beans, env, auto-conf, config props) ,Monitor application status and behavior(Health checks ,metrics), simple instrumentation(Graceful shutdown),several Endpoint types(HTTP,JMX, Remote Shell) , security aware can enable or disable security for each endpoint(endpoints.shutdown.enabled=true). Following would be the URL ,in case it asks for password use admin/admin as username and password.

http://localhost:18080/health

http://localhost:18080/info

http://localhost:18080/metrics

http://localhost:18080/trace

Further Enhancements:-

1. Field validation message and exception messages are hard coded that can be moved to external config file.

2. Configure spring security with OAuth2 to prohibit unauthorized access and indentify the client device.

3. Spring data rest can be used to provide a solid foundation on which to expose CRUD operation to our Account repository managed entities using plain HTTP Rest Semantics. HATEOAS provides info to navigate the REST interface dynamically by including hypermedia links with response.

4. For the moment Notification Service is implemented as Async execution which can be expose as microservice and Messaging can be use for communication.

5. Implement exception handlers in REST controller to expose custom business and validation exceptions.

6. Basic Swagger document is used that can be customize.

7. Actuator enhancement :-
    1.	Spring actuator custom HealthIndicator can be implemented to get detail custom info.
    2.	CounterService , GuageService or Dropwizard can be used to count no of transactions done and time taken by each transaction.
    3.	Metrics can be exported to external db like Redis,Open TSDB, Statsd, JMX ,Dropwizard.
    4.	Other tools can be used like graphite to store and render time-series data.
    
8. For config spring provides several approaches to set config including externalizing vial command line arguments ,env variables etc but these has gaps :-
    •	Changes to config requires restart
    •	No audit trail
    •	Config is de-centralized
    •	No Support for sensitive information(no encryption capabilities)
  
    PCF provides cf set-env command and using manifest with env : sections which works well with less demanding configuration needs. But more demanding use cases like changing logging levels of running app to debug prod issue, change number of thread receives message from message broker, report all config changes to a prod system to support regulatory audits , toggle feature on/off in running application, protect secrets(password) embedded in configuration would required a externalized ,versioned distribution config server.PCF config server can be used to support versioning, auditability, encryption and refresh without restart(need actuator)
  
    When running many apps refreshing each one can be cumbersome, Spring cloud bus pub/sub notification with RabbitMQ can be used to send post request to refresh endpoints to fetch updated config values. it would need cloud bus AMQP dependency on classpath.
  
9. Based on concurrency and resource utilization, our transaction app should be able to scale out by adding more instances and once load reduces should be shut down the additional instance. To implement the same hard coding of IP isn't going to work. We will need a discovery mechanism that services can use to find each other. This means having a source of truth for what services are available. 

    We can use different implementation Consul, Apache Zookeeper and Eureka to register and discover the service.

    A service can register itself during startup and send updates as it goes through different lifecycle phases (initializing, accepting requests, shutting down, etc). It will also need to send regular heartbeats to the registry to let it know that it's still available. The registry can then automatically mark the service as down if it doesn't get a heartbeat in configurable timeout.

    Eureka does not have backend data store all data kept in memory, clients also have in-memory cache of eureka registrations (so they don’t have to go to registry for every single request to a service.).When a client registers with Eureka, it provides meta-data about itself such as host, port, health indicator URL, home page etc.
  
10. We can use Edge service :API gateways(circuit breakers, client-side load balancing) for various reasons like to have logical place to insert any client-specific requirements (security, API translation, protocol translation) and keep the mid-tier services free of this burdensome logic (as well as free from associated redeploys!). 

    Proxy requests from an edge-service to mid-tier services with a microproxy. For some classes of clients, a microproxy and security (HTTPS, authentication) might be enough.

    API gateways are used whenever a client - like a mobile phone or HTML5 client - requires API translation. Perhaps the client requires coarser grained payloads, or transformed views on the data.

    We can use Netflix  zuul and Netflix ribbon , zuul Gateway application into a reverse proxy that forwards relevant calls to other services based on configure routes.

    Zuul uses Ribbon to perform client-side IPC(Inter process communication) load balancing, its primary usage model involves rest call with various serialization schema support, and by default, Ribbon would use Netflix Eureka for service discovery.

    Ribbon has following features:-
      1.	Multiple and pluggable load balancing rules and algo(Round Robin, Weighted Response Time , Zone Aware Round Robi Load Balancer ,Random LB)and support for custom rules
      2.	Integration with service registry , can determine the server’s availability through the constant pinging of servers at regular intervals and has a capability of skipping the servers which are not live.
      3.	Built in failure resistance
      4.	Cloud enabled
      5.	Clients integrated with load balancers.
    
11. We can replace RestTemplate with Feign Client, it is a declarative web service client. It makes writing web service clients easier. It has pluggable annotation support including Feign annotations and JAX-RS annotations. Feign also supports pluggable encoders and decoders. Spring Cloud adds support for Spring MVC annotations and for using the same HttpMessageConverters used by default in Spring Web. Spring Cloud integrates Ribbon and Eureka to provide a load balanced http client when using Feign.

12. After enhancing our transfer API with config, registry ,discovery and API gateway our API will work fine in cloud but it should always be and responding so we need a bit more defensive mechanism to give something useful when It can’t connect to stop cascading failures. We can use Hystrix as circuit-breaker to provide fallback when service is down.

    Hystrix is fail fast, can rapidly recover ,fallback and gracefully degrade when possible.It Enables near real time monitoring ,alerting and operational control. We can see following metrics for each @HystrixCommnad:
      1.	Information and status(isCircuitOpen)
      2.	Cumulative and rolling event counts(countExceptionThrown & rollingCountExceptionsThrown)
      3.	Latency Percentiles(latencyExecute_percentile_995
      4.	Latency Percentiles :End to End execution(latency Total_percentile_5
      5.	Property Values(propertyValue_circuitBreakerRequestVolumeThreshold)
    
13. Now our transfer app is resilient to failure –and its fault tolerant.if its go down it will degrade gracefully and correctly expand to accommodate the available capacity. But who starts and stops these services so we need a cloud platform like Cloud Foundry ,AWS etc.

    In order to move it to the cloud change the various bootstrap.properties files to load configuration from config server.

    Describe its RAM, DNS route, and required services - using a manifest.yml file

14. Once we move our application to cloud where we can have multiple instance running we will need distributed Tracing tools like Zipkin.it helps us to trace the path of a request from one service to another. It's very useful in understanding where a failure is occurring in a complex chain of calls.

    We can observe correspondences and sequences in a waterfall graph in the ZipKin web UI by drilling down to the service of choice. We can further drill down to see the headers and nature of the exchange between endpoints. The Dependencies view in Zipkin shows us the topology of the cluster.
    We can use Log Aggregation and Analysis tools like Logstash ,it’s an open source, server-side data processing pipeline that ingests data from a multitude of sources simultaneously, transforms it, and then sends it to your favorite “stash.” Like Elasticsearch. it filters parse each event, identify named fields to build structure, and transform them to converge on a common format for easier, accelerated analysis and business value.
    Logstash dynamically transforms and prepare your data regardless of format or complexity:
      1.	Derive structure from unstructured data with grok
      2.	Decipher geo coordinates from IP addresses
      3.	Anonymize PII data, exclude sensitive fields completely
      4.	Ease overall processing independent of the data source, format, or schema.
    
15. Our Transfer app is not secured, in a distributed systems world, multiple clients might access multiple services and it becomes very important to have an easy-to-scale answer to the question: which clients may access which resources? The solution for this problem is single signon: all requests to a given resource present a token that may be redeemed with a centralized authentication service. We'll build an OAuth 2-powered authorization service and that secure our edge service to talk to it.

    Our application will need to talk to an authentication service that understands OAuth. We could use any of a number of valid services, like Github, Facebook, Google, or even an API Gateway product like Apigee or build custom Spring Security OAuth-powered auth-service(More preferred for transfer API).

16. Consumer Driven Contract Testing should be used to trace the changes of API as in distributed system word ,  these incompatible changes are harder to catch. They get caught in the integration tests. Integration tests are among the slowest of the tests you should have in your system. They're towards the top of the testing pyramid because they're expensive - both in terms of time and computational resources. In order to run the tests we'd need to run both client and service and all supporting infrastructure. This is a worst-case scenario; organizations move to microservices to accelerate feedback (which in turn yields learning and improvement), not to reduce it! What we need is some way to capture breaking changes that keeps both producer and consumer in sync and that doesn't constrain velocity of feedback. Spring Cloud Contract, and consumer driven contracts and consumer driven contract testing, make this work easier. The idea is that contract definitions are used to capture the expected behavior of an API for a particular client. This may include all the quirks of particular clients, and it may include older clients using older APIs. A producer may capture as many contract scenarios as needed. These contracts are enforced bilaterally. On the producer side, the Spring Cloud Contract verifier turns the contract into a Spring MVC Test Framework test that fails if the actual API doesn't work as the contract stipulates. On the consumer, clients can run test against actual HTTP (or messaging-based) APIs that are themselves stubs. These stubs are stubs - that is, there's no real business logic behind them. Just preconfigured responses defined by the contracts. As the stub is defined entirely by the contract, it is trivially cheap to run the stub APIs and exercise clients against them. As the stubs are only ever available if the producer passes all its tests, this ensures that the client is building and testing against a reflection of the latest and actual API, not the understanding of the API implied when the client test was originally written.

17. Based on the complexity in future we may need to use different services for bank account management and transfer API.

    Rest API does not support distributed transaction (2 phase commit). When developing microservices you must tackle the problem of distributed data management. Each microservice has its own private database.

    Event-driven architecture solves the distributed data management problems inherent in a microservice architecture. There are multiple ways(DB Transactional logs ,updating entities through events and event sourcing).

    Event sourcing is a method of data persistence that borrows similar ideas behind a database’s transaction log. For event sourcing, the unit of a transaction becomes much more granular, using a sequence of ordered events to represent the state of a domain object stored in a database. Once an event has been added to the event log, it cannot be removed or re-ordered. Events are considered to be immutable and the sequences of events that are stored are append-only. It has following benefits:-
      1.	Aggregates can be used to generate the consistent state of any object
      2.	It provides an audit trail that can be replayed to generate the state of an object from any point in time
      3.	It provides the many inputs necessary for analyzing data using event stream processing
      4.	It enables the use of compensating transactions to rollback events leading to an inconsistent application state
      5.	It also avoids complex synchronization between microservices, paving the way for asynchronous non-blocking operations between microservices.

    Spring Cloud Stream or Eventuate can be use to implement event sourcing. We can create command and event for create, debit and credit account and aggregator to process the event and get eventually consistent result.

18. We should package the best practices as Spring Boot starter dependencies and auto-configurations otherwise we will pass the endless list of non-functional requirements required to go to production. We can customize Spring Initializer or disable the auto configuration not used by our app to reduce the deployment time.

19. For cloud native application we have already followed initial 2 principals Codebase (using git), Dependencies (using gradle) ,for third point we have created config server in point 8 ,For 4th point backing service, A backing service is any service on which your application relies for its functionality like data stores, messaging systems, caching systems, and any number of other types of service, including services that perform line-of-business functionality or security.

    For the moment we have tight coupling with In Memory db and notification service , we need to externalize them then we can use cloud foundry cf CLI, or simply declare a backing service dependency in your application’s “manifest.yml” file to bind them automatically.

    Configuration for the various cloud-specific backing services can be handle in terms of various configuration files in the Config Server suffixed with -cloud.properties.

20. For 5th point build, release run we can create Jenkins pipeline for continuous build, integration test and deployment. 

21. For 6th point execute the app as one or more stateless, application should not manage any state if state management is required then it should be manage it in external cache or DB.

    An application can have 5 types of states:-
      1.	Persistent state (can be solve using network volume /file system independent of host and container or by event driven model using event sourcing.)
      2.	Configuration state (it can be solve using the config server
      3.	Session state (state should stored in a distributed cache or a database that can be accessed by any service instance, Sticky sessions are a violation of twelve-factor and should never be used or relied upon.)
      4.	Connection state (container load-balancing solution will also need to support routing client requests to containers for stateful protocols i.e Websockets, the load balancing solution will need to support TCP connections that persist across requests.
      5.	Cluster state (initial bootstrapping with a seed set of members, typically their IP addresses and ports, and then are able to dynamically manage membership and changes.  However, some clustered services may require a manual update and restart when membership information changes need to be propagated.)

22. For 7th Point port binding, app should be completely self contained, should not bind to any particular web server. bind to a port specified as an environment variable or conig server where we should be able to manage the port assignment because it is likely also managing routing, scaling, high availability, and fault tolerance, all of which require to manage certain aspects of the network, including routing host names to ports and mapping external port numbers to container-internal ports.

23. For 8th point concurrency, we should be able to horizontally scale out the application by adding more resources not the vertically scale up application by adding more resource. By deploying microservices using container it is easy to scale out a app.

24.	9th point disposability. We should return job to the queue when clients disconnect or time out. So it not only responsibility of service when instance went down.

25. For	10th point Dev/prod parity , declarative provisioning tools such as Chef and Puppet combined with light-weight virtual environments such as Docker and Vagrant allow developers to run local environments which closely approximate production environments. 

26. For	11th point logs. Treat logs as event streams, same in explained in point 14.

27. For	12th point, Admin processes, once your app is released, we'll need to do administrative tasks, for things like database cleanup, schema updates, and toggling features for A/B testing. If possible, administrative tasks should be delivered by the same mechanisms we use to release code, and should be tested as such. Recommend way is to use CD pipeline to automate the tasks; this way we can gain the benefits of tested code to increase your confidence in changes.
