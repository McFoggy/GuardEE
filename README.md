# Microprofile Fault Tolerance for JEE

This project aim is to provide a portable JEE 7 compliant extension implementing [Microprofile Fault Tolerance 1.0 specification](https://projects.eclipse.org/projects/technology.microprofile/releases/fault-tolerance-1.0).

## Usage

As a portable extension, you just have to bring the implementation in your classpath and you're done.

*__Maven coordinates:__*
````
<dependency>
    <groupId>org.eclipse.microprofile.fault-tolerance</groupId>
    <artifactId>microprofile-fault-tolerance-api</artifactId>
    <version>1.0</version>
</dependency>
<dependency>
    <groupId>fr.brouillard.oss.jee</groupId>
    <artifactId>mpft-jee-impl</artifactId>
    <version>X.Y.Z</version>
</dependency>
````

*__Gradle coordinates:__*
````
dependencies {
    compile 'org.eclipse.microprofile.fault-tolerance:microprofile-fault-tolerance-api:1.0'
    compile 'fr.brouillard.oss.jee:mpft-jee-impl:X.Y.Z'
}
````



## Build & Test

### Build and run tests against the entire TCK
````
mvn clean install -Dit.test=Retry*
````

### Filter the TCKs tests
````
mvn clean install -Dit.test=FILTER
````

Where `FILTER` is one of :

- `Retry*`
- `Timeout*`
- `Fallback*`
- `CircuitBreaker*`
- `Config*`

example with filtering on Retry tests: `mvn clean install -Dit.test=Retry*` 

### Launch tests on external server
````
mvn clean install -Dremote=wildfly
````

## Release

In order to keep the project history clean, [jgitver](https://github.com/jgitver/jgitver-maven-plugin) is used for version management.  
`oss` & `release` profiles need to be activated during a release. 

- once on a commit that builds successfully using [Build & Test](#build--test) commands
- `git tag -a -s -m "release X.Y.Z, additionnal reason" X.Y.Z`: tag the current HEAD with the given tag name. The tag is signed by the author of the release. Adapt with gpg key of maintainer.
    - Matthieu Brouillard command:  `git tag -a -s -u 2AB5F258 -m "release X.Y.Z, additionnal reason" X.Y.Z`
    - Matthieu Brouillard [public key](https://sks-keyservers.net/pks/lookup?op=get&search=0x8139E8632AB5F258)
- `mvn -Poss,release -DskipTests deploy`
- `git push --follow-tags origin master`
