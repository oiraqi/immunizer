# Analyzer Microservice

This is the Java implementation of the Analyzer Microservice of [Immunizer: A Scalable Loosely-Coupled Self-Protecting Software Framework using Adaptive Microagents and Parallelized Microservices](https://github.com/oiraqi/immunizer). It leverages distributed and parallel processing thanks to cluster-computing frameworks, such as Apache Spark and Google DataFlow, abstracted and unified through Apache Beam.

## Design
- **Sequence Diagram**
<p align="center">
  <img src="design/sequence-diagram.png">
</p>

## Siblings
### Autonomic Protection Microagents
- [Sensor Microagent](https://github.com/oiraqi/immunizer-sensor)
- [Effector Microagent](https://github.com/oiraqi/immunizer-effector)
### Autonomic Protection Microservices
- [Monitor Microservice](https://github.com/oiraqi/immunizer-monitor)
- [Analyzer Microservice](https://github.com/oiraqi/immunizer-analyze)
- [Planner Microservice](https://github.com/oiraqi/immunizer-plan)
- [Executor Microservice](https://github.com/oiraqi/immunizer-execute)
- [Dashboard Microservice](https://github.com/oiraqi/immunizer-dashboard)

## Dependencies

All dependencies are managed through Gradle.

## Structure
- framework: source code and dependencies managed by Gradle

## Current Environment
- Linux Ubuntu 18.04 (Bionic)
- OpenJDK 11
- Gson 2.8.6
- Apache Kafka Clients API 2.4.0

## How To
- Please refer to the parent project [How To](https://github.com/oiraqi/immunizer#how-to)

## Publications
- Please refer to the parent project [Publications](https://github.com/oiraqi/immunizer#publications)
