# GuardEE :: a Microprofile Fault Tolerance for JEE7 [![Build Status](https://travis-ci.org/McFoggy/GuardEE.svg?branch=master)](https://travis-ci.org/McFoggy/GuardEE)  

![GuardEE icon](images/guardee_blue_small.png) 

GuardEE is a _no-deps_ portable JEE7 extension implementing [Microprofile Fault Tolerance 1.0 specification](https://projects.eclipse.org/projects/technology.microprofile/releases/fault-tolerance-1.0).
It leverages JEE & CDI features to implement the specification.

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
    <artifactId>guardee-impl</artifactId>
    <version>X.Y.Z</version>
</dependency>
````

*__Gradle coordinates:__*
````
dependencies {
    compile 'org.eclipse.microprofile.fault-tolerance:microprofile-fault-tolerance-api:1.0'
    compile 'fr.brouillard.oss.jee:guardee-impl:X.Y.Z'
}
````

## TCK compliance

| Mode / Filter | Status |
| :-- | --- |
| all TCK | :x: |
| Retry tests | :heavy_check_mark: |
| Timeout tests | :heavy_check_mark: |
| Fallback tests | :heavy_check_mark: |
| CircuitBreaker tests | :heavy_check_mark: |
| Config tests | :x: |
| Bulkhead tests | :heavy_check_mark: |
| Asynchronous tests | :x: |

## Build & Test

### Build and run tests
````
mvn clean install
````

### Filter the TCKs tests
````
mvn clean install -Dit.test=FILTER -Ptestng-scan -Dserver=widfly-local
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
mvn clean install -Ptestng-scan -Dserver=wildfly-remote
````

## Branch policy

On this project several rules apply:

1. __ONLY__ the `master` branch has to be considered as a public branch.  
    As such, other branches have to be considered as temporary/working branches only and can be modified/rewritten/rebased/deleted.  
1. In order to enforce previous point, `master` branch is protected _(included for admins)_.  
That also means that any change to master has to go first to an integration branch that passes the protections in place:
    - green CI build 

## Release

In order to keep the project history clean, [jgitver](https://github.com/jgitver/jgitver-maven-plugin) is used for version management.  
`oss` & `release` profiles need to be activated during a release. 

- once on a commit that builds successfully using [Build & Test](#build--test) commands
- `git tag -a -s -m "release X.Y.Z, additionnal reason" X.Y.Z`: tag the current HEAD with the given tag name. The tag is signed by the author of the release. Adapt with gpg key of maintainer.
    - Matthieu Brouillard command:  `git tag -a -s -u 2AB5F258 -m "release X.Y.Z, additionnal reason" X.Y.Z`
    - Matthieu Brouillard [public key](https://sks-keyservers.net/pks/lookup?op=get&search=0x8139E8632AB5F258)
- `mvn -Poss,release -DskipTests deploy`
- `git push --follow-tags origin master`
