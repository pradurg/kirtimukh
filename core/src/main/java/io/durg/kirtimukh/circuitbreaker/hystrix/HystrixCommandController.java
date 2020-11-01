/*
 * Copyright (c) 2020 Pradeep A. Dalvi <prad@apache.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.durg.kirtimukh.circuitbreaker.hystrix;

import com.google.common.base.Strings;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolKey;
import io.durg.kirtimukh.circuitbreaker.CircuitBreakerKey;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.util.GlobalTracer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.MDC;

import java.util.Map;

/**
 * Created by pradeep.dalvi on 27/10/20
 */
public class HystrixCommandController {
    //ConfigurationManager.getConfigInstance();
    private static final String TRACE_ID = "TRACE-ID";

    private static HystrixCommand.Setter setter(final CircuitBreakerKey circuitBreakerKey) {
        return HystrixCommand.Setter
                .withGroupKey(HystrixCommandGroupKey.Factory.asKey(circuitBreakerKey.getGroup()))
                .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey(circuitBreakerKey.getDomain()))
                .andCommandKey(HystrixCommandKey.Factory.asKey(circuitBreakerKey.getCommand()));
    }

    private static <R> HystrixCommand<R> createTraceable(final HystrixCommand.Setter setter,
                                                         final String traceId,
                                                         final HystrixCommandExecutor<R> executor) {

        return new HystrixCommand<R>(setter) {
            final Map<String, String> parentMDCContext = MDC.getCopyOfContextMap();
            final Span parentActiveSpan = (GlobalTracer.get() != null)
                    ? GlobalTracer.get().activeSpan()
                    : null;

            @Override
            protected R run() throws Exception {
                Scope scope = null;
                if (parentMDCContext != null) {
                    MDC.setContextMap(parentMDCContext);
                }

                if (parentActiveSpan != null) {
                    scope = GlobalTracer.get()
                            .scopeManager()
                            .activate(parentActiveSpan);
                }

                if (!Strings.isNullOrEmpty(traceId)) {
                    MDC.put(TRACE_ID, traceId);
                }

                try {
                    return executor.execute();
                } catch (Throwable t) {
                    throw (Exception) t;
                } finally {
                    if (scope != null) {
                        scope.close();
                    }

                    HystrixCommandProperties.ExecutionIsolationStrategy isolationStrategy = getProperties()
                            .executionIsolationStrategy()
                            .get();
                    if (isolationStrategy == HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE) {
                        if (!Strings.isNullOrEmpty(traceId)) {
                            MDC.remove(TRACE_ID);
                        }
                    } else {
                        MDC.clear();
                    }
                }
            }
        };
    }

    private static <R> HystrixCommand<R> create(final HystrixCommand.Setter setter,
                                                final HystrixCommandExecutor<R> executor) {

        return new HystrixCommand<R>(setter) {
            @Override
            protected R run() throws Exception {
                try {
                    return executor.execute();
                } catch (Throwable t) {
                    throw (Exception) t;
                }
            }
        };
    }

    private static <R> R execute(final CircuitBreakerKey circuitBreakerKey,
                                 final String traceId,
                                 final HystrixCommandExecutor<R> executor) {
        final HystrixCommand.Setter setter = setter(circuitBreakerKey);

        HystrixCommand<R> hystrixCommand = createTraceable(setter, traceId, executor);

        return hystrixCommand.execute();
    }

    private static <R> R execute(final CircuitBreakerKey circuitBreakerKey,
                                 final String traceId,
                                 final ProceedingJoinPoint joinPoint) {
        return execute(circuitBreakerKey, traceId, (HystrixCommandExecutor<R>) () -> (R) joinPoint.proceed());
    }

    public static <R> R execute(final CircuitBreakerKey circuitBreakerKey,
                                final ProceedingJoinPoint joinPoint) {
        return execute(circuitBreakerKey, null, joinPoint);
    }
}