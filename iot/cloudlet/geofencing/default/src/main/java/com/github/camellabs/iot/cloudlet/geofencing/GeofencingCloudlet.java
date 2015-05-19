/*
 * Licensed to the Camel Labs under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.camellabs.iot.cloudlet.geofencing;

import org.apache.camel.spring.boot.FatJarRouter;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.CustomConversions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SpringBootApplication
@ComponentScan("com.github.camellabs.iot.cloudlet")
public class GeofencingCloudlet extends FatJarRouter {

    @Override
    public void configure() throws Exception {
        from("timer:analyzeRoutes?period=60000&delay={{camellabs.iot.cloudlet.geofencing.routes.analysis.delay:15000}}").
                beanRef("routeService", "clients").split().body().parallelProcessing().
                beanRef("routeService", "analyzeRoutes");
    }

    @Bean
    public CustomConversions customConversions() throws Exception {
        List<Converter<?, ?>> converterList = new ArrayList<Converter<?, ?>>();
        converterList.add(new Converter<Long, Date>() {
            @Override
            public Date convert(Long source) {
                return new Date(source);
            }
        });
        return new CustomConversions(converterList);
    }

}