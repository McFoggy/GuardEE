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

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.brouillard.oss.ee.microprofile.config.GuardEEConverters;

public class EnumGuardEEConvertersTest {
    @Test
    public void can_convert_directions() {
        String[] expected = new String[] {"NORTH", "SOUTH", "EAST", "WEST"};

        for (String directionString : expected) {
            DIRECTIONS converted = GuardEEConverters.asEnum(DIRECTIONS.class)
                    .get()
                    .convert(directionString);
            Assert.assertEquals(converted, DIRECTIONS.valueOf(directionString));
        }
    }

    @Test(expectedExceptions = {IllegalArgumentException.class})
    public void cannot_convert_non_existing_directions() {
        GuardEEConverters.asEnum(DIRECTIONS.class)
                .get()
                .convert("LOST");
    }

    private static enum DIRECTIONS {
        NORTH, SOUTH, EAST, WEST;
    }
}
