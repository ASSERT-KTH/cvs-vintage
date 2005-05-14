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
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Method;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.rmi.server.ObjID;
import java.rmi.server.Operation;
import java.rmi.server.RemoteCall;
import java.rmi.server.RemoteObject;
import java.rmi.server.RemoteRef;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Ref class implements the {@link RemoteRef} interface for this
 * RMI implementation. Instances of this class are passed to a {@link
 * Remote} object's {@link java.rmi.server.RemoteStub} implementation.
 * These instances then get serialized and sent to the RMI client.
 *
 * @author Rafael H. Schloming &lt;rhs@mit.edu&gt;
 **/

public class Ref implements RemoteRef, Constants {

    /**
     * A store of reference counts for remote instances referenced
     * from this JVM. This Map goes from {@link Key} -> {@link Map}.
     * The nested {@link Map} goes from {@link ObjID} -> {@link
     * Count}.
     */
    private static Map COUNTS = new HashMap();

    private static class Key extends Pair {
        public Key(String host, int port) {
            super(host, port);
        }
        public String getHost() {
            return (String) getOne();
        }
        public int getPort() {
            return ((Integer) getTwo()).intValue();
        }
    }

    private static class Count {
        private int count;
        private Count(int initial) {
            count = initial;
        }
    }

    /**
     * Returns the {@link Key}s from the COUNTS Map in a thread-safe
     * manner.
     *
     * @return a collection of the {@link Key} objects stored by
     * COUNTS
     */

    private static final Collection getKeys() {
        synchronized (COUNTS) {
            return new ArrayList(COUNTS.keySet());
        }
    }

    /**
     * Creates if necessary and returs a {@link Map} from {@link
     * ObjID} -> {@link Count} for the given host and port.
     *
     * @param host the desired host
     * @param port the desired port
     * @return reference counts for the given host, port
     */

    private static final Map getCounts(String host, int port) {
        synchronized (COUNTS) {
            Key key = new Key(host, port);
            Map result = (Map) COUNTS.get(key);
            if (result == null) {
                result = new HashMap();
                COUNTS.put(key, result);
            }
            return result;
        }
    }

    /**
     * Creates if necessary and returns the a {@link Count}) object
     * for the given host, port, and oid.
     *
     * @param host the originating host of the oid
     * @param port the port on <var>host</var>
     * @param oid the ObjID
     * @return a {@link Count} object for use in reference counting
     */

    private static final Count getCount(String host, int port, ObjID oid) {
        Map counts = getCounts(host, port);
        synchronized (counts) {
            Count count = (Count) counts.get(oid);
            if (count == null) {
                count = new Count(0);
                counts.put(oid, count);
            }
            return count;
        }
    }

    /**
     * Adjusts the reference count for a given oid.
     *
     * @param host the originating host of the oid
     * @param port the originating port of the oid
     * @param oid the oid whose count needs to be adjusted
     * @param amount the amount by which adjust the increment
     * @return the new reference count
     */

    private static final int incr(String host, int port, ObjID oid, int amount) {
        Count count = getCount(host, port, oid);
        synchronized (count) {
            count.count += amount;
            return count.count;
        }
    }

    /**
     * Increments the reference count for oid.
     *
     * @param host the originating host of the oid
     * @param port the originating port of the oid
     * @param oid the oid whose count needs to be incremented
     * @return the new reference count
     */

    private static final int incr(String host, int port, ObjID oid) {
        return incr(host, port, oid, 1);
    }

    /**
     * Decrements the reference count for oid.
     *
     * @param host the originating host of the oid
     * @param port the originating port of the oid
     * @param oid the oid whose count needs to be decremented
     * @return the new reference count
     */

    private static final int decr(String host, int port, ObjID oid) {
        return incr(host, port, oid, -1);
    }

    /**
     * The remote host this ref points to.
     */
    private String host;

    /**
     * The port on the remote host.
     */
    private int port;

    /**
     * The ObjID of the remote instance referred to by this Ref.
     */
    private ObjID oid;

    /**
     * The Interceptor used by the remote RMI implementation or null
     * if there is none.
     */
    private ClientInterceptor clint;

    /**
     * Public noargs constructor required for serialization.
     */

    public Ref() {}

    /**
     * Constructs a Ref instance.
     *
     * @param host the remote host
     * @param port the port on the remote host
     * @param oid the ObjID of the remote object
     * @param clint a ClientInterceptor or null
     */

    public Ref(String host, int port, ObjID oid, ClientInterceptor clint) {
        this.host = host;
        this.port = port;
        this.oid = oid;
        this.clint = clint;
        incr(host, port, oid);
    }

    /**
     * Returns the oid of this Ref.
     *
     * @return the oid of this Ref
     */

    public ObjID getOID() {
        return oid;
    }

    /**
     * Returns the remote host pointed to by this Ref.
     *
     * @return the remote host pointed to by this Ref
     */

    public String getHost() {
        return host;
    }

    /**
     * Returns the port on the remote host pointed to by this Ref.
     *
     * @return the port on the remote host pointed to by this Ref
     */

    public int getPort() {
        return port;
    }

    /**
     * Returns the ClientInterceptor used by the remote RMI
     * implementation.
     *
     * @return the ClientInterceptor used by the remote RMI
     * implementation
     */

    ClientInterceptor getInterceptor() {
        return clint;
    }

    /**
     * Make this Ref instance identical to the given Ref.
     *
     * @param ref the ref to duplicate
     */

    public void connect(Ref ref) {
        this.host = ref.host;
        this.port = ref.port;
        this.oid = ref.oid;
        this.clint = ref.clint;
    }

    /**
     * Casts the given object to an {@link Exception} or {@link Error}
     * and throws it.
     *
     * @param o the object to throw
     * @throws Exception this is thrown when o is an instance of
     * {@link Exception}, otherwise an {@link Error} is thrown
     */

    private void throwAny(Object o) throws Exception {
        if (o instanceof Exception) {
            throw (Exception) o;
        } else if (o instanceof Error) {
            throw (Error) o;
        } else {
            throw new RuntimeException("" + o);
        }
    }

    public Object invoke(Remote rem, Method meth, Object[] args, long hash)
        throws Exception {
        return invoke(meth, -1, hash, args);
    }

    /**
     * Invoke the specified method or operation on the given
     * arguments. This method delegates to {@link Server#invoke(Ref,
     * Method, int, long, Object[]) and decodes the result.
     *
     * @param meth the method to invoke or null for v1.1 stub
     * @param opnum the opnum or -1 for v1.2 stubs
     * @param hash the method hash for v1.2 stubs and the interface
     * hash for v1.1 stubs
     * @param args the arguments for the remote method call
     * @return the result of the remote method call
     * @throws Exception any exceptional result returned by the remote
     * method call
     */

    private Object invoke(Method meth, int opnum, long hash, Object[] args)
        throws Exception {
        Server.Result result = Server.invoke(this, meth, opnum, hash, args);

        switch (result.code) {
        case METHOD_RESULT:
            return result.value;
        case METHOD_ERROR:
            if (result.value instanceof RemoteException) {
                throw new ServerException
                    ("RemoteException thrown during remote method invocation",
                     (Exception) result.value);
            } else {
                throwAny(result.value);
            }
            break;
        case SYSTEM_ERROR:
            throwAny(result.value);
            /*Throwable srv = (Throwable) result.value;
            if (srv == null) {
                throw new RuntimeException("unknown system error");
            }
            Throwable err = (Throwable) srv.getClass().newInstance();
            err.initCause(srv);
            throwAny(err);*/
            break;
        }

        throw new IllegalStateException("unreachable");
    }

    /**
     * Returns a new {@link RemoteCall} object. This is only used for
     * v1.1 stubs.
     *
     * @param obj the stub
     * @param ops the operations contained by the stub
     * @param opnum the opnum to be invoked
     * @param hash the interface hash for this remote object
     * @return the RemoteCall implementation for use by the v1.1. stub
     */

    public RemoteCall newCall(RemoteObject obj, Operation[] ops, int opnum, long hash) {
        return new Call(opnum, hash);
    }

    /**
     * Delegates to {@link RemoteCall#executeCall()}.
     *
     * @param call the RemoteCall obtained from invoking {@link
     * #newCall(RemoteObject, Operation[], int, long)}
     */

    public void invoke(RemoteCall call) throws Exception {
        call.executeCall();
    }

    /**
     * Delegates to {@link RemoteCall}#done().
     *
     * @param call the RemoteCall obtained from invoking {@link
     * #newCall(RemoteObject, Operation[], int, long)}
     */

    public void done(RemoteCall call) {
        try {
            call.done();
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    /**
     * Returns null. This is important for indicating to the containg
     * {@link RemoteObject} implementation that this Ref should be
     * serialized and sent directly to the client rather than being
     * replaced with a {@link RemoteRef} implementation from the
     * client's RMI system.
     *
     * @param out this is ignored
     */

    public String getRefClass(ObjectOutput out) {
        return null;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(host);
        out.writeInt(port);
        oid.write(out);
        out.writeObject(clint);
    }

    public void readExternal(ObjectInput in)
        throws IOException, ClassNotFoundException {
        host = in.readUTF();
        port = in.readInt();
        oid = ObjID.read(in);
        clint = (ClientInterceptor) in.readObject();
        incr(host, port, oid);
    }

    public void finalize() {
        decr(host, port, oid);
    }

    public boolean remoteEquals(RemoteRef obj) {
        Ref ref = (Ref) obj;
        return host.equals(ref.host)
            && port == ref.port
            && oid.equals(ref.oid);
    }

    public int remoteHashCode() {
        // This method for combining hashes is taken from the
        // specifications for List implementations found at the
        // following url:
        //   http://java.sun.com/j2se/1.4.2/docs/api/java/util/List.html#hashCode()
        int hash = 1;
        hash = hash*31 + host.hashCode();
        hash = hash*31 + port;
        hash = hash*31 + oid.hashCode();
        return hash;
    }

    public String remoteToString() {
        return "<remote reference for " + host + ":" + port + ":" + oid + ">";
    }

    /**
     * This class is the {@link RemoteCall} implementation for this
     * RMI implementation and exists solely for the purpose of
     * compatibility with v1.1 stubs. This implementation uses {@link
     * ObjectOutputList} and {@link ObjectInputList} to gather the
     * arguments from the v1.1 stubs and delegate back to {@link
     * #invoke(Method, int, long, Object[])} for the actual call.
     */

    private class Call implements RemoteCall {

        private int opnum;
        private long hash;
        private List args;
        private ObjectOutput out;
        private List result;
        private ObjectInput in;

        public Call(int opnum, long hash) {
            this.opnum = opnum;
            this.hash = hash;
            args = new ArrayList();
            out = new ObjectOutputList(args);
            result = new ArrayList();
            in = new ObjectInputList(result);
        }

        public void executeCall() throws Exception {
            result.add(invoke(null, opnum, hash, args.toArray()));
        }

        public void done() {}

        public ObjectOutput getOutputStream() {
            return out;
        }

        public void releaseOutputStream() {}

        public ObjectInput getInputStream() {
            return in;
        }

        public void releaseInputStream() {}

        public ObjectOutput getResultStream(boolean success) {
            throw new UnsupportedOperationException();
        }

    }

    static {
        new Pinger(5*60*1000).start();
    }

    /**
     * This thread is used by the distributed garbage collection
     * system to keep objects alive on the server when they have no
     * local references.
     */

    private static class Pinger extends Thread {
        private long interval;
        private Pinger(long interval) {
            this.interval = interval;
            setDaemon(true);
        }
        public void run() {
            while (true) {
                try {
                    sleep(interval);
                } catch (InterruptedException e) {
                    break;
                }
                Collection keys = getKeys();
                for (Iterator it = keys.iterator(); it.hasNext(); ) {
                    Key key = (Key) it.next();
                    Map counts = getCounts(key.getHost(), key.getPort());
                    List oids = new ArrayList(counts.size());
                    synchronized (counts) {
                        Set entries = counts.entrySet();
                        for (Iterator iter = entries.iterator();
                             iter.hasNext(); ) {
                            Map.Entry me = (Map.Entry) iter.next();
                            ObjID oid = (ObjID) me.getKey();
                            Count count = (Count) me.getValue();
                            if (count.count <= 0) {
                                iter.remove();
                            } else {
                                oids.add(oid);
                            }
                        }
                    }
                    try {
                        Server.ping(key.getHost(), key.getPort(), oids);
                    } catch (IOException e) {
                        // Because Ref objects are primarily used
                        // inside stubs, this code will most likely be
                        // running inside the RMI client. Because of
                        // this we have no way of knowing if a logging
                        // system has been configured or not. It may
                        // be that some logging systems will when not
                        // properly configured default to stderr, but
                        // in my (possibly outdated) experience with
                        // log4j, this was not the case, so just to be
                        // safe I am sending these error messages to
                        // stderr instead of using a logging system
                        // here.
                        System.err.println("ERROR WHILE PINGING SERVER");
                        e.printStackTrace(System.err);
                    }
                }
            }
        }
    }

}
