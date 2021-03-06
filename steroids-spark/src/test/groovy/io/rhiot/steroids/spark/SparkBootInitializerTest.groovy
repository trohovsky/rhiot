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
package io.rhiot.steroids.spark

import io.rhiot.bootstrap.Bootstrap
import org.apache.spark.api.java.JavaSparkContext
import org.junit.AfterClass
import org.junit.Test

import static com.google.common.truth.Truth.assertThat

class SparkBootInitializerTest {

    // Fixtures

    static def bootstrap = new Bootstrap().start()

    @AfterClass
    static void afterClass() {
        bootstrap.stop()
    }

    // Tests

    @Test
    void shouldStartSparkContext() {
        // Given
        def sparkContext = bootstrap.beanRegistry().bean(JavaSparkContext.class).get()

        // When
        def aCounter = { line -> line.contains('a') }.dehydrate()
        def linesCount = sparkContext.textFile('pom.xml').cache().filter(aCounter).count()

        // Then
        assertThat(linesCount).isGreaterThan(0L)
    }

}
