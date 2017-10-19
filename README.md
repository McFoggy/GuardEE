# Microprofile Fault Tolerance for JEE

## Release

## Build

Filter tests
````
mvn clean install -Dit.test=Retry*
````

Available TCKs tests:

- Retry*
- Timeout*
- Fallback*
- CircuitBreaker*
- Config*

Launch tests on external server

````
mvn clean install -Dremote=wildfly
````
