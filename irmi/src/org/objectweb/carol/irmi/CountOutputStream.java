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

import java.io.IOException;
import java.io.OutputStream;

/**
 * CountOutputStream is a simple instrumented {@link OutputStream}
 * implementation that delegates all method calls to a provided {@link
 * OutputStream} while timing all method calls and counting how many
 * bytes are written by this {@link OutputStream}.
 *
 * @author Rafael H. Schloming &lt;rhs@mit.edu&gt;
 **/

public class CountOutputStream extends OutputStream {

    /**
     * Times all the method calls made on this object.
     */
    private static final Timer OUTPUT = new Timer("OUTPUT") {
        public String toString() {
            return super.toString() + " (" + count + " bytes)";
        }
    };

    /**
     * Tracks the number of bytes written.
     */
    static int count = 0;

    /**
     * Increments the count of bytes written by the given value.
     *
     * @param n the amount by which to increment
     */

    private static synchronized void incr(int n) {
        count += n;
    }

    /**
     * The delegate {@link OutputStream}.
     */
    private OutputStream out;

    /**
     * Constructs a new CountOutputStream that delegates to the given
     * {@link OutputStream}.
     *
     * @param os the delegate {@link OutputStream}
     */

    public CountOutputStream(OutputStream os) {
        this.out = os;
    }

    public void close() throws IOException {
        OUTPUT.start();
        try {
            out.close();
        } finally {
            OUTPUT.stop();
        }
    }

    public void flush() throws IOException {
        OUTPUT.start();
        try {
            out.flush();
        } finally {
            OUTPUT.stop();
        }
    }

    public void write(byte[] b) throws IOException {
        OUTPUT.start();
        try {
            out.write(b);
        } finally {
            OUTPUT.stop();
        }
        incr(b.length);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        OUTPUT.start();
        try {
            out.write(b, off, len);
        } finally {
            OUTPUT.stop();
        }
        incr(len);
    }

    public void write(int b) throws IOException {
        OUTPUT.start();
        try {
            out.write(b);
        } finally {
            OUTPUT.stop();
        }
        incr(1);
    }

}
