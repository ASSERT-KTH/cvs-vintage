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

import junit.framework.TestCase;

/**
 * @author redsolo
 */
public class SemaphoreTest extends TestCase {

    /**
     * Tests the constructors.
     */
    public void testConstructors() {
        Semaphore semaphore = new Semaphore();
        assertTrue("The semaphore is not holding.", semaphore.isHolding());

        semaphore.hold();
        assertTrue("The semaphore is not holding.", semaphore.isHolding());

        semaphore.release();
        assertTrue("The semaphore has not been released", !semaphore.isHolding());

        semaphore = new Semaphore(false);
        assertTrue("The semaphore is holding.", !semaphore.isHolding());

        semaphore.hold();
        assertTrue("The semaphore is not holding.", semaphore.isHolding());

        semaphore.release();
        assertTrue("The semaphore has not been released", !semaphore.isHolding());
    }

    /**
     * Test the hold() and release() methods.
     */
    public void testHold() {
        Semaphore semaphore = new Semaphore();
        assertTrue("The semaphore is not holding.", semaphore.isHolding());

        semaphore.hold();
        assertTrue("The semaphore is not holding.", semaphore.isHolding());

        semaphore.release();
        assertTrue("The semaphore has not been released", !semaphore.isHolding());

        semaphore.hold();
        assertTrue("The semaphore is not holding.", semaphore.isHolding());
    }

    /**
     * Test a single thread wait on the semaphore.
     * @throws InterruptedException thrown for any good reason if the test failed.
     */
    public void testSingleWait() throws InterruptedException {
        Semaphore semaphore = new Semaphore();

        StopWatch timer = new StopWatch();
        semaphore.waitUntilReleased(50);
        if (timer.getTiming() < 25) {
            fail("Single thread did not wait for semaphore.");
        }

        semaphore.hold();
        timer.start();
        semaphore.waitUntilReleased(50);
        if (timer.getTiming() < 25) {
            fail("Single thread did not wait for semaphore.");
        }

        semaphore.release();
        timer.start();
        semaphore.waitUntilReleased(100);
        if (timer.getTiming() > 25) {
            fail("Single thread did wait for semaphore.");
        }
    }
}
