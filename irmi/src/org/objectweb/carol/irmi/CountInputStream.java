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
import java.io.InputStream;

/**
 * CountInputStream is a simple instrumented {@link InputStream}
 * implementation that delegates all method calls to a provided {@link
 * InputStream} while timing all method calls and counting how many
 * bytes are read by this {@link InputStream}.
 *
 * @author Rafael H. Schloming &lt;rhs@mit.edu&gt;
 **/

public class CountInputStream extends InputStream {

    /**
     * Times all the method calls made on this object.
     */
    private static final Timer INPUT = new Timer("INPUT") {
        public String toString() {
            return super.toString() + " (" + count + " bytes)";
        }
    };

    /**
     * Tracks how many bytes have been read.
     */
    static int count = 0;

    /**
     * Increments the count of bytes read by the given value.
     *
     * @param n the amount by which to increment
     */

    private static synchronized int incr(int n) {
        if (n != -1) {
            count += n;
        }
        return n;
    }

    /**
     * The delegate {@link InputStream}.
     */
    private InputStream in;

    /**
     * Constructs a new CountInputStream that delegates to the given
     * {@link InputStream}.
     *
     * @param in the delegate {@link InputStream}.
     */
    public CountInputStream(InputStream in) {
        this.in = in;
    }

    public int available() throws IOException {
        INPUT.start();
        try {
            return in.available();
        } finally {
            INPUT.stop();
        }
    }

    public void close() throws IOException {
        INPUT.start();
        try {
            in.close();
        } finally {
            INPUT.stop();
        }
    }

    public void mark(int readlimit) {
        INPUT.start();
        try {
            in.mark(readlimit);
        } finally {
            INPUT.stop();
        }
    }

    public boolean markSupported() {
        INPUT.start();
        try {
            return in.markSupported();
        } finally {
            INPUT.stop();
        }
    }

    public int read() throws IOException {
        INPUT.start();
        try {
            int b = in.read();
            if (b != -1) { incr(1); }
            return b;
        } finally {
            INPUT.stop();
        }
    }

    public int read(byte[] b) throws IOException {
        INPUT.start();
        try {
            return incr(in.read(b));
        } finally {
            INPUT.stop();
        }
    }

    public int read(byte[] b, int off, int len) throws IOException {
        INPUT.start();
        try {
            return incr(in.read(b, off, len));
        } finally {
            INPUT.stop();
        }
    }

    public void reset() throws IOException {
        INPUT.start();
        try {
            in.reset();
        } finally {
            INPUT.stop();
        }
    }

    public long skip(long n) throws IOException {
        INPUT.start();
        try {
            return in.skip(n);
        } finally {
            INPUT.stop();
        }
    }

}
