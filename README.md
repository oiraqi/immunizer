# Immunizer: A Scalable Loosely-Coupled Self-Protecting Software Framework using Adaptive Microagents and Parallelized Microservices

Immunizer strives to empower software applications with *artificial immunity* against cyber attacks.
To achieve this goal, Immunizer enables applications themselves to play a *central* and *active* role in the intrusion detection and response processes. While traditional network and host intrusion detection systems have access to raw strings and bytes through I/O operations only, Immunizer allows tracking application domain objects all along the processing lifecycle.

Thanks to unsupervised learning, Immunizer leverages the application business context and learns from production data, without creating any training burden on the application owner. Moreover, as Immunizer uses runtime application instrumentation, it incurs no additional cost on the application provider.

In terms of effectiveness, Immunizer R-precision exceeds 97%, while its cloud-based, fully-distributed and parallelized microservices architecture provides high performance and scalability.

Immunizer can be extended through cloud-based collaboration. The aim is to create and manage *software communities* whose members automatically share security alarms and learn from each other. This allows Immunizer to evolve from empowering applications with immunity like the *human body*, to providing them with a sense of belonging and an ability to collaborate like in *human societies*. All related details are given in the corresponding project: [Communizer: A Collaborative Cloud-based Self-Protecting Software Communities Framework](https://github.com/oiraqi/communizer).

## Architecture

Immunizer is made of several modules/layers, each designed and implemented as either an in-app microagent, an on-premise microservice, or a cloud microservice. These are:
- Microagents:
  - [Sensor Microagent](https://github.com/oiraqi/immunizer-sensor)
  - [Effector Microagent](https://github.com/oiraqi/immunizer-effector)
- Microservices:
  - [Monitoring Microservice](https://github.com/oiraqi/immunizer-monitor)
  - [Analysis Microservice](https://github.com/oiraqi/immunizer-analyze)
  - [Planning Microservice](https://github.com/oiraqi/immunizer-plan)
  - [Execution Microservice](https://github.com/oiraqi/immunizer-execute)
  - [Dashboard Microservice](https://github.com/oiraqi/immunizer-dashboard)

Communication among these modules/layers is based on streams and is performed asynchronously, through brokers.

## How To
- Make sure you have Git, Docker and Docker Compose installed
- git clone https://github.com/oiraqi/immunizer.git
- cd immunizer/docker
- docker-compose up --build
- Login to the different containers
  - docker exec -it immunizer-microagents bash
    - cd immunizer-sensor/scripts
    - ./ofbiz-immunized.sh
  - docker exec -it immunizer-microservices bash
    - cd /root/immunizer-monitor/scripts
    - ./build-spark-submit
    - cd /root/immunizer-analyze/scripts
    - ./build-spark-submit

## Publications
- O. Iraqi and H. El Bakkali, "Immunizer: A Scalable Loosely-Coupled Self-Protecting Software Framework using Adaptive Microagents and Parallelized Microservices" 2020 IEEE 29th International Conference on Enabling Technologies: Infrastructure for Collaborative Enterprises (WETICE), Bayonne, France, 2020, pp. 24-27, doi: 10.1109/WETICE49692.2020.00013.
- Omar Iraqi and Hanan El Bakkali, “Application-Level Unsupervised Outlier-Based Intrusion Detection and Prevention” Security and Communication Networks, vol. 2019, Article ID 8368473, 13 pages, 2019. https://doi.org/10.1155/2019/8368473
- Omar Iraqi, Meryeme Ayach and Hanan El Bakali, "Collaborative Cloud-based Application-level Intrusion Detection and Prevention", ICWMC 2019 : The Fifteenth International Conference on Wireless and Mobile Communications, IARIA.
- Iraqi O., El Bakkali H. (2017) Toward Third-Party Immune Applications. In: Rak J., Bay J., Kotenko I., Popyack L., Skormin V., Szczypiorski K. (eds) Computer Network Security. MMM-ACNS 2017. Lecture Notes in Computer Science, vol 10446. Springer, Cham
