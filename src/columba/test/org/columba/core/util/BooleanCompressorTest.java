//The contents of this file are subject to the Mozilla Public License Version 1.1
//(the "License"); you may not use this file except in compliance with the 
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License 
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.
package org.columba.core.util;

import org.columba.core.base.BooleanCompressor;

import junit.framework.TestCase;


public class BooleanCompressorTest extends TestCase {
    private Boolean[] test = {
        Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, Boolean.TRUE
    };

    /**
 * Constructor for BooleanCompressorTest.
 * @param arg0
 */
    public BooleanCompressorTest(String arg0) {
        super(arg0);
    }

    public void test() {
        int testInt = BooleanCompressor.compress(test);

        assertTrue(testInt == 25);

        Boolean[] result = new Boolean[test.length];

        for (int i = 0; i < test.length; i++) {
            result[i] = BooleanCompressor.decompress(testInt, i);
            assertTrue(result[i].equals(test[i]));
        }
    }
}
