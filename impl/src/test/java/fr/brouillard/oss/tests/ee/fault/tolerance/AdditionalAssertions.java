/**
 * Copyright © 2017 Matthieu Brouillard [http://oss.brouillard.fr/GuardEE] (matthieu@brouillard.fr)
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
package fr.brouillard.oss.tests.ee.fault.tolerance;

public abstract class AdditionalAssertions {
    private AdditionalAssertions() {}

    public static <T extends Throwable> void assertThrows(Class<T> expected, Runnable r) {
        expectThrows(expected, r);
    }

    public static <T extends Throwable> T expectThrows(Class<T> throwableClass, Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable caught) {
            if (throwableClass.isInstance(caught)) {
                return (T)throwableClass.cast(caught);
            }

            String mismatchMessage = String.format("Expected %s to be thrown, but %s was thrown", throwableClass.getSimpleName(), caught.getClass().getSimpleName());
            throw new AssertionError(mismatchMessage, caught);
        }

        String message = String.format("Expected %s to be thrown, but nothing was thrown", throwableClass.getSimpleName());
        throw new AssertionError(message);
    }
}
