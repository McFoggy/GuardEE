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
package fr.brouillard.oss.tests.ee.microprofile.config;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;

import org.testng.annotations.Test;

public class ConvertersTest {
    public static void main(String...args) {
        System.out.println(LocalTime.now().toString());
        System.out.println(LocalDate.now().toString());
        System.out.println(LocalDateTime.now().toString());
        System.out.println(OffsetDateTime.now().toString());
        System.out.println(OffsetTime.now().toString());
        System.out.println(Clock.systemDefaultZone().instant().toString());
    }
}
