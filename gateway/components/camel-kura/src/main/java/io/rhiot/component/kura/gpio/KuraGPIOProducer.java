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
package io.rhiot.component.kura.gpio;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultProducer;
import org.eclipse.kura.gpio.KuraClosedDeviceException;
import org.eclipse.kura.gpio.KuraGPIODirection;
import org.eclipse.kura.gpio.KuraGPIOPin;
import org.eclipse.kura.gpio.KuraUnavailableDeviceException;

/**
 * The Pin producer.
 */
public class KuraGPIOProducer extends DefaultProducer {

    private KuraGPIOPin pin;
    private KuraGPIOAction action;
    private ExecutorService pool;

    public KuraGPIOProducer(KuraGPIOEndpoint endpoint, KuraGPIOPin pin) {
        super(endpoint);

        this.pin = pin;
        this.action = endpoint.getAction();
        this.pool = this.getEndpoint().getCamelContext().getExecutorServiceManager().newSingleThreadExecutor(this,
                KuraGPIOConstants.CAMEL_KURA_GPIO_THREADPOOL + pin.getIndex());
    }

    private void output(Exchange exchange)
            throws KuraUnavailableDeviceException, KuraClosedDeviceException, IOException {

        //
        KuraGPIODirection direction = pin.getDirection();
        log.debug("Mode > " + direction + " for " + pin);

        // Check mode
        switch (direction) {

        case OUTPUT:
            Boolean outputBoolean = exchange.getIn().getBody(Boolean.class);
            pin.setValue(outputBoolean);
            break;

        case INPUT:
            exchange.getIn().setHeader(KuraGPIOConstants.CAMEL_KURA_GPIO_ID, getPin().getIndex());
            exchange.getIn().setHeader(KuraGPIOConstants.CAMEL_KURA_GPIO_NAME, getPin().getName());
            exchange.getIn().setBody(pin.getValue());
            break;

        default:
            log.error("Any PinDirection found");
            break;
        }

    }

    protected KuraGPIOAction resolveAction(Message message) {
        if (message.getHeaders().containsKey(KuraGPIOConstants.CAMEL_KURA_GPIO_ACTION)) {
            // Exchange Action
            return message.getHeader(KuraGPIOConstants.CAMEL_KURA_GPIO_ACTION, KuraGPIOAction.class);
        } else {
            return getAction(); // Endpoint Action
        }
    }

    /**
     * Process the message
     */
    @Override
    public void process(Exchange exchange) throws Exception {
        if (log.isTraceEnabled()) {
            log.trace(exchange.toString());
        }

        KuraGPIOAction messageAction = resolveAction(exchange.getIn());

        if (messageAction == null) {
            log.trace("No action pick up body");
            output(exchange);
        } else {
            log.trace("action= {} ", action);
            switch (messageAction) {

            case TOGGLE:
                pin.setValue(!pin.getValue());
                break;

            case LOW:
                pin.setValue(false);
                break;

            case HIGH:
                pin.setValue(true);
                break;

            case BLINK:

                pool.submit(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            Thread.sleep(((KuraGPIOEndpoint) getEndpoint()).getDelay());
                            pin.setValue(!pin.getValue());
                            Thread.sleep(((KuraGPIOEndpoint) getEndpoint()).getDuration());
                            pin.setValue(!pin.getValue());
                        } catch (Exception e) {
                            log.error("Thread interruption into BLINK sequence", e);
                        }
                    }
                });

                break;

            default:
                log.error("Any action set found");
                break;
            }
        }
    }

    @Override
    protected void doShutdown() throws Exception {
        // 2 x (delay + timeout) + 5s
        long timeToWait = (((KuraGPIOEndpoint) getEndpoint()).getDelay()
                + ((KuraGPIOEndpoint) getEndpoint()).getDuration()) * 2 + 5000;
        log.debug("Wait for {} ms", timeToWait);
        pool.awaitTermination(timeToWait, TimeUnit.MILLISECONDS);
        pin.close();
        log.debug("Pin {} {}", pin.getIndex(), pin.getValue());
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        pin.open();
    }

    public KuraGPIOPin getPin() {
        return pin;
    }

    public void setPin(KuraGPIOPin pin) {
        this.pin = pin;
    }

    public KuraGPIOAction getAction() {
        return action;
    }

    public void setAction(KuraGPIOAction action) {
        this.action = action;
    }

}