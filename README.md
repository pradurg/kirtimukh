# Durg | Kirtimukh
## The Guardian of Thresholds

[![Build Status](https://travis-ci.org/pradurg/kirtimukh.svg?branch=develop)](https://travis-ci.org/pradurg/kirtimukh)
[![Maintainability](https://api.codeclimate.com/v1/badges/69d188353b29f9352a34/maintainability)](https://codeclimate.com/github/pradurg/kirtimukh/maintainability)
[![Test Coverage](https://api.codeclimate.com/v1/badges/69d188353b29f9352a34/test_coverage)](https://codeclimate.com/github/pradurg/kirtimukh/test_coverage)
[![Coverage Status](https://coveralls.io/repos/pradurg/kirtimukh/badge.svg)](https://coveralls.io/r/pradurg/kirtimukh)
[![Apache V2 License](http://img.shields.io/badge/license-Apache%20V2-blue.svg)](//github.com/pradurg/kirtimukh/blob/develop/LICENSE)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.durg.kirtimukh/kirtimukh/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.durg.kirtimukh/kirtimukh)
[![Libraries.io for GitHub](https://img.shields.io/librariesio/github/pradurg/kirtimukh.svg)](https://libraries.io/github/pradurg/kirtimukh)
[![Analytics](https://ga-beacon.appspot.com/UA-181243333-1/pradurg/kirtimukh/README.md)](https://github.com/igrigorik/ga-beacon)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=pradurg_kirtimukh&metric=alert_status)](https://sonarcloud.io/dashboard?id=pradurg_kirtimukh)
[![Clojars Project](https://img.shields.io/clojars/v/io.durg.kirtimukh/kirtimukh.svg)](https://clojars.org/io.durg.kirtimukh/kirtimukh)


## Introduction
As a part of constant efforts towards Stability & Reliability, what every Software Engineer look for is High Precisions & Predictability. Especially when margin for errors & recovery gets tinier.

## Why does Fault Tolerance/Resiliency matter?
### Imagine...
* You are in an overloaded elevator, which keeps returning to the ground floor in an honest attempt to lift entire load
* Everytime you turned on Air Conditioner & Television, it results in power-cut in your entire area
* A Civil Engineer receiving a call in the middle of the night that flood gates have opened unexpectedly
* You are mid-air and flight's landing gears appear to have jammed because all passengers are watching movies

##### As an engineer, we all do understand that systems can give-up or fail anytime and they might need regular servicing/tuning.
#### However _the experience_ is frustrating as well as scary at the same time, isn't it?

## Resiliency & Microservices
### Throttling
Resiliency is the key thing to look after in Microservices Architecture.
While most of the Architectures rely completely on Circuit Breaker Pattern & Retries, which gives the complete control of _The Stability_ to the client side.
They do perform well initially in the small setup or closely working small number of microservices.
As organisations grow bigger & bigger, clients may not have fine-tuned parameters, resulting in creating back-pressures.
##### And then comes those _completely eventful_ moments when in the event of _Outages due to Back Pressure_, we end up searching for _which client_ caused this.
Throttling essentially provides a mechanism for the flow control in the event of unexpected increase in traffic.

## About
### Athang | Durg
##### _Durg_ means _Fortress_ or something difficult to conquer.

### About Kirtimukh
##### _Kirtimukh_ is the guardian of the thresholds.

[![Clojars Project](https://img.shields.io/clojars/v/io.durg.kirtimukh/kirtimukh-core.svg)](https://clojars.org/io.durg.kirtimukh/kirtimukh-core)
[![Clojars Project](https://img.shields.io/clojars/v/io.durg.kirtimukh.dw/throttling-bundle.svg)](https://clojars.org/io.durg.kirtimukh.dw/throttling-bundle)

## How to use?
A Throttling bundle for [DropWizard](//github.com/dropwizard/dropwizard).
### Maven
```
<dependency>
    <groupId>io.durg.kirtimukh.dw</groupId>
    <artifactId>throttling-bundle</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```
### Gradle 
```
compile 'io.durg.kirtimukh.dw:throttling-bundle:0.0.1-SNAPSHOT'
```

### Sample bundle initialisation
```
    bootstrap.addBundle(new ThrottlingBundle<ApplicationConfiguration>() {
        @Override
        protected ThrottlingBundleConfiguration getThrottlingConfiguration(ApplicationConfiguration appConfig) {
            return appConfig.getThrottlingConfig();
        }

        @Override
        protected ThrottlingExceptionTranslator<ApplicationException> getExceptionTranslator() {
            return new ThrottlingExceptionTranslator<ApplicationException>() {
                @Override
                public HermesException throwable(ThrottlingException e) {
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
    unit: SECOND
    threshold: 64
  commandStrategyConfigs:
    EventIngestor.publish:
      type: QUOTA
      unit: SECOND
      threshold: 32
    STATUS_APIS:
      type: LEAKY_BUCKET
      unit: SECOND
      threshold: 64
```

## Licence
This project is released under an [Apache Licence v2](http://www.apache.org/licenses/LICENSE-2.0).
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