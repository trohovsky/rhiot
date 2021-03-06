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
package io.rhiot.component.pi4j.mock;

import io.rhiot.component.pi4j.Pi4jConstants;
import com.pi4j.io.gpio.GpioProvider;
import com.pi4j.io.gpio.GpioProviderBase;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.exception.InvalidPinException;
import com.pi4j.wiringpi.GpioInterruptEvent;
import com.pi4j.wiringpi.GpioInterruptListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * To emulate Raspberry from RaspiGpioProvider.java (PI4J library)
 */
public class RaspiGpioProviderMock extends GpioProviderBase implements GpioProvider, GpioInterruptListener {

    private static final transient Logger LOG = LoggerFactory.getLogger(RaspiGpioProviderMock.class);

    private RaspberryPiRevision revision;

    public RaspiGpioProviderMock() {
        revision = RaspberryPiRevision.MODEL_2;
    }

    public RaspiGpioProviderMock(RaspberryPiRevision revision) {
        this.revision = revision;
    }

    @Override
    public void export(Pin pin, PinMode mode) {
        if (!hasPin(pin)) {
            throw new InvalidPinException(pin);
        }

        // cache exported state
        getPinCache(pin).setExported(true);

        // cache mode
        getPinCache(pin).setMode(mode);
    }

    @Override
    public void setMode(Pin pin, PinMode mode) {
        // cache mode
        getPinCache(pin).setMode(mode);
    }

    @Override
    public PinMode getMode(Pin pin) {
        return getPinCache(pin).getMode();
    }

    @Override
    public String getName() {
        return Pi4jConstants.PROVIDER_NAME;
    }

    @Override
    public boolean hasPin(Pin pin) {
        return pin.getAddress() <= revision.getPinNumber();
    }

    @Override
    public void pinStateChange(GpioInterruptEvent event) {
        // iterate over the pin listeners map
        for (Pin pin : listeners.keySet()) {
            // dispatch this event to the listener
            // if a matching pin address is found
            if (pin.getAddress() == event.getPin()) {
                dispatchPinDigitalStateChangeEvent(pin, PinState.getState(event.getState()));
            }
        }
    }

    @Override
    public void setPwm(Pin pin, int value) {
        super.setPwm(pin, value);
        LOG.info("Pwm {} {}", pin, value);
    }

    @Override
    public void setState(Pin pin, PinState state) {
        super.setState(pin, state);
        LOG.info("State {} {}", pin, state);
    }

    @Override
    public void setValue(Pin pin, double value) {
        // TODO Auto-generated method stub
        super.setValue(pin, value);
        LOG.info("Value {} {}", pin, value);
    }

}
