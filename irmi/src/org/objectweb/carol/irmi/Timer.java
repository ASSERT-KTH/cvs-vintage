/**
 * Copyright (c) 2004 Red Hat, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * Component of: Red Hat Application Server
 *
 * Initial Developers: Rafael H. Schloming
 */
package org.objectweb.carol.irmi;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>The Timer class is a utility used to gather timing information
 * about specific operations within a system. It is intended to be
 * lightweight enough that it can be used to gather timing information
 * in a production environment without adversly impacting the
 * performance of the system. It can be used as follows:</p>
 *
 * <code><pre>
 *     private static final Timer OPERATION = new Timer();
 *
 *     private void doOperation() {
 *         OPERATION.start();
 *         try {
 *             ...
 *             // do the operation
 *             ...
 *         } finally {
 *             OPERATION.stop();
 *         }
 *     }
 * </pre></code>
 *
 * <p>In order for the timing information gathered by an instance of
 * this class to be accurate it is necessary that every call to {@link
 * #start()} has a corresponding call to {@link #stop()}. Because of
 * this it is good practice to put the call to {@link #stop()} in a
 * finally block as depicted above. This will guarantee proper timing
 * information even in the event of unanticipated exceptions.</p>
 *
 * <p>Timer instances track ellapsed time using ThreadLocal storage.
 * This makes it safe for multiple threads to use a single Timer
 * instance to accumulate time. It is important to note that Timer
 * instances track ellapsed real time, not ellapsed CPU time, so if
 * you have 10 threads running simultaneously it is possible for a
 * Timer instance to accumulate 100 seconds even if your program exits
 * after 10 seconds.</p>
 *
 * <p>Any paired calls to {@link #start()}/{@link #stop()} that occur
 * inside an enclosing {@link #start()}/{@link #stop()} pair for a
 * given timer instance are not double counted, i.e. it is as if only
 * the outermost {@link #start()}/{@link #stop()} pair were invoked.
 * This behavior allows a Timer instance to be used in a reentrant
 * manner either inside the body of a recursive function, or across
 * multiple methods without worrying about double counting when one
 * timed method invokes another timed method.</p>
 *
 * <p>If the timer.out system property is specified then at JVM exit
 * the time accumulated by each Timer instance will be printed to the
 * location specified by timer.out. The value of the timer.out system
 * property must be a filename. If the value refers to a file that
 * already exists, the timing information will be appended to the
 * contents of the file, otherwise the file will be created. If "-" is
 * specified for timer.out then the timing results will be displayed
 * on standard error.</p>
 *
 * <p>The results are displayed by printing for each timer instance:
 * the name of that timer; accumulated time; and number of
 * start()/stop() pairs. The result for each Timer instance is printed
 * on a separate line with the following format.</p>
 *
 * <code><pre>
 *   &lt;NAME&gt;: &lt;millis&gt;/&lt;count&gt;
 * </pre></code>
 *
 * <p>The default {@link Timer#toString()} displays the accumulated
 * milliseconds and {@link #start()}/{@link #stop()} count. This
 * output can be customized by overriding the {@link #toString()}
 * method of the Timer instance on construction. This can be useful
 * for including additional information gathered by your program. For
 * example if you are timing I/O and you wish to track how many bytes
 * are written by your program in addition to the standard timing
 * information then you could use a Timer instance as follows:</p>
 *
 * <code><pre>
 *     private static int bytes = 0;
 *     private static final Timer OUTPUT = new Timer("OUTPUT") {
 *         public String toString() {
 *             return super.toString() + " (" + bytes + " bytes)";
 *         }
 *     }
 * </pre></code>
 *
 * <p>The code described above will result in the output similar to
 * the following upon JVM termination when the timer.out system
 * property is set:</p>
 *
 * <code><pre>
 *   OUTPUT: 11231/100 (1024861 bytes)
 * </pre></code>
 *
 * @author Rafael H. Schloming &lt;rhs@mit.edu&gt;
 **/

public class Timer {

    /**
     * Tracks all Timer instances for display on JVM exit.
     */
    private static final List TIMERS = new ArrayList();

    static {
        final String name = System.getProperty("timer.out");
        if (name != null && !name.equals("")) {
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    try {
                        dump(name);
                    } catch (IOException e) {
                        throw new Error(e);
                    }
                }
            });
        }
    }

    /**
     * Writes timer instances to the designated location.
     *
     * @param name the filename to append to/create or "-" for stderr
     * @throws IOException if there is an error outputting the timers
     */

    private static synchronized void dump(String name) throws IOException {
        PrintWriter w;
        boolean close = true;
        if (name.equals("-")) {
            w = new PrintWriter(new OutputStreamWriter(System.err));
            close = false;
        } else {
            w = new PrintWriter(new FileWriter(name, true));
            w.println("--------------------");
        }
        for (int i = 0; i < TIMERS.size(); i++) {
            Timer t = (Timer) TIMERS.get(i);
            if (t.getCount() > 0) {
                w.println(t.getName() + ": " + t);
            }
        }
        w.flush();
        if (close) { w.close(); }
    }

    /**
     * Class used for ThreadLocal storage.
     */
    private static class Slot {
        /**
         * The time that start() was called in the current thread.
         */
        private long start;

        /**
         * This variable is incremented everytime a start() is
         * encountered and decremented everytime a stop() is
         * encountered. It is used to ignore nested start()/stop()
         * pairs.
         */
        private int level;
    }

    /**
     * Stores the name of this Timer instance.
     */
    private String name;

    /**
     * A ThreadLocal slot for storing timing information.
     */
    private ThreadLocal start = new ThreadLocal() {
        protected Object initialValue() {
            Slot result = new Slot();
            result.level = 0;
            return result;
        }
    };

    /**
     * The accumulated ellapsed time.
     */
    private long total = 0;

    /**
     * The number of start()/stop() pairs (not including nested ones).
     */
    private int count = 0;

    /**
     * Constructs a new Timer instance with the given unique name.
     *
     * @param name a globally unique name used to distinguish the
     * contents this timer in the timing output
     */

    public Timer(String name) {
        this.name = name;
        synchronized (Timer.class) {
            TIMERS.add(this);
        }
    }

    /**
     * Returns the name of this Timer.
     *
     * @return the Timer name
     */

    public String getName() {
        return name;
    }

    /**
     * Returns the ThreadLocal slot.
     *
     * @return the ThreadLocal slot
     */

    private Slot getSlot() {
        return (Slot) start.get();
    }

    /**
     * Start timing the current thread.
     */

    public void start() {
        Slot slot = getSlot();
        slot.level++;
        if (slot.level == 1) {
            slot.start = System.currentTimeMillis();
        }
    }

    /**
     * Stop timing the current thread and add the elapsed time to the
     * total count stored by this Timer instance.
     */

    public void stop() {
        long stop = System.currentTimeMillis();
        Slot slot = getSlot();
        if (slot.level == 1) {
            long delta = stop - slot.start;
            synchronized (this) {
                total += delta;
                count++;
            }
        } else if (slot.level < 1) {
            throw new Error("no matching start");
        }
        slot.level--;
    }

    /**
     * Returns the total time accumulated by this Timer.
     *
     * @return the total time accumulated by this Timer
     */

    public long getTotal() {
        return total;
    }

    /**
     * Returns the number of {@link #start()}/{@link #stop()} pairs
     * accumulated by this Timer instance, not including nested {@link
     * #start()}/{@link #stop()} pairs.
     *
     * @return the number of {@link #start()}/{@link #stop()} pairs
     */

    public int getCount() {
        return count;
    }

    /**
     * Returns a String depicting the total number of milliseconds
     * accumulated by this Timer and the total number of {@link
     * #start()}/{@link #stop()} pairs separated by a "/".
     *
     * @return "total/count"
     */

    public String toString() {
        return total + "/" + count;
    }

}
