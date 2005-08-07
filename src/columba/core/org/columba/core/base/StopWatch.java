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
package org.columba.core.base;


/**
 * Simple StopWatch class for timing stuff.
 * <p>
 * Usage to measure an operation:
 * <code>
 * StopWatch timer = new StopWatch();
 * <Time consuming operation>
 * System.out.println( timer ); // outputs the time between the creation of Timer and "now"
 * </code>
 * <p>
 * Usage to use the StopWatch in different methods and classes:
 * <code>
 * public void test()
 * {
 *   StopWatch.instance().start();
 *   <time consuming operation>
 *   doOther();
 * }
 * public void doOther()
 * {
 *   <time consuming operation>
 *   System.out.println( StopWatch.instance() ); // outputs the time between the instance().start() and "now"
 * }
 * </code>
 *
 * @author  redsolo
 */
public class StopWatch {
    private static StopWatch instance = null;
    private long startTime = 0;
    private long stopTime = -1;

    /**
 * Creates a new instance of StopWatch
 * Starts the timing from the time the object was created
 */
    public StopWatch() {
        start();
    }

    /**
 * Returns a StopWatch instance. This can be used to measure the time between different methods/classes.
 *
 * @return a static StopWatch instance
 */
    public static StopWatch instance() {
        if (instance == null) {
            instance = new StopWatch();
        }

        return instance;
    }

    /**
 * Starts the watch.
 * Resets the start time and resets the stop time as well.
 */
    public final void start() {
        startTime = System.currentTimeMillis();
        stopTime = -1;
    }

    /**
 * Stops the watch.
 * @return the time passed since the StopWatch was started.
 */
    public final long stop() {
        stopTime = System.currentTimeMillis();

        return (stopTime - startTime);
    }

    /**
 * Gets the time (ms) elapsed from the start() method was run until now OR the stop() method was run.
 * If stop() is executed then this method will return the same all the time, BUT if the stop() hasnt
 * been executed this method returns the time (ms) elapsed from the latest start()
 * @return the time since the StopWatch was started; or if it has been stopped, the time between start() and stop()
 */
    public long getTiming() {
        long time;

        if (stopTime == -1) {
            time = (System.currentTimeMillis() - startTime);
        } else {
            time = (stopTime - startTime);
        }

        return time;
    }

    /**
 * Returns the time elapsed from the start() until now, OR until stop() was executed
 * @return the time (ms) as a string
 */
    public String toString() {
        return String.valueOf(getTiming()) + " ms";
    }
}
