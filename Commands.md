# Recurrent useful commands

## Run tests on running server

`mvn clean install -Dserver=SERVER_TYPE`

For example

### Local Wildfly server

`mvn clean install -Dserver=wildfly-remote`

### Remote Wildfly server

````
mvn clean install -Dserver=wildfly-remote \
    -Dserver.username=ADMIN_NAME \
    -Dserver.password=ADMIN_PASSWORD \
    -Dserver.ip=SERVER \
    -Dserver.managementPort=PORT (default 9990)
````

### Local Glassfish server

`mvn clean install -Dserver=glassfish`


## Manual selection of tests via CLI

The `manual` profile needs to be activated.
By doing so, testng classes will be scanned instead of using a testng suite.

This scan then allows to use standard `failsafe` filters to select tests. 

For example, to run only `Retry` tests on a local running wildfly:

```
mvn clean verify -Pmanual -Dserver=wildfly-remote -Dit.test=Retry*
```
