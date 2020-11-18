# Durg | Kirtimukh
## The Guardian of Thresholds

[![Build Status][Badge-Travis]][Link-Travis]
[![Libraries.io for GitHub][Badge-Libraries]][Link-Libraries]
[![Javadoc][Badge-Javadoc]][Link-Javadoc]
[![Apache V2 License][Badge-License]][Link-License]

[![Maintainability][Badge-Maintainability]][Link-Maintainability]
[![Quality Gate Status][Badge-SonarCloud]][Link-SonarCloud]
[![Coverage Status][Badge-Coderalls]][Link-Coderalls]
[![Codecov][Badge-Codecov]][Link-Codecov]

[![Snapshot Artifacts][Badge-SonatypeSnapshots]][Link-SonatypeSnapshots]
[![Maven Central][Badge-MavenCentral]][Link-MavenCentral]
[![Release Artifacts][Badge-SonatypeReleases]][Link-SonatypeReleases]
[![Clojars Project][Badge-Clojars]][Link-Clojars]
<!--
[![Test Coverage][Badge-Codeclimate]][Link-Codeclimate]
-->
[![Analytics][Badge-Analytics]][Link-Analytics]
[![Twitter][Badge-Twitter]][Link-Twitter]

## Introduction
As a part of constant efforts towards Stability & Reliability, what every Software Engineer look for is High Precisions & Predictability. Especially when margin for errors & recovery gets tinier.

## Why does Fault Tolerance/Resiliency matter?
### Imagine...
* You are in an overloaded elevator, which keeps returning to the ground floor in an honest attempt to lift entire load
* Everytime you turned on Air Conditioner & Television, it results in power-cut in your entire area
* A Civil Engineer receiving a call in the middle of the night that flood gates have opened unexpectedly
* You are mid-air and flight's landing gears appear to have jammed because all passengers are watching movies

##### As an engineer, we all do understand that systems can give-up or fail anytime and they might need regular servicing/tuning.
#### However _the experience is frustrating_ as well as scary at the same time, isn't it?

## Resiliency & Microservices
Resiliency is the key thing to look after in Microservices Architecture.
### Circuit Breaker
One of the most important or moreover the critical/key aspects of Microservices Architecture is fault tolerance using Circuit Breakers.
One may choose between FailFast or FailSafe approaches, purely depends on the use-cases.
While the FailFast approach effectively provides fast failing, faster recovery & effectively better utilisation of resources, FailSafe approach tolerates fluctuations.

### Throttling
While most of the Architectures rely completely on Circuit Breaker Pattern & Retries, which gives the complete control of _The Stability_ to the client side.
They do perform well initially in the small setup or closely working small number of microservices.
As organisations grow bigger & bigger, clients may not have fine-tuned parameters, resulting in creating back-pressures.
##### And then comes those _completely eventful_ moments when in the event of _Outages due to Back Pressure_, we end up searching for _which client_ caused this.
Throttling essentially provides a mechanism for the flow control in the event of unexpected increase in incoming traffic or increase in latencies from downstream.

## About Durg | Kirtimukh
##### _Durg_ means _Fortress_ or something difficult to conquer.

### About Kirtimukh
##### _Kirtimukh_ is the guardian of the thresholds.

##### Current Version
[![Maven Central][Badge-MavenCentral]][Link-MavenCentral]

[![Snapshot Artifacts][Badge-SonatypeReleases]][Link-SonatypeReleases]

[![Release Artifacts][Badge-SonatypeSnapshots]][Link-SonatypeSnapshots]


## How to use throttling?
A Throttling bundle for [DropWizard](//github.com/dropwizard/dropwizard).
### Rate-Limiting/Throttling Strategies
All strategies can be applied on one single command or bucket of commands. This enables us bucketizing similar API calls which are being served under different versions.
This also helps in providing a mechanism to traffic structuring on different windows.

#### Quota based
Quota based rate-limiting can be used where one needs to restrict number of request volumes based timed windows e.g. a second, a minute, 5 seconds or 10 minutes.

#### Leaky Bucket
Wish to serve high requests when systems are healthy but throttle when downstream becomes latent or unhealthy, this strategy shall help.
This is the preferred strategy for rate-limiting as well as traffic shaping/flow control if downstream is latent due to external factors which are not in our control.

#### Priority Bucket
Unsure about traffic patterns but want to give preference to certain commands/buckets, in an unexpected event or things start falling apart, Priority Bucket is the right thing to do.

#### Custom Controller
Define your own Centralised or Decentralised way to handle rate-limiting.

[![Maven Central][Badge-MavenCentral]][Link-MavenCentral]

[![Release Artifacts][Badge-SonatypeReleases]][Link-SonatypeReleases]

### Maven
```
<dependency>
    <groupId>io.durg.kirtimukh.dw</groupId>
    <artifactId>throttling-bundle</artifactId>
    <version>0.0.2</version>
</dependency>
```
### Gradle 
```
compile 'io.durg.kirtimukh.dw:throttling-bundle:0.0.2'
```

### Sample bundle initialisation
```
    bootstrap.addBundle(new ThrottlingBundle<ApplicationConfiguration>() {
        @Override
        protected ThrottlingBundleConfiguration getThrottlingConfiguration(ApplicationConfiguration appConfig) {
            return appConfig.getThrottlingConfig();
        }

        @Override
        protected CustomThrottlingController getCustomController() {
            // Introduce custom controller logic here
            return null;
        }

        @Override
        protected ThrottlingExceptionTranslator<ApplicationException> getExceptionTranslator() {
            return new ThrottlingExceptionTranslator<ApplicationException>() {
                @Override
                public ApplicationException throwable(ThrottlingException e) {
                    return new ApplicationException(ResponseCode.TOO_MANY_REQUESTS, "Too Many Requests");
                }
            };
        }
    });
```

### Sample config
```
throttlingConfig:
  defaultStrategyConfig:
    type: LEAKY_BUCKET
    threshold: 64
  commandStrategyConfigs:
    EventIngestor.publish:
      type: LEAKY_BUCKET
      threshold: 16
    STATUS_APIS_BUCKET:
      type: QUOTA
      unit: SECOND
      threshold: 64
```

### Sample code
```
// Single API level throttling
@Throttle
public Response publish(EventPublishRequest request)

// API Bucket level throttling
@Throttle(bucket = STATUS_APIS_BUCKET)
public Response statusV1(StatusRequest request)

@Throttle(bucket = STATUS_APIS_BUCKET)
public Response statusV2(StatusRequest request)

@Throttle(bucket = STATUS_APIS_BUCKET)
public Response statusV3(StatusRequest request)
```
## License
[![Apache V2 License][Badge-License]][Link-License]

This project has been released under an [Apache License v2](http://www.apache.org/licenses/LICENSE-2.0).
### ASF v2.0
```
 Copyright (c) 2020 Pradeep A. Dalvi <prad@apache.org>

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
```

[Link-Analytics]: https://github.com/igrigorik/ga-beacon
[Link-Clojars]: https://clojars.org/io.durg.kirtimukh/kirtimukh
[Link-Codeclimate]: https://codeclimate.com/github/pradurg/kirtimukh/test_coverage
[Link-Codecov]: https://codecov.io/gh/pradurg/kirtimukh 
[Link-Coderalls]: https://coveralls.io/r/pradurg/kirtimukh
[Link-Javadoc]: https://javadoc.io/doc/io.durg.kirtimukh/kirtimukh/latest/zio/index.html
[Link-Libraries]: https://libraries.io/github/pradurg/kirtimukh
[Link-License]: https://github.com/pradurg/kirtimukh/blob/develop/LICENSE
[Link-Maintainability]: https://codeclimate.com/github/pradurg/kirtimukh/maintainability "Average time to resolve an issue"
[Link-MavenCentral]: https://maven-badges.herokuapp.com/maven-central/io.durg.kirtimukh/kirtimukh
[Link-SonarCloud]: https://sonarcloud.io/dashboard?id=pradurg_kirtimukh
[Link-SonatypeReleases]: https://oss.sonatype.org/content/repositories/releases/io/durg/kirtimukh/kirtimukh/ "Sonatype Releases"
[Link-SonatypeSnapshots]: https://oss.sonatype.org/content/repositories/snapshots/io/durg/kirtimukh/kirtimukh/ "Sonatype Snapshots"
[Link-Travis]: https://travis-ci.org/pradurg/kirtimukh "travis-ci"
[Link-Twitter]: https://twitter.com/pradurg

[Badge-Analytics]: https://ga-beacon.appspot.com/UA-181243333-1/pradurg/kirtimukh/README.md
[Badge-Clojars]: https://img.shields.io/clojars/v/io.durg.kirtimukh/kirtimukh.svg
[Badge-Codeclimate]: https://api.codeclimate.com/v1/badges/69d188353b29f9352a34/test_coverage
[Badge-Codecov]: https://codecov.io/gh/pradurg/kirtimukh/branch/develop/graph/badge.svg
[Badge-Coderalls]: https://coveralls.io/repos/pradurg/kirtimukh/badge.svg
[Badge-Javadoc]: https://javadoc.io/badge2/io.durg.kirtimukh/kirtimukh/javadoc.svg?label=Javadoc "javadoc"
[Badge-Libraries]: https://img.shields.io/librariesio/github/pradurg/kirtimukh.svg?label=Dependencies
[Badge-License]: http://img.shields.io/badge/license-Apache%20v2-blue.svg?label=License
[Badge-Maintainability]: https://api.codeclimate.com/v1/badges/69d188353b29f9352a34/maintainability "Average time to resolve an issue"
[Badge-MavenCentral]: https://maven-badges.herokuapp.com/maven-central/io.durg.kirtimukh/kirtimukh/badge.svg
[Badge-SonarCloud]: https://sonarcloud.io/api/project_badges/measure?project=pradurg_kirtimukh&metric=alert_status
[Badge-SonatypeReleases]: https://img.shields.io/nexus/r/https/oss.sonatype.org/io.durg.kirtimukh/kirtimukh.svg?label=Release "Sonatype Releases"
[Badge-SonatypeSnapshots]: https://img.shields.io/nexus/s/https/oss.sonatype.org/io.durg.kirtimukh/kirtimukh.svg?label=Snapshot "Sonatype Snapshots"
[Badge-Travis]: https://travis-ci.org/pradurg/kirtimukh.svg?branch=develop&label=Build "travisci"
[Badge-Twitter]: https://img.shields.io/twitter/follow/pradurg.svg?style=plastic&label=Follow%20@pradurg&logo=twitter