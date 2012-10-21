/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sis.io;

import org.junit.Test;
import org.apache.sis.test.TestCase;

import static org.junit.Assert.*;
import static org.apache.sis.io.X364.*;


/**
 * Tests the {@link X364} class.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @since   0.3 (derived from geotk-3.00)
 * @version 0.3
 * @module
 */
public final strictfp class X364Test extends TestCase {
    /**
     * Tests the {@link X364#plain(String)} method.
     */
    @Test
    public void testPlain() {
        String colored, plain;
        colored = "Some plain text";
        plain   = "Some plain text";
        assertEquals(plain, plain(colored));
        assertEquals(plain.length(), lengthOfPlain(colored));

        plain   = "With blue in the middle";
        colored = "With " + FOREGROUND_BLUE.sequence() +
                  "blue"  + FOREGROUND_DEFAULT.sequence() + " in the middle";
        assertEquals(plain, plain(colored));
        assertEquals(plain.length(), lengthOfPlain(colored));
    }
}
