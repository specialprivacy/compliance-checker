# Compliance Checker
This is a stream processor which check if application logs are compliant with user consent policies. It relies on HermiT and implements the policy language from deliverable D2.1

## TODO
+ Connect properly to KAFKA

## Run
The application currently runs as Tomcat webapp. In the future it will most likely become a plain old java app (it does not have any UI to expose).
In its current form policies are loaded through a REST API.

A docker image is available at `specialregistry.wdullaer.com/special/complaince-checker`

Configuration is passed in through environment variables:

* *POLICY_CLASS_BASE*: the base for the policy class URI's
* *RULES_DIRECTORY*: the directory containing the rules **mandatory**
* *KAFKA_URL_LIST*: The hostname of a kafka broker to boostrap the connection to the cluster
* *KAFKA_CLIENT_ID*: The clientId used for grouping multiple instances of this app together in a single consumer group
* *KAFKA_TOPIC_POLICY*: The topic containing the full policies to be validated
* *KAFKA_TOPIC_ACCESS*: The topic containing the application logs to be validated
* *KAFKA_TOPIC_CONSENT*: The topic containing the application logs to be validated

## Build
### Local
The application uses jdk-8. You will need to have maven and a working jdk installed

```bash
mvn clean build
```

### Docker
The docker image can be built in the standard way

```bash
docker build .
```

## TODO
* Actually get the application to run
* Remove the dependency on Tomcat (double check with Jonathan)
* Add some spaces in the environment variables
* Add sane default values to the configuration parameters
* Replace the kafka configuration variables with a comma separated default broker list (as is idiomatic in most kafka using applications)
* Add some drawing explaining the data flow
