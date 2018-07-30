# Compliance Checker
This is a stream processor which check if application logs are compliant with user consent policies. It relies on HermiT and implements the policy language from deliverable D2.1

## Run
The application runs as a standalone jar file. Everything is packaged as an uberjar, so no additional dependencies
are required.
A docker image is available at `registry-special.tenforce.com/special/complaince-checker`

Configuration is passed in through environment variables:

* *RULES_DIRECTORY*: the directory containing the rules (default: `/rules`)
* *KAFKA_URL_LIST*: A comma separated list of kafka brokers to bootstrap the connection to the cluster (default: `localhost:9092`)
* *KAFKA_CLIENT_ID*: The clientId used for grouping multiple instances of this app together in a single consumer group (default: `UUID.randomUUID()`)
* *KAFKA_TOPIC_POLICY*: The topic containing the full policies to be validated (default: `full-policies`)
* *KAFKA_TOPIC_ACCESS*: The topic containing the application logs to be validated (default: `accesses`)
* *KAFKA_TOPIC_CONSENT*: The topic containing the application logs to be validated (default: `consents`)

## Build
### Local
The application uses jdk-8. You will need to have maven and a working jdk installed

```bash
mvn clean package
```

### Docker
The docker image can be built in the standard way

```bash
docker build .
```

## TODO
* Add more configuration validation
* Tests
* Add support for Piero's custom algorithm
* Add support for time dimension on logs and policies (logs should be checked with the policy valid at the proper time)
* Move vocabulary constants into an enum that can be shared
* Reevaluate package structure
* Preallocate buffer when reading from rocksdb
* Better error handling, manual offset committing, in ApplicationLogConsumer (see todo's in code)
* Investigate keeping often used user policies in memory
