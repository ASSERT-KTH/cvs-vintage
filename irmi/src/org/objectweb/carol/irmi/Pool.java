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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMISocketFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The Pool class provides a simple implementation for pooling network
 * {@link Socket}s and associated {@link RMIObjectInputStream} and
 * {@link RMIObjectOutputStream} instances. Every Pool instance has a
 * an associated reaper thread that will periodically close idle
 * connections.
 *
 * @author Rafael H. Schloming &lt;rhs@mit.edu&gt;
 **/

class Pool {

    /**
     * This variable stores how long in milliseconds the reaper thread
     * will sleep between checks for idle connections.
     */
    private long interval;

    /**
     * This variable stores how long in milliseconds a connection must
     * be idle for the reaper thread to close it.
     */
    private long timeout;

    /**
     * The pool entries. This map is keyed by a (host, port) pair and
     * maps to an {@link Entry}.
     */
    private Map entries = new HashMap();

    /**
     * Constructs a new Pool with the given interval and timeout.
     *
     * @param interval the time in milliseconds between checks for idle connections
     * @param timeout the time in milliseconds that a connection must
     * be idle in order to be closed
     */

    Pool(long interval, long timeout) {
        this.interval = interval;
        this.timeout = timeout;
        new Reaper().start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() { shutdown(); }
        });
    }

    /**
     * Constructs a new Pool with an interval and timeout of 30
     * seconds.
     */

    Pool() {
        this(30*1000, 30*1000);
    }

    /**
     * Uses the RMISocketFactory API to connect to the given host and
     * port. Also call {@link Socket#setTcpNoDelay(boolean)}, and
     * {@link Socket#setKeepAlive(boolean)} passing true to both
     * methods.
     *
     * @param host the host to connect to
     * @param port the port to connect to
     * @throws IOException when there is an I/O error
     */

    private Socket connect(String host, int port) throws IOException {
        RMIClientSocketFactory csf = RMISocketFactory.getSocketFactory();
        if (csf == null) {
            csf = RMISocketFactory.getDefaultSocketFactory();
        }
        Socket sock = csf.createSocket(host, port);
        sock.setTcpNoDelay(true);
        sock.setKeepAlive(true);
        return sock;
    }

    /**
     * Obtain an {@link Entry} for the host and port pointed to by the
     * given {@link Ref} object. Entries obtained by this method
     * should be returned to the pool using the {@link
     * #release(Entry)} method or closed using the {@link
     * #close(Entry} method.
     *
     * @param ref a Ref referring to the desired host and port
     * @return an {@link Entry} connecting to the desired host and
     * port
     * @throws IOException if there is an I/O error
     * @see #acquire(String, int)
     */

    public Entry acquire(Ref ref) throws IOException {
        return acquire(ref.getHost(), ref.getPort());
    }

    /**
     * Obtain an {@link Entry} for the specified host and port.
     * Entries obtained by this method should be returned to the pool
     * using the {@link #release(Entry)} method or closed using the
     * {@link #close(Entry} method.
     *
     * @param host the host to which the {@link Entry} should point
     * @param port the port to which the {@link Entry} should point
     * @return an {@link Entry} pointing to the specified host and
     * port
     * @throws IOException if there is an I/O error
     */

    public Entry acquire(String host, int port) throws IOException {
        synchronized (entries) {
            Object key = key(host, port);
            Entry result = (Entry) entries.remove(key);
            if (result == null) {
                result = new Entry(key, connect(host, port));
            }
            return result;
        }
    }

    /**
     * Returns a key object for the given host, port.
     *
     * @param host the host
     * @param port the port
     * @return a new Pair(host, port)
     */

    private Object key(String host, int port) {
        return new Pair(host, port);
    }

    /**
     * Returns a valid Entry back to the Pool for later reuse. Entries
     * which may be in an indeterminate state due to unrecoverable
     * protocol errors should not be returned to the pool with this
     * method, but closed using the {@link #close(Entry)} method.
     *
     * @param entry the {@link Entry} to return to the pool
     */

    public void release(Entry entry) throws IOException {
        Entry old;
        synchronized (entries) {
            old = (Entry) entries.remove(entry.key);
            entries.put(entry.key, entry);
            entry.used = System.currentTimeMillis();
        }
        if (old != null) { old.close(); }
    }

    /**
     * Closes the connection and frees any resources associated with
     * the the given {@link Entry}.
     *
     * @param entry the {@link Entry} to close
     */

    public void close(Entry entry) {
        entry.close();
    }

    /**
     * Closes all connections held open by this Pool instance.
     */

    public void shutdown() {
        synchronized (entries) {
            for (Iterator it = entries.values().iterator(); it.hasNext(); ) {
                Entry entry = (Entry) it.next();
                entry.close();
                it.remove();
            }
        }
    }

    /**
     * The Entry class stores a single network {@link Socket} and the
     * associated {@link RMIObjectInputStream} and {@link
     * RMIObjectOutputStream} used for client/server communication by
     * this RMI implementation.
     */

    class Entry {

        /**
         * The key under which this entry is stored.
         */
        private Object key;

        /**
         * The network {@link Socket}
         */
        private Socket sock;

        /**
         * The {@link RMIObjectInputStream} attached to
         * sock.getInputStream().
         */
        private ObjectInputStream in;

        /**
         * The {@link RMIObjectOutputStream} attached to
         * sock.getOutputStream().
         */
        private ObjectOutputStream out;

        /**
         * Stores the last used timestamp for this Entry.
         */
        private long used;

        /**
         * Constructs a new Entry with the given key and network
         * {@link Socket}.
         *
         * @param key the key under which this entry will be stored
         * @param sock the network {@link Socket} pooled by this entry
         */
        private Entry(Object key, Socket sock) throws IOException {
            this.key = key;
            this.sock = sock;
            out = new RMIObjectOutputStream(sock.getOutputStream());
            in = new RMIObjectInputStream(sock.getInputStream());
            used = System.currentTimeMillis();
        }

        /**
         * Returns the ObjectInputStream pooled by this Entry.
         *
         * @return the ObjectInputStream pooled by this Entry
         */

        public ObjectInputStream getIn() {
            return in;
        }

        /**
         * Returns the ObjectOutputStream pooled by this Entry.
         *
         * @return the ObjectOutputStream pooled by this Entry
         */

        public ObjectOutputStream getOut() {
            return out;
        }

        /**
         * Returns the network {@link Socket} pooled by this Entry.
         *
         * @return the network {@link Socket} pooled by this Entry.
         */

        public Socket getSocket() {
            return sock;
        }

        /**
         * Closes the network {@link Socket} pooled by this Entry.
         */

        private void close() {
            try {
                sock.close();
            } catch (IOException e) {
                // do nothing
            }
        }

        /**
         * Returns true if the connection pooled by this Entry is
         * ready to be reaped. This is determined by comparing the
         * time ellapsed since this Entry was last used to the timeout
         * for this pool.
         *
         * @return true if the connection is ready to be reaped
         */

        private boolean isExpired() {
            return System.currentTimeMillis() - used > timeout;
        }
    }

    /**
     * Reaper thread for closing idle connections.
     */

    class Reaper extends Thread {
        Reaper() {
            setDaemon(true);
            setName("IRMI Connection Reaper");
        }

        public void run() {
            while (true) {
                try {
                    sleep(interval);
                } catch (InterruptedException e) {
                    break;
                }

                List values;
                synchronized (entries) {
                    values = new ArrayList(entries.values());
                }
                for (Iterator it = values.iterator(); it.hasNext(); ) {
                    Entry entry = (Entry) it.next();
                    if (entry.isExpired()) {
                        Entry expired = null;
                        synchronized (entries) {
                            Entry e = (Entry) entries.get(entry.key);
                            if (e.isExpired()) {
                                entries.remove(e.key);
                                expired = e;
                            }
                        }

                        if (expired != null) {
                            expired.close();
                        }
                    }
                }
            }
        }
    }

}
