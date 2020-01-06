# Compliance Checker

## Description
This is a stream processor which check if application logs are compliant with user consent policies. It relies on HermiT and implements the policy language from deliverable D2.1

## Configuration
The application runs as a standalone jar file. Everything is packaged as an uberjar, so no additional dependencies
are required.
A docker image is available at `registry-special.tenforce.com/special/compliance-checker`

Configuration is passed in through environment variables:

* *LOGGING_LEVEL*: the verbosity of the logs (oneOf: [`trace`, `debug`, `info`, `warn`, `error`], default: `info`)
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
