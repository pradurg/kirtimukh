# Athang | Durg | Kirtimukh
## Requests Throttling/Rate-Limiter

As a part of constant efforts towards Stability & Reliability, what every Software Engineer look for is High Precisions & Predictability. Especially when margin for errors & recovery gets tinier.

#### Why Resiliency matters?
##### Imagine...
* Everytime you turned on Air Conditioner & Television, it results in power-cut in your entire area
* You are in an overloaded elevator, which keeps returning to the ground floor in an honest attempt to lift entire load
* A Civil Engineer receiving a call in the middle of night that flood gates have opened unexpectedly
* You are mid-air and flight's landing gears appear to have jammed during landing because all passengers are watching movies

###### As an engineer, we all do understand that systems can give-up or fail anytime and they might need regular servicing/tuning.
##### However _the experience_ is frustrating as well as scary at the same time, isn't it?

#### Resiliency & Microservices Architecture
Resiliency is the key thing to look after in Microservices Architecture.
While most of the Architectures rely completely on Circuit Breaker Pattern & Retries, which gives the complete control of _The Stability_ to the client side.
They do perform well initially in the small setup or closely working small number of microservices.
As organisations grow bigger & bigger, clients may not have fine-tuned parameters, resulting in creating back-pressures.
######And then comes those _completely eventful_ moments when, in the event of outages due to _Back Pressure_, we end up searching for _which client_ caused this.

#### About Athang | Durg
######_Durg_ means something difficult to defeat

#### About Kirtimukh
######_Kirtimukh_ is the guardian of the thresholds.

#### License - ASF v2.0
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

#### Kirtimukh Components
##### Core [![Clojars Project](https://img.shields.io/clojars/v/io.kalp.athang.durg/kirtimukh-core.svg)](https://clojars.org/io.kalp.athang.durg/kirtimukh-core)
##### Throttling Bundle [![Clojars Project](https://img.shields.io/clojars/v/io.kalp.athang.durg/kirtimukh-dw-throttling-bundle.svg)](https://clojars.org/io.kalp.athang.durg/kirtimukh-dw-throttling-bundle)

#### How to use?
###### Maven Pom Dependency
```
    <dependency>
        <groupId>io.kalp.athang.durg</groupId>
        <artifactId>kirtimukh-dw-throttling-bundle</artifactId>
        <version>1.0.0.1</version>
    </dependency>
```

###### Sample bundle initialisation
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