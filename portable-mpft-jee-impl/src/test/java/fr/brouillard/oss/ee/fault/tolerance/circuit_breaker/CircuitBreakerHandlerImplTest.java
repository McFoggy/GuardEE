/**
 * Copyright © 2017 Matthieu Brouillard [http://oss.brouillard.fr/portable-mpft-jee] (matthieu@brouillard.fr)
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
package fr.brouillard.oss.ee.fault.tolerance.circuit_breaker;

import java.util.concurrent.TimeUnit;

import org.eclipse.microprofile.faulttolerance.exceptions.CircuitBreakerOpenException;
import org.testng.Assert;
import org.testng.annotations.Test;

import fr.brouillard.oss.ee.fault.tolerance.AdditionalAssertions;

public class CircuitBreakerHandlerImplTest {
    @Test
    public void circuit_open_after_expected_failures() {
        int expectedFailures = 3;
        CircuitBreakerHandlerImpl cb = new CircuitBreakerHandlerImpl(
                new Class[] {Exception.class},
                TimeUnit.MILLISECONDS.toNanos(2000),
                expectedFailures,
                0.75d,
                1
                );

        Exception exception = new Exception();

        // fails the expected number of failure
        for (int i = 1; i <= expectedFailures; i++) {
            cb.enter(i);
            cb.onFailure(exception, i);
        }

        Assert.assertEquals(cb.getState(), CircuitState.OPENED);
    }

    @Test
    public void circuit_closes_after_one_expected_success() {
        int expectedFailures = 3;
        int expectedSuccess = 1;
        CircuitBreakerHandlerImpl cb = new CircuitBreakerHandlerImpl(
                new Class[] {Exception.class},
                TimeUnit.MILLISECONDS.toNanos(1000l),
                expectedFailures,
                0.75d,
                1
                );

        Exception exception = new Exception();

        // fails the expected number of failure
        for (int i = 0; i < expectedFailures; i++) {
            cb.enter();
            cb.onFailure(exception);
        }

        Assert.assertEquals(cb.getState(), CircuitState.OPENED);

        // wait to semi open the circuit
        try {
            Thread.sleep(2000l);
        } catch (InterruptedException ignore) {
        }

        // then success
        cb.enter();
        Assert.assertEquals(cb.getState(), CircuitState.SEMI_OPENED);
        cb.success();
        Assert.assertEquals(cb.getState(), CircuitState.CLOSED);
    }

    @Test
    public void mimic_CircuitBreakerTest_testCircuitHighSuccessThreshold() {
        int volumeThreshold = 4;
        int successThreshold = 3;

        CircuitBreakerHandlerImpl cb = new CircuitBreakerHandlerImpl(
                new Class[] {Exception.class},
                TimeUnit.MILLISECONDS.toNanos(1000l),
                volumeThreshold,
                0.75d,
                successThreshold
        );
        Exception exception = new Exception();

         /*
            * 1         RunTimeException
            * 2         RunTimeException
            * 3         RunTimeException
            * 4         RunTimeException
            * 5         CircuitBreakerOpenException
            * Pause for longer than CircuitBreaker delay, so that it transitions to half-open
            * 6         SUCCEED
            * 7         SUCCEED
            * 8         RunTimeException (CircuitBreaker will be re-opened)
            * 9         CircuitBreakerOpenException
        */

        cb.enter(1);
        cb.onFailure(exception, 1);

        cb.enter(2);
        cb.onFailure(exception, 2);

        cb.enter(3);
        cb.onFailure(exception, 3);

        cb.enter(4);
        cb.onFailure(exception, 4);

        Assert.assertEquals(cb.getState(), CircuitState.OPENED);
        AdditionalAssertions.assertThrows(CircuitBreakerOpenException.class, () -> cb.enter(5));
        Assert.assertEquals(cb.getState(), CircuitState.OPENED);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignore) {
        }

        Assert.assertEquals(cb.getState(), CircuitState.OPENED);
        cb.enter(6);
        Assert.assertEquals(cb.getState(), CircuitState.SEMI_OPENED);
        cb.success(6);
        Assert.assertEquals(cb.getState(), CircuitState.SEMI_OPENED);

        Assert.assertEquals(cb.getState(), CircuitState.SEMI_OPENED);
        cb.enter(7);
        Assert.assertEquals(cb.getState(), CircuitState.SEMI_OPENED);
        cb.success(7);
        Assert.assertEquals(cb.getState(), CircuitState.SEMI_OPENED);

        Assert.assertEquals(cb.getState(), CircuitState.SEMI_OPENED);
        cb.enter(8);
        Assert.assertEquals(cb.getState(), CircuitState.SEMI_OPENED);
        cb.onFailure(exception, 8);
        Assert.assertEquals(cb.getState(), CircuitState.OPENED);

        AdditionalAssertions.assertThrows(CircuitBreakerOpenException.class, () -> cb.enter(9));
    }

    @Test
    public void mimic_CircuitBreakerTest_testCircuitDefaultSuccessThreshold() {
        int volumeThreshold = 4;
        int successThreshold = 1;

        CircuitBreakerHandlerImpl cb = new CircuitBreakerHandlerImpl(
                new Class[] {Exception.class},
                TimeUnit.MILLISECONDS.toNanos(1000l),
                volumeThreshold,
                0.75d,
                successThreshold
        );
        Exception exception = new Exception();

        /*
        * 1         RunTimeException
        * 2         RunTimeException
        * 3         RunTimeException
        * 4         RunTimeException
        * 5         CircuitBreakerOpenException
        * Pause for longer than CircuitBreaker delay, so that it transitions to half-open
        * 6         SUCCEED (CircuitBreaker will be re-closed as successThreshold is 2)
        * 7         RunTimeException
        * 8         RunTimeException
        * 9         RunTimeException
        * // 10        RunTimeException
        * 11        CircuitBreakerOpenException
        */

        cb.enter(1);
        cb.onFailure(exception, 1);

        cb.enter(2);
        cb.onFailure(exception, 2);

        cb.enter(3);
        cb.onFailure(exception, 3);

        cb.enter(4);
        cb.onFailure(exception, 4);

        Assert.assertEquals(cb.getState(), CircuitState.OPENED);
        AdditionalAssertions.assertThrows(CircuitBreakerOpenException.class, () -> cb.enter(5));
        Assert.assertEquals(cb.getState(), CircuitState.OPENED);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignore) {
        }

        Assert.assertEquals(cb.getState(), CircuitState.OPENED);
        cb.enter(6);
        Assert.assertEquals(cb.getState(), CircuitState.SEMI_OPENED);
        cb.success(6);
        Assert.assertEquals(cb.getState(), CircuitState.CLOSED);

        cb.enter(7);
        Assert.assertEquals(cb.getState(), CircuitState.CLOSED);
        cb.onFailure(exception, 7);
        Assert.assertEquals(cb.getState(), CircuitState.CLOSED);

        cb.enter(8);
        Assert.assertEquals(cb.getState(), CircuitState.CLOSED);
        cb.onFailure(exception, 8);
        Assert.assertEquals(cb.getState(), CircuitState.CLOSED);

        cb.enter(9);
        Assert.assertEquals(cb.getState(), CircuitState.CLOSED);
        cb.onFailure(exception, 9);
        Assert.assertEquals(cb.getState(), CircuitState.OPENED);

//        cb.enter(10);
//        Assert.assertEquals(cb.getState(), CircuitState.CLOSED);
//        cb.onFailure(exception, 10);
//        Assert.assertEquals(cb.getState(), CircuitState.OPENED);

        AdditionalAssertions.assertThrows(CircuitBreakerOpenException.class, () -> cb.enter(11));
    }
}