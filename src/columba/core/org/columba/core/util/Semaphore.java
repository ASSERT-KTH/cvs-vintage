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

/**
 * A Semaphore class to be used by multiple threads that needs to be notified when a
 * thread has released this semaphore. Depending threads uses one of the
 * waitUntilReleased() method to wait for the semaphore, then when the Thread that
 * owns the Semaphore (called the hold() method) calls the release method all other
 * threads are awaken.
 *
 * All threads that invoke waitUntilReleased() waits until a thread invokes the
 * release()
 *
 * @author erma
 */
public final class Semaphore {

    private Object lockObject = new Object();

    private boolean isReleased = false;

    /**
     * Creates a new instance of Semaphore
     */
    public Semaphore() {
        this(true);
    }

    /**
     * Creates a new instance of Semaphore
     *
     * @param isHolding if the semaphore should hold all threads from the begining or
     *            not.
     */
    public Semaphore(boolean isHolding) {
        if (isHolding) {
            hold();
        } else {
            release();
        }
    }

    /**
     * Holds this semaphores. Threads that call the waitUntilReleased stops until the
     * release() method is called.
     *
     * @see #release()
     */
    public void hold() {
        isReleased = false;
    }

    /**
     * Waits the current thread until another thread invokes the relase() or
     * continues if another thread already has invoked the release(). This method
     * behaves exactly as if it simply performs the call waitUntilReleased(0).
     *
     * @throws InterruptedException if another thread has interrupted the current
     *             thread.
     */
    public void waitUntilReleased() throws InterruptedException {
        waitUntilReleased(0);
    }

    /**
     * Waits the current thread until another thread invokes the relase() or
     * continues if another thread already has invoked the release().
     *
     * @param timeout the maximum time to wait in milliseconds.
     * @throws InterruptedException if another thread has interrupted the current
     *             thread.
     */
    public void waitUntilReleased(long timeout) throws InterruptedException {
        if (!isReleased) {
            synchronized (lockObject) {
                lockObject.wait(timeout);
            }
        }
    }

    /**
     * Release all threads that has invoked the waitUntilReleased().
     */
    public void release() {
        isReleased = true;
        synchronized (lockObject) {
            lockObject.notifyAll();
        }
    }

    /**
     * Returns if the semaphore holds all threads that invoke the waitUntilReleased()
     * methods.
     *
     * @return true or false
     */
    public boolean isHolding() {
        return !isReleased;
    }
}
