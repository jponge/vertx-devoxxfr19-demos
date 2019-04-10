# LavaJUG 2019 Eclipse Vert.x demo

Initial implementation by [Julien Ponge](https://julien.ponge.org/) at _Red Hat, Inc_.

## Scenario

This demo shows various aspects of Vert.x around the toy scenario of tyres that send pressure updates to some backend.

It especially shows:

* HTTP APIs to store and query data in MongoDB,
* implementation of a _edge service_ with RxJava2 that aggregates data from 3 other HTTP services,
* Kafka-based event streaming with live updates to a web application plugged on the Vert.x event-bus.

## How to run it

Just enjoy live reload

    # In one terminal
    $ docker compose-up

    # In a second terminal
    $ ./gradlew vertxRun

    # In a third terminal, have fun with the APIs:
    $ http POST :3000/ingest tyreId=123 pressure:=12.5
    # (...)

The Kafka example is not running from the `vertxRun` task to avoid polluting the logs, you can just run each instance from the `main` method of the classes in `demo.eventdriven` from your IDE.

## How to generate some workload

See `locustfile.py` with sections to uncomment to generate traffic to the HTTP APIs with [Locust](https://locust.io/).

It's not worth focusing on the reported data, since all instances are running as single verticles, and everything happens on the same machine anyway.
I also have no idea of how _correct_ Locust is, especially with respect to _coordinated omission_.
