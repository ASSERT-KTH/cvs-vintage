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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.ExportException;
import java.rmi.server.ObjID;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RMISocketFactory;
import java.rmi.server.RemoteCall;
import java.rmi.server.RemoteRef;
import java.rmi.server.RemoteStub;
import java.rmi.server.Skeleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections.map.AbstractReferenceMap;
import org.apache.commons.collections.map.ReferenceIdentityMap;

/**
 * The Server class manages exported {@link Remote} objects and
 * listens for connections from remote references. It also contains
 * static methods that the {@link Ref} class uses to make remote
 * invocations.
 *
 * @author Rafael H. Schloming &lt;rhs@mit.edu&gt;
 **/

public class Server extends Thread implements Constants {

    // I don't think transient actually means anything for static
    // variables, but I put it in just in case.
    /**
     * A pool of network connections and their associated I/O classes.
     * This is for use by the static invoke methods used by the {@link
     * Ref} class.
     */
    private static transient Pool POOL = null;

    /**
     * Creates if necessary and returns the connection Pool for this
     * JVM.
     *
     * @return a connection pool
     */

    private static synchronized Pool getPool() {
        if (POOL == null) {
            POOL = new Pool();
        }
        return POOL;
    }

    /**
     * Contains a list of all Server objects created in this JVM. This
     * is used to find stubs for {@link Remote} objects and vice
     * versa. This should probably be replaced with a more explicit
     * mapping at some point.
     */
    private static final List servers =
        Collections.synchronizedList(new ArrayList());

    /**
     * Returns all the Server instances for this JVM.
     *
     * @return all the Server instances for this JVM
     */

    public static Collection getServers() {
        return servers;
    }

    /**
     * Signature for the Stub constructor.
     */
    private static final Class[] STUB_PARAMS = new Class[] { RemoteRef.class };

    /**
     * The port to be used by this Server or zero if the Server should
     * choose any available port.
     */
    private int port;

    /**
     * The client Interceptor used by this Server.
     */
    private ClientInterceptor clint;

    /**
     * The server Interceptor used by this Server.
     */
    private Interceptor srvint;

    /**
     * The {@link ServerSocket} on which this Server listens.
     */
    private ServerSocket ssock = null;

    /**
     * A monitor used to synchronize Server startup.
     */
    private final Object monitor = new Object();

    /**
     * A {@link Map} from {@link Remote} instance -> {@link ObjID}.
     */
    private Map oids = new ReferenceIdentityMap
        (AbstractReferenceMap.WEAK, AbstractReferenceMap.HARD);

    /**
     * A {@link Map} from {@link ObjID} -> Entry.
     */
    private Map entries = new HashMap();

    /**
     * The RMIServerSocketFactory used by this Server.
     */
    private RMIServerSocketFactory ssf;

    /**
     * The reaper thread for distributed garbage collection.
     */
    private Thread reaper = new Reaper();

    /**
     * There is one instance of the Entry class for each exported
     * object.
     */

    private class Entry {

        /**
         * Hard reference to the exported object. This variable gets
         * set to null by the reaper thread when the timestamp of the
         * most recent DGC ping exceeds the timeout of the reaper
         * thread.
         */
        private Remote hard;

        /**
         * Weak reference to the exported object.
         */
        private WeakReference weak;

        /**
         * The {@link ClassLoader} to use when deserializing arguments
         * for remote method calls on the exported object.
         */
        private ClassLoader loader;

        /**
         * The timestamp of the most recent DGC ping.
         */
        private long timestamp;

        /**
         * Constructs a new Entry object.
         *
         * @param object the exported object
         * @param loader the {@link ClassLoader} to use when
         * deserializing arguments for remote method calls on the
         * exported object
         * @param timestamp the initial timestamp to use for the DGC system
         */

        public Entry(Remote object, ClassLoader loader, long timestamp) {
            this.hard = object;
            this.weak = new WeakReference(object);
            this.loader = loader;
            this.timestamp = timestamp;
        }

        /**
         * Makes this entry retain a hard reference to the exported
         * object.
         */

        public void makeHard() {
            hard = getObject();
        }

        /**
         * Makes this entry null out all hard references to the
         * exported object and retain weak references only.
         */

        public void makeWeak() {
            hard = null;
        }

        /**
         * Returns the exported object.
         *
         * @return the exported object
         */

        public Remote getObject() {
            return (Remote) weak.get();
        }

        /**
         * Returns the ClassLoader to use when deserializing arguments
         * for method calls on the exported object.
         *
         * @return the ClassLoader to use when deserializing arguments
         * for method calls on the exported object
         */

        public ClassLoader getLoader() {
            return loader;
        }

        /**
         * Returns the timestamp of the last DGC ping.
         *
         * @return the timestamp of the last DGC ping
         */

        public synchronized long getTimestamp() {
            return timestamp;
        }

        /**
         * Sets the timestamp of the last DGC ping to the given value.
         *
         * @param time the new timestamp value
         */

        public synchronized void setTimestamp(long time) {
            timestamp = time;
        }
    }

    /**
     * Construcst a Server instance with the specified port, client,
     * and server {@link Interceptor}s. If port is zero then any
     * available port will be chosen.
     *
     * @param port the port for the server to bind to
     * @param clint the client {@link Interceptor} or null
     * @param srvint the server {@link Interceptor} or null
     */

    public Server(int port, ClientInterceptor clint, Interceptor srvint) {
        this.port = port;
        this.clint = clint;
        this.srvint = srvint;
        ssf = RMISocketFactory.getSocketFactory();
        if (ssf == null) {
            ssf = RMISocketFactory.getDefaultSocketFactory();
        }
        servers.add(this);
        reaper.start();
    }

    /**
     * Constructs a Server instance that will bind to any available
     * port and use the given client and server {@link Interceptor}s.
     *
     * @param clint the client {@link Interceptor}
     * @param srvint the server {@link Interceptor}
     */

    public Server(ClientInterceptor clint, Interceptor srvint) {
        this(0, clint, srvint);
    }

    /**
     * Constructs a Server instance that will bind to any available
     * port and does not have any {@link Interceptor}s.
     */

    public Server() {
        this(null, null);
    }

    /**
     * Returns the host String for use in Ref objects.
     *
     * @return the host String for use in Ref objects
     */

    private String getHost() {
        waitForBind();
        InetAddress addr = ssock.getInetAddress();
        return addr.getHostName();
    }

    /**
     * Returns the port for use in Ref objects.
     *
     * @return the port for use in Ref objects
     */

    private int getPort() {
        waitForBind();
        return ssock.getLocalPort();
    }

    /**
     * Creates a bound {@link ServerSocket} using the {@link
     * RMISocketFactory} API.
     *
     * @throws IOException when there is an I/O error
     */

    private void bind() throws IOException {
        synchronized (monitor) {
            ssock = ssf.createServerSocket(port);
            setName("IRMI Server " + ssock.getLocalPort());
            reaper.setName(getName() + ": Reaper");
            monitor.notifyAll();
        }
    }

    /**
     * Used by {@link #getHost()} and {@link #getPort()} to avoid null
     * pointer exceptions when one thread may have started the server
     * and another thread is exporting objects.
     */

    private void waitForBind() {
        synchronized (monitor) {
            if (ssock == null) {
                try {
                    monitor.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * Accepts connections and launches Handler threads.
     */

    public void run() {
        try {
            bind();
            while (true) {
                Socket client = ssock.accept();
                client.setTcpNoDelay(true);
                new Handler(client).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Return the {@link Entry} object for the given oid or null if
     * there is none.
     *
     * @param oid the ObjID of an exported object
     * @return the {@link Entry} for <var>oid</var> or null if there
     * is none
     */

    private synchronized Entry getEntry(ObjID oid) {
        return (Entry) entries.get(oid);
    }

    /**
     * Return the exported object for the given oid or null if there
     * is none.
     *
     * @param oid the oid of an exported object
     * @return the exported object or null if there is none
     */

    public Remote getObject(ObjID oid) {
        Entry entry = getEntry(oid);
        if (entry == null) { return null; }
        return entry.getObject();
    }

    /**
     * Return the ObjID for the given {@link Remote} object or null if
     * there is none.
     *
     * @param obj an exported object
     * @return the ObjID assigned to the exported object, or null if
     * there is none
     */

    public ObjID getOID(Remote obj) {
        return (ObjID) oids.get(obj);
    }

    /**
     * Return the {@link ClassLoader} used for deserializing arguments
     * to methods of the exported object mapped to the given oid or
     * null if there is none.
     *
     * @param oid the oid of an exported object
     * @return the ClassLoader used for deserializing arguments to
     * methods of the exported object mapped to the given oid or null
     * if there is none
     */

    public ClassLoader getLoader(ObjID oid) {
        Entry entry = getEntry(oid);
        if (entry == null) { return null; }
        return entry.getLoader();
    }

    /**
     * Construct and return a {@link Skeleton} for the given {@link
     * Remote} object.
     *
     * @param obj a {@link Remote} object
     * @return the {@link Skeleton} for <var>obj</var>
     */

    public Skeleton getSkel(Remote obj) {
        Class klass = getSkelClass(obj.getClass());
        if (klass == null) { return null; }
        try {
            return (Skeleton) klass.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Delegates to {@link #getClass(Class, String)} with "_Skel" as
     * the suffix.
     *
     * @param klass a remote class
     * @return the "_Skel" class for <var>klass</var> or null if there
     * is none
     */

    private Class getSkelClass(Class klass) {
        return getClass(klass, "_Skel");
    }

    /**
     * Converts the given {@link Remote} object to a {@link
     * RemoteStub} instance. This may involve nothing if the object is
     * already a stub, or it may involve search for an appropriate
     * stub class and constructing an instance of it.
     *
     * @param obj the {@link Remote} object
     * @return a {@link RemoteStub} for <var>obj</var>
     */

    public RemoteStub getStub(Remote obj) {
        if (obj instanceof RemoteStub) {
            return (RemoteStub) obj;
        }

        ObjID oid = getOID(obj);
        if (oid == null) {
            return null;
        }

        Ref ref = new Ref(getHost(), getPort(), oid, clint);
        return getStub(obj.getClass(), ref);
    }

    /**
     * Constructs a {@link RemoteStub} instance for the given {@link
     * Class} using the given {@link Ref}.
     *
     * @param remote the class of the {@link Remote} object
     * @param ref the Ref to pass to the stub constructor
     * @return the newly constructed {@link RemoteStub} instance
     */

    private RemoteStub getStub(Class remote, Ref ref) {
        Class klass = getStubClass(remote);
        try {
            Constructor cons = klass.getConstructor(STUB_PARAMS);
            return (RemoteStub) cons.newInstance(new Object[] {ref});
        } catch (NoSuchMethodException e) {
            throw new RuntimeException
                ("unable to locate stub constructor", e);
        } catch (InstantiationException e) {
            throw new RuntimeException
                ("unable to instantiate stub", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException
                ("unable to access stub constructor", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException
                ("unable to construct stub", e);
        }
    }

    /**
     * Delegates to {@link getClass(Class, String)} with "_Stub" as
     * the suffix.
     *
     * @param klass a remote class
     * @return the "_Stub" class for <var>klass</var> or null if there
     * is none
     */

    private Class getStubClass(Class klass) {
        return getClass(klass, "_Stub");
    }

    /**
     * Searches through the class hierarchy for the most specific
     * class whose name matches the given class' name when the given
     * suffix has been appended.
     *
     * @param klass the search start in the klass hierarchy
     * @param suffix the suffix to append to generate candidate
     * classes
     * @return the resulting class or null if none is found
     */

    private Class getClass(Class klass, String suffix) {
        String name = klass.getName() + suffix;
        Class result = forName(name, klass.getClassLoader());
        if (result == null) {
            ClassLoader loader =
                Thread.currentThread().getContextClassLoader();
            result = forName(name, loader);
        }
        if (result == null) {
            Class sup = klass.getSuperclass();
            if (sup != null) {
                return getClass(sup, suffix);
            }
        }
        return result;
    }

    /**
     * Looks for a class using the given {@link ClassLoader} and
     * converts {@link ClassNotFoundException} to a null result.
     *
     * @param name the class name
     * @param loader the class loader
     * @return the class or null if it was not found
     */
    private Class forName(String name, ClassLoader loader) {
        try {
            return Class.forName(name, true, loader);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * Updates the timestamp for the given oid.
     *
     * @param oid the ObjID being pinged
     */

    private void ping(ObjID oid) {
        Entry entry = getEntry(oid);
        if (entry == null) { return; }
        entry.makeHard();
        entry.setTimestamp(System.currentTimeMillis());
    }

    /**
     * Exports the given {@link Remote} object.
     *
     * @param object the object to export
     * @throws ExportException when there is an error
     */

    public synchronized void export(Remote object) throws ExportException {
        ObjID oid = getOID(object);
        if (oid == null) {
            Class klass = object.getClass();
            Class stub = getStubClass(klass);
            if (stub == null) {
                throw new ExportException
                    ("unable to find stub for " + klass);
            }
            ClassLoader union = UnionClassLoader.get
                (klass.getClassLoader(),
                 Thread.currentThread().getContextClassLoader());
            Entry entry = new Entry(object, union, System.currentTimeMillis());
            oid = new ObjID();
            oids.put(object, oid);
            entries.put(oid, entry);
        }
    }

    /**
     * Unexports the given {@link Remote} object if it is being
     * exported by this Server.
     *
     * @param obj the object to unexport
     * @return true iff the object was unexported
     */

    public boolean unexport(Remote obj) {
        if (obj instanceof RemoteStub) {
            RemoteStub stub = (RemoteStub) obj;
            Ref ref = (Ref) stub.getRef();
            return unexport(ref.getOID());
        } else {
            ObjID oid = getOID(obj);
            if (oid == null) {
                return false;
            } else {
                return unexport(oid);
            }
        }
    }

    /**
     * Unexports the {@link Remote} object associated with the given
     * {@link ObjID} if it is being exported by this Server.
     *
     * @param oid the ObjID of the {@link Remote} object to be
     * unexported
     * @return true iff the object was unexported
     */

    public synchronized boolean unexport(ObjID oid) {
        Entry removed = (Entry) entries.remove(oid);
        if (removed == null) { return false; }
        oids.remove(removed.getObject());
        return true;
    }

    /**
     * Struct for returning both the value and a code from the various
     * invoke methods.
     */

    static class Result {

        /**
         * The code indicating what kind of result this is. The values
         * are documented in the {@link Constants} interface.
         */
        byte code;

        /**
         * The value of this Result.
         */
        Object value;
    }

    /**
     * A convenience exception for aborting remote method execution
     * with a given result.
     */

    static class ResultException extends Exception {
        private Result result;
        public ResultException(Throwable t) {
            result = system(t);
        }
        public ResultException(String msg) {
            result = system(msg);
        }
        public Result getResult() { return result; }
    }

    /**
     * Shorthand for constructing a system error with the given value.
     *
     * @param t an exception indicating the type of system error
     * @return the newly constructed {@link Result} object
     */

    static Result system(Throwable t) {
        Result result = new Result();
        result.code = SYSTEM_ERROR;
        result.value = t;
        return result;
    }

    /**
     * Shorthand for constructing a system error with the given error
     * message.
     *
     * @param msg the error message
     * @return the newly constructed {@link Result} object
     */

    static Result system(String msg) {
        return system(new RemoteException(msg));
    }

    /**
     * Find the Server object that exports the given {@link ObjID} or
     * null if it is a remote {@link ObjID}.
     *
     * @param oid the oid
     * @return the Server exporting <var>oid</var> or null if
     * <var>oid</var> is remote
     */

    static Server getServer(ObjID oid) {
        Collection servers = getServers();
        for (Iterator it = servers.iterator(); it.hasNext(); ) {
            Server server = (Server) it.next();
            Remote obj = server.getObject(oid);
            if (obj != null) {
                return server;
            }
        }
        return null;
    }

    /**
     * A {@link Timer} for tracking DGC overhead.
     */
    private static final Timer PING = new Timer("PING");

    /**
     * Sends a ping message to the given host, port updating the
     * timestamp for the given oids. The network path is
     * short-circuited for local oids.
     *
     * @param host the remote host
     * @param port the remote port
     * @param oids the oids to ping
     */
    static void ping(String host, int port, Collection oids) throws IOException {
        PING.start();
        try {
            List remote = new ArrayList(oids.size());
            for (Iterator it = oids.iterator(); it.hasNext(); ) {
                ObjID oid = (ObjID) it.next();
                Server server = getServer(oid);
                if (server != null) {
                    server.ping(oid);
                } else {
                    remote.add(oid);
                }
            }

            if (remote.isEmpty()) {
                return;
            }

            Pool pool = getPool();
            Pool.Entry entry = pool.acquire(host, port);
            ObjectOutputStream out = entry.getOut();
            out.writeByte(DGC_PING);
            out.writeInt(remote.size());
            for (int i = 0; i < remote.size(); i++) {
                ObjID oid = (ObjID) remote.get(i);
                oid.write(out);
            }
            out.flush();
            pool.release(entry);
        } finally {
            PING.stop();
        }
    }

    /**
     * A {@link Timer} for tracking local call overhead.
     */
    private static final Timer LOCAL = new Timer("LOCAL");
    /**
     * A {@link Timer} for tracking remote call overhead.
     */
    private static final Timer CLIENT = new Timer("CLIENT");

    /**
     * Invokes a method either remotely or locally. For v1.1
     * invocations meth is null and hash is the interface hash. For
     * v1.2 or later invocations meth is nonnull, opnum is -1, and
     * hash is the method hash.
     *
     * @param ref the remote ref for which the method is being invoked
     * @param meth the method being invoked or null for v1.1
     * invocations
     * @param opnum -1 or an index into the operations array for v1.1
     * invocations
     * @param hash the method hash or the interface hash for v1.1
     * invocations
     * @param args the arguments for the method call
     * @return a {@link Result} object with the invocation result
     */

    static Result invoke(Ref ref, Method meth, int opnum, long hash,
                         Object[] args) {
        ObjID oid = ref.getOID();
        Server server = getServer(oid);

        Result result;
        if (server != null) {
            LOCAL.start();
            try {
                result = server.invoke(oid, meth, opnum, hash, args);
            } finally {
                LOCAL.stop();
            }
        } else {
            CLIENT.start();
            try {
                result = invoke(ref, opnum, hash, args);
            } finally {
                CLIENT.stop();
            }
        }

        return result;
    }

    /**
     * A {@link Timer} for measuring {@link Interceptor} overhead.
     */
    private static final Timer INTERCEPTS = new Timer("INTERCEPTS");
    /**
     * A {@link Timer} for measuring serialization overhead.
     */
    private static final Timer SERIALIZE = new Timer("SERIALIZE");
    /**
     * A {@link Timer} for measuring deserialization overhead.
     */
    private static final Timer DESERIALIZE = new Timer("DESERIALIZE");

    /**
     * Sends a remote method invocation request over the network. For
     * v1.1 invocations opnum is an index into the operations array
     * and hash is the interface hash. For v1.2 and later, opnum is -1
     * and hash is the method hash.
     *
     * @param ref the {@link Ref} for that requested this invocation
     * @param opnum -1 or an index into the operations array for v1.1
     * invocations
     * @param hash the method hash or the interface hash for v1.1
     * invocations
     * @param args the arguments for the method call
     * @return the {@link Result} for the method invocation
     */

    private static Result invoke(Ref ref, int opnum, long hash,
                                 Object[] args) {
        ClientInterceptor clint = ref.getInterceptor();
        ObjID oid = ref.getOID();
        Result result = new Result();

        try {
            Pool pool = getPool();
            Pool.Entry entry = pool.acquire(ref);
            ObjectOutputStream out = entry.getOut();
            out.reset();

            out.writeByte(METHOD_CALL);
            oid.write(out);
            out.writeInt(opnum);
            out.writeLong(hash);
            if (clint != null) {
                INTERCEPTS.start();
                try {
                    clint.send(METHOD_CALL, out);
                } finally {
                    INTERCEPTS.stop();
                }
            }
            if (opnum >= 0) {
                out.writeShort(args == null ? 0 : args.length);
            }
            if (args != null) {
                SERIALIZE.start();
                try {
                    for (int i = 0; i < args.length; i++) {
                        out.writeObject(args[i]);
                    }
                } finally {
                    SERIALIZE.stop();
                }
            }

            out.flush();

            ObjectInputStream in = entry.getIn();

            result.code = in.readByte();
            DESERIALIZE.start();
            try {
                result.value = in.readObject();
            } finally {
                DESERIALIZE.stop();
            }
            if (clint != null) {
                INTERCEPTS.start();
                try {
                    clint.receive(result.code, in);
                } finally {
                    INTERCEPTS.stop();
                }
            }
            if (result.code == SYSTEM_ERROR) {
                pool.close(entry);
            } else {
                pool.release(entry);
            }
        } catch (IOException e) {
            result.code = SYSTEM_ERROR;
            result.value = e;
        } catch (ClassNotFoundException e) {
            result.code = SYSTEM_ERROR;
            result.value = e;
        }

        return result;
    }

    /**
     * Invokes a remote method. For v1.1 invocations the method is
     * null.
     *
     * @param oid the {@link ObjID} for the {@link Remote} object
     * @param meth the method to invoke or null if this is a v1.1
     * invocation
     * @param opnum -1 or an index into the operations array for v1.1
     * invocations
     * @param hash the method hash or the interface hash for v1.1
     * invocations
     * @param args the method arguments
     * @return the {@link Result} of the method invocation
     */

    Result invoke(ObjID oid, Method meth, int opnum, long hash, Object[] args) {
        try {
            Remote obj = findObject(oid);
            if (opnum < 0) {
                return invoke(obj, meth, args);
            } else {
                return invoke(obj, opnum, hash, args);
            }
        } catch (ResultException e) {
            return e.getResult();
        }
    }

    /**
     * Reflectively invokes the given method on the given object and
     * arguments. Any normal or exceptional result is stored in a
     * {@link Result} instance and returned.
     *
     * @param obj the {@link Remote} object
     * @param meth the method to invoke
     * @param args the arguments for the method
     * @return the {@link Result} of the method invocation
     */

    Result invoke(Remote obj, Method meth, Object[] args) {
        Result result = new Result();
        try {
            result.value = meth.invoke(obj, args);
            result.code = METHOD_RESULT;
        } catch (IllegalAccessException e) {
            result.code = SYSTEM_ERROR;
            result.value = e;
        } catch (InvocationTargetException e) {
            result.code = METHOD_ERROR;
            result.value = e.getTargetException();
        }
        return result;
    }

    /**
     * Invokes the given operation by dispatching to the "_Skel"
     * instance for the given {@link Remote} object.
     *
     * @param obj the {@link Remote} object
     * @param opnum an index into the operations array
     * @param hash the interface hash
     * @param args the method arguments
     * @return the {@link Result} of the method invocation
     */

    Result invoke(Remote obj, int opnum, long hash, Object[] args) {
        Skeleton skel = getSkel(obj);
        if (skel == null) {
            return system("no skel for object: " + obj);
        }

        List list = new ArrayList();
        RemoteCall call = new Call(new ObjectInputList(Arrays.asList(args)),
                                   new ObjectOutputList(list));
        Result result = new Result();
        try {
            skel.dispatch(obj, call, opnum, hash);
            result.code = METHOD_RESULT;
            if (list.size() > 0) {
                result.value = list.get(0);
            }
        } catch (Throwable t) {
            if (t instanceof RemoteException) {
                return system(t);
            } else {
                result.code = METHOD_ERROR;
                result.value = t;
            }
        }
        return result;
    }

    /**
     * Finds the exported object for a given {@link ObjID}.
     *
     * @param oid the {@link ObjID} of an exported object
     * @return the exported object
     * @throws ResultException if there is no exported object for the
     * given {@link ObjID}
     */

    private Remote findObject(ObjID oid) throws ResultException {
        Remote obj = getObject(oid);
        if (obj == null) {
            throw new ResultException
                (new NoSuchObjectException("oid: " + oid));
        }
        return obj;
    }

    /**
     * Finds the {@link Method} object for the given {@link Class} and
     * method hash.
     *
     * @param klass the class
     * @param hash the method hash
     * @return the method of <var>klass</var> matching the given hash
     * @throws ResultException when there is no matching method
     */

    private Method findMethod(Class klass, long hash) throws ResultException {
        Method meth = Hashes.getMethod(klass, hash);
        if (meth == null) {
            throw new ResultException
                (new NoSuchMethodException("hash: " + hash));
        }
        return meth;
    }

    /**
     * A {@link Timer} for tracking dispatch overhead.
     */
    private static final Timer DISPATCH = new Timer("DISPATCH");
    /**
     * A {@link Timer} for tracking invoke overhead.
     */
    private static final Timer INVOKE = new Timer("INVOKE");

    /**
     * There is exactly one Handler thread per accepted connection.
     */

    private class Handler extends Thread {

        /**
         * The client socket.
         */
        private Socket client;

        /**
         * The ObjectInputStream used to read client requests.
         */
        private ObjectInputStream in;

        /**
         * The ObjectOutputStream used to respond to client requests.
         */
        private ObjectOutputStream out;

        /**
         * This can be set to false to terminate this thread.
         */
        private boolean loop = true;

        /**
         * Constructs a new handler thread for the given client {@link
         * Socket}.
         *
         * @param client the client {@link Socket}
         * @throws IOException when there is an I/O error
         */

        public Handler(Socket client) throws IOException {
            this.client = client;
            this.in = new RMIObjectInputStream(client.getInputStream());
            this.out = new RMIObjectOutputStream(client.getOutputStream());
        }

        public void run() {
            try {
                client.setTcpNoDelay(true);
                client.setKeepAlive(true);
                String ip = client.getInetAddress().getHostAddress();
                setName("IRMI Connection - " + ip + ":" + client.getPort());
                while (loop) {
                    dispatch();
                }
                client.shutdownOutput();
                client.close();
            } catch (EOFException e) {
                try {
                    client.close();
                } catch (IOException ee) {
                    // do nothing
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * Reads in a code indicating the type of client request and
         * dispatches to the appropriate subroutine for handling the
         * request.
         *
         * @throws IOException when there is an I/O error
         */

        private void dispatch() throws IOException {
            byte code = in.readByte();
            DISPATCH.start();
            try {
                switch (code) {
                case METHOD_CALL:
                    call();
                    break;
                case DGC_PING:
                    ping();
                    break;
                default:
                    System.err.println("bad protocol constant: " + code);
                    loop = false;
                }
            } finally {
                DISPATCH.stop();
            }
        }

        /**
         * Reads an object in from the client and converts any
         * ClassNotFoundExceptions into a system error.
         *
         * @throws IOException when there is an I/O error
         * @throws ResultException when there is an error
         * deserializing the object
         */

        private Object readObject() throws IOException, ResultException {
            try {
                return in.readObject();
            } catch (ClassNotFoundException e) {
                throw new ResultException(e);
            }
        }

        /**
         * Reads in a request for a remote invocation, invokes the
         * method, and writes out the result.
         *
         * @throws IOException when there is an I/O error
         */

        private void call() throws IOException {
            Result result;
            boolean invoked = false;
            try {
                ObjID oid = ObjID.read(in);
                Remote obj = findObject(oid);
                Class klass = obj.getClass();

                setContextClassLoader(getLoader(oid));

                int opnum = in.readInt();
                long hash = in.readLong();

                if (srvint != null) {
                    INTERCEPTS.start();
                    try {
                        srvint.receive(METHOD_CALL, in);
                    } catch (ClassNotFoundException e) {
                        throw new ResultException(e);
                    } finally {
                        INTERCEPTS.stop();
                    }
                }

                if (opnum < 0) {
                    Method meth = findMethod(klass, hash);
                    int nargs = meth.getParameterTypes().length;
                    Object[] args = new Object[nargs];
                    DESERIALIZE.start();
                    try {
                        for (int i = 0; i < args.length; i++) {
                            args[i] = readObject();
                        }
                    } finally {
                        DESERIALIZE.stop();
                    }
                    INVOKE.start();
                    try {
                        result = invoke(obj, meth, args);
                    } finally {
                        INVOKE.stop();
                    }
                } else {
                    int nargs = in.readShort();
                    Object[] args = new Object[nargs];
                    DESERIALIZE.start();
                    try {
                        for (int i = 0; i < args.length; i++) {
                            args[i] = readObject();
                        }
                    } finally {
                        DESERIALIZE.stop();
                    }
                    INVOKE.start();
                    try {
                        result = invoke(obj, opnum, hash, args);
                    } finally {
                        INVOKE.stop();
                    }
                }
            } catch (ResultException e) {
                result = e.getResult();
            }

            out.reset();
            out.writeByte(result.code);
            SERIALIZE.start();
            try {
                out.writeObject(result.value);
            } finally {
                SERIALIZE.stop();
            }
            if (srvint != null) {
                INTERCEPTS.start();
                try {
                    srvint.send(result.code, out);
                } finally {
                    INTERCEPTS.stop();
                }
            }
            out.flush();
            if (result.code == SYSTEM_ERROR) {
                loop = false;
            }
        }

        private void ping() throws IOException {
            int num = in.readInt();
            for (int i = 0; i < num; i++) {
                ObjID oid = ObjID.read(in);
                Server.this.ping(oid);
            }
        }
    }

    /**
     * RemoteCall implementation used when calling {@link
     * Skeleton.dispatch(Remote, RemoteCall, int, long)} for v1.1
     * invocations.
     */

    private class Call implements RemoteCall {

        private ObjectInput in;
        private ObjectOutput out;

        /**
         * Constructs a new Call instance that uses the given {@link
         * ObjectInput} for arguments and the given {@link
         * ObjectOuput} for results.
         */

        Call(ObjectInput in, ObjectOutput out) {
            this.in = in;
            this.out = out;
        }

        public ObjectInput getInputStream() {
            return in;
        }

        public void releaseInputStream() {
            // do nothing;
        }

        public ObjectOutput getResultStream(boolean success)
            throws IOException, StreamCorruptedException {
            if (!success) {
                throw new RuntimeException
                    ("don't know how to handle success = false case");
            }
            return out;
        }

        /**
         * <font color="red">UNSUPPORTED</font>
         */

        public ObjectOutput getOutputStream() {
            throw new IllegalStateException("server side call");
        }

        /**
         * <font color="red">UNSUPPORTED</font>
         */

        public void releaseOutputStream() {
            throw new IllegalStateException("server side call");
        }

        /**
         * <font color="red">UNSUPPORTED</font>
         */

        public void executeCall() {
            throw new IllegalStateException("server side call");
        }

        /**
         * <font color="red">UNSUPPORTED</font>
         */

        public void done() {
            throw new IllegalStateException("server side call");
        }
    }

    /**
     * The interval between reaper sweeps.
     */
    private static final long INTERVAL = 60*1000;
    /**
     * The timeout after which an object can be reaped.
     */
    private static final long TIMEOUT = 10*INTERVAL;

    /**
     * The Reaper thread automatically unexports objects that have
     * been idle for longer than TIMEOUT.
     */

    private class Reaper extends Thread {
        public Reaper() {
            setDaemon(true);
        }
        public void run() {
            while (true) {
                try {
                    sleep(INTERVAL);
                } catch (InterruptedException e) {
                    break;
                }
                List l;
                synchronized (Server.this) {
                    l = new ArrayList(entries.entrySet());
                }
                for (int i = 0; i < l.size(); i++) {
                    Map.Entry me = (Map.Entry) l.get(i);
                    ObjID oid = (ObjID) me.getKey();
                    Entry entry = (Entry) me.getValue();
                    long now = System.currentTimeMillis();
                    if (now - entry.getTimestamp() > TIMEOUT) {
                        entry.makeWeak();
                        Remote obj = entry.getObject();
                        if (obj == null) {
                            unexport(oid);
                        }
                    }
                }
            }
        }
    }

}
