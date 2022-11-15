# crazy-event-gateway
Project for the Cloud Edge Services team
Imagine you are building a service in Java called CRAZY EVENT GATEWAY. (Use any Java-based technology you like to build it.) The service is expected to be accessed by clients and also by other services. CRAZY EVENT GATEWAY provides an interface
 (via a REST API or some appropriate alternative of your choosing) for ingesting events. Every event is associated with an EVENT_TYPE. There are a fixed number of EVENT_TYPEs; let's say 4.
 
 The gateway segregates received events based on their EVENT_TYPEs and writes them to outbound sinks in batches. The outbound sinks are intended to be kafka topics, but for the purpose of this miniproject, it is acceptable to append the batches to log files instead. (Of course, feel free to jump right to using kafka topics if you like.) 


 Each batch may contain no more than 1000 events. Once a batch contains 1000 events, it must be sent to the appropriate sink right away. Although we aim to send 1000 events in each batch, no batch (and hence no event) should remain in the gateway for longer than roughly 15 seconds, even if it contains less than 1000 events.

The event gateway must also include a homemade rate limiter. It must limit ingestion to M requests per minute and H requests per hour. So if M=1000 and H=50,000, then the rate limiter should permit no more than 1000 requests in any given minute and no more than 50,000 requests in any consecutive 60 minute span. 

Aside from implementing the rate limiter and event processing logic, feel free to leverage any existing code or useful technology available.

If any questions come up during development, slack us for clarification.



 
Evaluation Criteria
Design and implement the service API contract. (One could characterize this as the ingestion operation "signature" or the request/response format covering various cases.) Make your service API close to a real-world service API (if a small one). (P1)
Implement the rate limiter. (P1)
Implement the logic to batch and write events to the sinks per the above requirements with sensible class designs. Be sure the system is efficient and will be able to handle heavy load (many requests per unit of time). (P1)
Make sure the service actually works, and be prepared to demo it using some client (Postman, a homemade client, some other appropriate client). (P1)
Write 2 or 3 important unit (or other) tests. (P1)

Make the system configurable where possible/appropriate. (P2)
Incorporate intelligent logging and exception handling. (P2)
Somehow document the service API contract. (P2)
Time permitting, add some security to your service. (P2)

Follow-up session:
Prepare to explain/justify your code and design choices.
Prepare to brainstorm how we might accommodate additional requirements into the system design. Examples:
High scalability/availability: How would we scale your solution (how can it be scaled to millions of requests per some unit of time, hundreds of event types, etc.)?
Security: What are some of the security holes in the system so far, and how could we fix them?
Etc.
