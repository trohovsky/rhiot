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
package io.rhiot.steroids

import io.rhiot.bootstrap.classpath.ClasspathBeans
import io.rhiot.bootstrap.classpath.Named
import org.junit.Assert
import org.junit.Test

import static ClasspathBeans.bean
import static com.google.common.truth.Truth.assertThat

class ClasspathDependencyInjectionTest extends Assert {

    @Test
    void shouldCallCallback() {
        bean(ConfigurationCallback.class).get().configureMe()
    }

    @Test
    void shouldFindAllCallbacks() {
        assertThat(ClasspathBeans.beans(ConfigurationCallback.class)).hasSize(1)
    }

    @Test
    void shouldNotFindBean() {
        assertThat(bean(NotImplementedCallback.class).isPresent()).isFalse()
    }

    @Test
    void shouldFindBeanByName() {
        assertThat(bean('foo').isPresent()).isTrue()
    }

    @Test
    void shouldNotFindBeanByName() {
        assertThat(bean('unknownName').isPresent()).isFalse()
    }

    @Named(name = 'foo')
    static class NamedOnly {
    }

}