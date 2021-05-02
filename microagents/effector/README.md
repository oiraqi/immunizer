# Response Microagent

This is the Java implementation of the Effector Microagent of [Immunizer: A Scalable Loosely-Coupled Self-Protecting Software Framework using Adaptive Microagents and Parallelized Microservices](https://github.com/oiraqi/immunizer)

## Siblings
### Autonomic Protection Microagents
- [Sensor Microagent](https://github.com/oiraqi/immunizer-sensor)
- [Effector Microagent](https://github.com/oiraqi/immunizer-effector)
### Autonomic Protection Microservices
- [Monitoring Microservice](https://github.com/oiraqi/immunizer-monitor)
- [Analysis Microservice](https://github.com/oiraqi/immunizer-analyze)
- [Planning Microservice](https://github.com/oiraqi/immunizer-plan)
- [Execution Microservice](https://github.com/oiraqi/immunizer-execute)
- [Dashboard Microservice](https://github.com/oiraqi/immunizer-dashboard)

## Dependencies

All dependencies are managed through Docker and Gradle. Docker is all what you need, while Gradle distribution will be automatically fetched by Gradle wrapper, which will be downloaded from this repository by Docker.

## Structure
- docker: hosts Dockerfile, all what you need to build your development and test environment
- framework: source code and dependencies managed by Gradle
- scripts: folder containing build/run scripts

## Current Environment / Docker Image
- Linux Ubuntu 18.04 (Bionic)
- OpenJDK 8
- ByteBuddy 1.10.3
- Gson 2.8.6
- Apache Kafka Clients API 2.4.0
- OFBiz (nightly built)

## How To
- Please refer to the parent project [How To](https://github.com/oiraqi/immunizer#how-to)

## Publications
- Please refer to the parent project [Publications](https://github.com/oiraqi/immunizer#publications)
