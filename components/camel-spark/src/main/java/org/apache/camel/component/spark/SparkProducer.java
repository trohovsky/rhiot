/**
 * Licensed to the Rhiot under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.spark;

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;

import java.util.List;

public class SparkProducer extends DefaultProducer {

    public SparkProducer(SparkEndpoint endpoint) {
        super(endpoint);
    }

    @Override
    protected void doStart() throws Exception {
        if(getEndpoint().getRdd() == null) {
            throw new IllegalStateException("No RDD defined.");
        }
        super.doStart();
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        JavaRDD rdd = getEndpoint().getRdd();
        if(exchange.getIn().getHeader("CAMEL_SPARK_RDD") != null) {
            rdd = (JavaRDD) exchange.getIn().getHeader("CAMEL_SPARK_RDD");
        }

        RddCallback rddCallback = null;
        if(exchange.getIn().getHeader("CAMEL_SPARK_RDD_CALLBACK") != null) {
            rddCallback = (RddCallback) exchange.getIn().getHeader("CAMEL_SPARK_RDD_CALLBACK");
        } else {
            rddCallback = getEndpoint().getRddCallback();
        }

        if(rddCallback == null) {
            Object body = exchange.getIn().getBody();

            if(body instanceof Function) {
                JavaRDD result = rdd.map((Function) body);
                collectResults(exchange, result);
            } else if(body instanceof FlatMapFunction) {
                JavaRDD result = rdd.flatMap((FlatMapFunction) body);
                collectResults(exchange, result);
            } else {
                throw new IllegalArgumentException("Unrecognized body type: " + body);
            }
        } else {
            Object body = exchange.getIn().getBody();
            Object result = body instanceof List ? rddCallback.onRdd(rdd, ((List)body).toArray(new Object[0])) : rddCallback.onRdd(rdd, body);
            collectResults(exchange, result);
        }
    }

    private void collectResults(Exchange exchange, Object result) {
        if(result instanceof JavaRDD) {
            JavaRDD rddResults = (JavaRDD) result;
            if(getEndpoint().isCollect()) {
                exchange.getIn().setBody(rddResults.collect());
            } else {
                exchange.getIn().setBody(result);
                exchange.getIn().setHeader("CAMEL_SPARK_RDD", result);
            }
        } else {
            exchange.getIn().setBody(result);
        }
    }

    @Override
    public SparkEndpoint getEndpoint() {
        return (SparkEndpoint) super.getEndpoint();
    }

}