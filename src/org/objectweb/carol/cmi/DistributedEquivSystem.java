/**
 * Copyright (C) 2002-2005 - Bull S.A.
 *
 * CMI : Cluster Method Invocation
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
 * --------------------------------------------------------------------------
 * $Id: DistributedEquivSystem.java,v 1.10 2005/07/27 11:49:22 pelletib Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.cmi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.ChannelClosedException;
import org.jgroups.ChannelException;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.SuspectEvent;
import org.jgroups.View;
import org.objectweb.carol.util.configuration.TraceCarol;

/**
 * msg format to export an object
 *
 * @author Simon Nieuviarts
 */
class ExportMsg implements Serializable {

    /**
     * Object Identifier
     */
    public transient ObjectId oid;

    /**
     * Key
     */
    public transient Serializable key;

    /**
     * Stub
     */
    public transient byte[] stub;

    /**
     * Distributing factor
     */
    public transient int factor;

    /**
     * Constructor - creates the export format for the stub
     * @param key key
     * @param sd stub data
     */
    public ExportMsg(Serializable key, StubData sd) {
        this.oid = sd.getObjectId();
        this.key = key;
        this.stub = sd.getSerializedStub();
        this.factor = sd.getFactor();
    }

    /**
     * Write the export stub to the ouput stream
     * @param out output
     * @throws IOException if an I/O error occurs
     */
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        oid.writeExternal(out);
        out.writeObject(key);
        out.writeObject(stub);
        out.writeInt(factor);
    }

    /**
     * Read the input stream and builds an export object
     * @param in input
     * @throws IOException if an I/O error occurs
     * @throws ClassNotFoundException if the expected object can't be build
     */
    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        oid = ObjectId.read(in);
        key = (Serializable) in.readObject();
        stub = (byte[]) in.readObject();
        factor = in.readInt();
    }
}

/**
 * msg format to unexport an object
 *
 * @author Simon Nieuviarts
 */
class UnexportMsg implements Serializable {

    /**
     * Key
     */
    public transient Serializable k;

    /**
     * server id
     */
    public transient ClusterId i;

    /**
     * Constructor
     * @param key key
     * @param serverId server id
     */
    public UnexportMsg(Serializable key, ClusterId serverId) {
        k = key;
        i = serverId;
    }

    /**
     * Write the object to the output stream
     * @param out output
     * @throws IOException, if an I/O error occurs
     */
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        i.write(out);
        out.writeObject(k);
    }

    /**
     * Read an object from the input stream
     * @param in input
     * @throws IOException if an I/O error occurs
     * @throws ClassNotFoundException if the expected object can't be build
     */
    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        i = ClusterId.read(in);
        k = (Serializable) in.readObject();
    }
}

/**
 * Maps keys to DistributedEquivStubs objects.
 *
 * @author Simon Nieuviarts
 */
class GlobalExports {
    /**
     * Hashmap
     */
    private HashMap table = new HashMap();

    /**
     * Default Constructor
     *
     */
    public GlobalExports() {}

    /**
     * Put an entry [key,stubdata] in the hashmap
     * @param key key
     * @param stubData stub data
     * @throws RemoteException if an exception is encountered
     */
    public synchronized void put(Serializable key, StubData stubData)
            throws RemoteException {
        DistributedEquivStubs stubList = (DistributedEquivStubs) table.get(key);
        if (stubList == null) {
            stubList = new DistributedEquivStubs();
            table.put(key, stubList);
        }
        stubList.setStub(stubData);
    }

    /**
     * Remove a server id in the hashmap
     * if the server id was the last entry in the stubdata, the map entry is removed
     * @param key key
     * @param serverId server id
     */
    public synchronized void remove(Serializable key, ClusterId serverId) {
        DistributedEquivStubs stubList = (DistributedEquivStubs) table.get(key);
        if (stubList == null) {
            return;
        }
        if (stubList.removeStub(serverId)) {
            table.remove(key);
        }
    }

    /**
     * Remove a server id in the stubdata
     * if the server id was the last entry in the stubdata, the map entry is removed
     * @param serverId server id
     */
    public synchronized void zapExports(ClusterId serverId) {
        Iterator i = table.values().iterator();
        while (i.hasNext()) {
            DistributedEquivStubs stubList = (DistributedEquivStubs) i.next();
            if (stubList.removeStub(serverId)) {
                i.remove();
            }
        }
    }

    /**
     * Get the stub list by key
     * @param key key
     * @return server stub list
     * @throws RemoteException  if an exception is encountered
     */
    public ServerStubList getStubList(Serializable key) throws RemoteException {
        synchronized (this) {
            DistributedEquivStubs stubs = (DistributedEquivStubs) table.get(key);
            return (stubs == null) ? null : stubs.getServerStubList();
        }
    }

    /**
     * Creates a set with the hashmap entries
     * @return set
     */
    public synchronized Set keySet() {
        HashSet s = new HashSet();
        Iterator it = table.keySet().iterator();
        while (it.hasNext()) {
            s.add(it.next());
        }
        return s;
    }
}

/**
 * Thread performing a socket send used to identify the bind address
 *
 * @author Simon Nieuviarts
 */
class BindAddressChooser extends Thread {

    /**
     * Multicast Socket
     */
    private MulticastSocket sock;

    /**
     * Address
     */
    private InetAddress group;

    /**
     * Port
     */
    private int port;

    /**
     * Timeout
     */
    static final int TIMEOUT = 10;

    /**
     * Nb retries
     */
    static final int RETRIES = 20;

    /**
     * Constructor
     * @param sock socket
     * @param group address
     * @param port port
     */
    BindAddressChooser(MulticastSocket sock, InetAddress group, int port) {
        this.sock = sock;
        this.group = group;
        this.port = port;
    }

    /**
     * Body of the thread : sending a message
     */
    public void run() {
        for (int i = 0; i < RETRIES; i++) {
            byte[] msg = {0};
            DatagramPacket pkt = new DatagramPacket(msg, msg.length, group,
                    port);
            try {
                sock.send(pkt);
            } catch (IOException e) {
                // Something wrong with the socket, should return ?
            }
            try {
                Thread.sleep(TIMEOUT);
            } catch (InterruptedException e1) {
                // Work finished
                return;
            }
        }
    }
}

/**
 * Manage equivalences between objects in the cluster. Two objects are equivalent if
 * their keys have the same value (key1.equals(key2)).
 *
 *  @author Simon Nieuviarts
 */
class DistributedEquivSystem {

    /**
     * Protocol stack (JGroups)
     */
    private String chan_props;

    /**
     * Group name
     */
    private String groupname;

    /**
     * Jgroups Channel
     */
    private Channel chan;

    /**
     * Message dequeuer thread
     */
    private MessageDequeuer mdq;

    /**
     * JGroups view
     */
    private View view;

    /**
     * JGroups address
     */
    private Address my_addr;

    /**
     * Server Id
     */
    private ClusterId myServerId;

    /**
     * Object id factory
     */
    private ObjectIdFactory oidFactory;

    /**
     * local exports
     */
    private HashMap localExports = new HashMap();

    /**
     * global exports
     */
    private GlobalExports globalExports = new GlobalExports();

    /**
     * id map
     */
    private HashMap idMap = new HashMap();

    /**
     * The message dequeuer must not be multithreaded, to preserve message
     * processing order. So, we do not need to synchronize message handling
     * functions.
     *
     *  @author Simon Nieuviarts
     */
    private class MessageDequeuer extends Thread {
        public void run() {
            if (TraceCarol.isDebugCmiDes())
                    TraceCarol.debugCmiDes("Message dequeuer started");
            Object msg;
            try {
                do {
                    // Awful. why not checked by Receive() ?
                    if (isInterrupted()) {
                        break;
                    }
                    msg = chan.receive(0);
                    if (msg == null) {
                        continue;
                    }
                    else if (msg instanceof Message){
                        receive((Message) msg);
                    }
                    else if (msg instanceof View) {
                        viewAccepted((View) msg);
                    }
                    else if (msg instanceof SuspectEvent) {
                        ;
                    }
                    else if (TraceCarol.isDebugCmiDes()) {
                            TraceCarol
                                    .debugCmiDes("Received but not supported : "
                                            + msg.getClass());
                    }
                } while (true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (TraceCarol.isDebugCmiDes())
                    TraceCarol.debugCmiDes("Message dequeuer finished.");
        }
    }

    /**
     * Choose a bind address - seems to be not used anymore
     * @param groupname_or_ip groupname or ip addr
     * @param port port
     * @return Bind addr
     */
    private static String chooseBindAddress2(String groupname_or_ip, int port) {
        int ip_ttl = 0;
        MulticastSocket sock;
        Thread sender;
        try {
            InetAddress group = InetAddress.getByName(groupname_or_ip);
            sock = new MulticastSocket(port);
            sender = new BindAddressChooser(sock, group, port);
            sock.setTimeToLive(ip_ttl);
            sock.joinGroup(group);
        } catch (IOException e2) {
            return null;
        }

        sender.start();
        byte[] buf = new byte[2];
        DatagramPacket recv = new DatagramPacket(buf, buf.length);
        long end = System.currentTimeMillis() + BindAddressChooser.RETRIES
                * BindAddressChooser.TIMEOUT;
        do {
            recv.setData(buf, 0, buf.length);
            try {
                sock.receive(recv);
            } catch (IOException e1) {
                // Something wrong with the socket, cancel
                sender.interrupt();
                return null;
            }
            byte[] msg = recv.getData();
            if ((recv.getLength() != 1) || (recv.getData()[0] != 0)) {
                continue;
            }
            InetAddress a = recv.getAddress();
            try {
                sock.setInterface(a);
            } catch (SocketException e) {
                continue;
            }
            sender.interrupt();
            return a.getHostAddress();
        } while (System.currentTimeMillis() < end);
        sender.interrupt();
        return null;
    }

    /**
     * Choose a bind address
     * @return Bind addr
     */
    private static String chooseBindAddress() {
        String s = ServerConfig.getMulticastItf();
        if (s == null) {
            return null;
        }
        LinkedList l;
        try {
            InetMask m = new InetMask(s);
            l = m.filterLocal();
        } catch (Exception e) {
            return null;
        }
        if (l.size() != 1) {
            return null;
        }
        return ((InetAddress) l.getFirst()).getHostAddress();
    }

    /**
     * Constructor
     * Initialize the JGroups channel
     * @throws ServerConfigException, if there is a configuration error
     * @throws ChannelException, if an exception occurs in the JGroups channem
     * @throws ChannelClosedException, if Jgroups channel is closed
     */
    DistributedEquivSystem() throws ServerConfigException,
            ChannelException, ChannelClosedException {
        ServerIdFactory sidf = new ServerIdFactory();
        String mcast_addr = ServerConfig.getMulticastAddress();
        int mcast_port = ServerConfig.getMulticastPort();
        String bind_addr = chooseBindAddress();
        if (bind_addr != null) {
            bind_addr = ";bind_addr=" + bind_addr;
            if (TraceCarol.isDebugCmiDes()) {
                    TraceCarol.debugCmiDes("bind address : "
                            + bind_addr.substring(11));
            }
        } else {
            bind_addr = "";
            TraceCarol.debugCmiDes("bind address : null");
        }

        // protocol stack allowing to multiple nodes to start simultaneously and
        // merge their group.
        chan_props = "UDP(mcast_addr="
            + mcast_addr
            + ";mcast_port="
            + mcast_port
            + bind_addr
            + ";ip_ttl=32;"
            + "mcast_send_buf_size=150000;mcast_recv_buf_size=80000):"
            + "PING(timeout=2000;num_initial_members=3):"
            + "MERGE2(min_interval=5000;max_interval=10000):"
            + "FD(timeout=2000;max_tries=3;shun=true):"
            + "VERIFY_SUSPECT(timeout=1500):"
            + "pbcast.NAKACK(gc_lag=50;retransmit_timeout=300,600,1200,2400,4800):"
            + "UNICAST(timeout=1200,2400,3600):"
            + "pbcast.STABLE(stability_delay=1000;desired_avg_gossip=2000):"
            + "FRAG(frag_size=4096;down_thread=false;up_thread=false):"
            + "pbcast.GMS(join_timeout=3000;join_retry_timeout=2000;"
            + "shun=false;print_local_addr=true)";

        //org.jgroups.log.Trace.init();
        groupname = ServerConfig.getMulticastGroupName();
        chan = new JChannel(chan_props);
        chan.connect(groupname);
        my_addr = chan.getLocalAddress();
        myServerId = sidf.getLocalId();
        idMap.put(my_addr, myServerId);

        oidFactory = new ObjectIdFactory(myServerId);

        Vector v = new Vector();
        v.add(my_addr);
        view = new View(my_addr, 0, v);

        mdq = new MessageDequeuer();
        mdq.setContextClassLoader(Thread.currentThread()
                .getContextClassLoader());
        mdq.start();
        if (TraceCarol.isDebugCmiDes()) {
                TraceCarol.debugCmiDes("DistributedEquivSystem started on "
                        + ServerConfig.getMulticastAddress() + ":"
                        + ServerConfig.getMulticastPort() + "/"
                        + ServerConfig.getMulticastGroupName() + ", cluster Id "
                        + myServerId);
        }
    }

    /**
     * Broadcast a message on the cluster
     * @param msg serialized message
     */
    private void broadcast(Serializable msg) {
        ByteArrayOutputStream outs = new ByteArrayOutputStream();
        try {
            CmiOutputStream out = new CmiOutputStream(outs);
            out.writeObject(msg);
            out.flush();
            Message m = new Message(null, my_addr, outs.toByteArray());
            chan.send(m);
            if (TraceCarol.isDebugCmiDes()) {
                TraceCarol.debugCmiDes("broadcast sent");
            }
        } catch (Exception e) {
            if (TraceCarol.isDebugCmiDes()) {
                TraceCarol.debugCmiDes("when broadcasting " + e.toString());
            }
        }
    }

    /**
     * Process a new viewed received on the JGroups's channel
     * @param v2 new view
     */
    private void viewAccepted(View v2) {
        if (TraceCarol.isDebugCmiDes()) {
                TraceCarol.debugCmiDes("New view accepted : " + v2);
        }
        LinkedList newMembers = new LinkedList();
        LinkedList oldMembers = new LinkedList();

        Object ar1[] = ((Vector) (view.getMembers().clone())).toArray();
        Arrays.sort(ar1);
        Iterator i1 = Arrays.asList(ar1).iterator();
        Object ar2[] = ((Vector) (v2.getMembers().clone())).toArray();
        Arrays.sort(ar2);
        Iterator i2 = Arrays.asList(ar2).iterator();

        view = v2;
        Address a1 = (i1.hasNext()) ? (Address) i1.next() : null;
        Address a2 = (i2.hasNext()) ? (Address) i2.next() : null;
        while (true) {
            int d;
            if (a1 != null) {
                if (a2 != null) {
                    d = a1.compareTo(a2);
                }
                else
                    d = -1;
            } else {
                if (a2 == null) {
                    break;
                }
                d = 1;
            }

            if (d > 0) {
                // Member a2 has been added in the new view
                newMembers.addLast(a2);
                a2 = (i2.hasNext()) ? (Address) i2.next() : null;
            } else if (d < 0) {
                // Member a1 has been removed since last view
                oldMembers.addLast(a1);
                a1 = (i1.hasNext()) ? (Address) i1.next() : null;
            } else {
                a1 = (i1.hasNext()) ? (Address) i1.next() : null;
                a2 = (i2.hasNext()) ? (Address) i2.next() : null;
            }
        }

        while (oldMembers.size() > 0) {
            Address a = (Address) oldMembers.removeFirst();
            ClusterId id = (ClusterId) idMap.get(a);
            if (id != null) globalExports.zapExports(id);
            idMap.remove(a);
            if (TraceCarol.isDebugCmiDes()) {
                if (id == null) {
                    TraceCarol.debugCmiDes("Member " + a + " removed");
                }
                else {
                    TraceCarol.debugCmiDes("Member " + a
                            + " removed (server id : " + id + ")");
                }
            }
        }

        if (newMembers.size() > 0) {
            if (TraceCarol.isDebugCmiDes())
                    TraceCarol.debugCmiDes("sending local exports");

            synchronized (localExports) {
                Iterator i = localExports.entrySet().iterator();
                while (i.hasNext()) {
                    Map.Entry e = (Map.Entry) i.next();
                    broadcast(new ExportMsg((Serializable) e.getKey(),
                            (StubData) e.getValue()));
                }
            }

            //	    broadcast(new ExportsMsg(myServerId, localExports));
            // Now done when receiving RequestExportsMsg
            /*
             if (TraceCarol.isDebugCmiDes()) TraceCarol.debugCmiDes("sending local exports");
             try {
             Thread.sleep(3000);
             } catch (Exception e) {
             e.printStackTrace();
             }
             broadcast(new ExportsMsg(myServerId, localExports));
             */
        }

        if (TraceCarol.isDebugCmiDes()) {
            while (newMembers.size() > 0) {
                Address a = (Address) newMembers.removeFirst();
                TraceCarol.debugCmiDes("New member " + a);
            }
        }
    }

    /**
     * Check if this server is allowed in the group
     * @param serverId server id
     * @param ad address
     * @return ClusterId if server allowed, and null if not allowed
     */
    private ClusterId checkServer(ClusterId serverId, Address ad) {
        ClusterId i = (ClusterId) idMap.get(ad);
        if (i == null) {
            i = serverId;
            idMap.put(ad, serverId);
            return serverId;
        } else if (i.equals(serverId)) {
            return serverId;
        }
        TraceCarol.error("Found " + ad + "->" + i + " and now " + ad + "->"
                + serverId + ". Ignoring new server ID !");
        return null;
    }

    /**
     * Check if the server id corresponds to the mine
     * @param serverId server id
     * @return true if equals, otherwise false
     */
    private boolean self(ClusterId serverId) {
        return myServerId.equals(serverId);
    }

    /**
     * Process a new JGroups channel message
     * @param m received msg
     */
    private void receive(Message m) {
        Object o;
        byte[] buf = m.getBuffer();
        if (buf == null) {
            if (TraceCarol.isDebugCmiDes()) {
                    TraceCarol.debugCmiDes("buf == null");
            }
            o = null;
        } else {
            try {
                ByteArrayInputStream in_stream = new ByteArrayInputStream(buf);
                CmiInputStream in = new CmiInputStream(in_stream);
                o = in.readObject();
            } catch (Exception e) {
                if (TraceCarol.isDebugCmiDes()) {
                        TraceCarol.debugCmiDes(e.toString());
                }
                o = null;
            }
        }

        Address from = m.getSrc();
        if (o instanceof ExportMsg) {
            ExportMsg pm = (ExportMsg) o;
            ClusterId serverId = checkServer(pm.oid.getServerId(), from);
            if (serverId == null) {
                return;
            }
            if (self(serverId)) {
                return;
            }
            if (TraceCarol.isDebugCmiDes()) {
                    TraceCarol.debugCmiDes("Put message received from server "
                            + from + ", ID : " + pm.key);
            }
            try {
                StubData sd = new StubData(pm.oid, pm.stub, pm.factor);
                globalExports.put(pm.key, sd);
            } catch (RemoteException e) {}
        } else if (o instanceof UnexportMsg) {
            UnexportMsg rm = (UnexportMsg) o;
            ClusterId serverId = checkServer(rm.i, from);
            if (serverId == null) {
                return;
            }
            if (self(serverId)) {
                return;
            }
            if (TraceCarol.isDebugCmiDes()) {
                    TraceCarol
                            .debugCmiDes("Remove message received from server "
                                    + from + ", ID : " + rm.k);
            }
            globalExports.remove(rm.k, serverId);
        } else if (TraceCarol.isDebugCmiDes()) {
            TraceCarol
                    .debugCmiDes("Message of unknown type received from server "
                            + from);
        }
    }

    /**
     * Terminate the Distributed Equivalent System
     *
     */
    void terminate() {
        mdq.interrupt();
    }

    /**
     * Export an object within the cluster (broadcast)
     *
     * @param key key
     * @param obj object
     * @throws RemoteException if an exception is encountered
     */
    boolean exportObject(Serializable key, byte[] obj) throws RemoteException {
        if (TraceCarol.isDebugCmiDes()) {
                TraceCarol.debugCmiDes("exportObject(" + key + ")");
        }
        StubData sd = new StubData(oidFactory.getId(), obj, ServerConfig
                .getLoadFactor());
        synchronized (localExports) {
            StubData cur = (StubData) localExports.get(key);
            if (cur != null) {
                return false;
            }
            localExports.put(key, sd);
            globalExports.put(key, sd);
            broadcast(new ExportMsg(key, sd));
        }
        return true;
    }

    /**
     * Unexport an object within the cluster (broadcast)
     *
     * @param key key
     * @throws RemoteException if an exception is encountered
     */
    boolean unexportObject(Serializable key) {
        if (TraceCarol.isDebugCmiDes()) {
                TraceCarol.debugCmiDes("unexportObject(" + key + ")");
        }
        synchronized (localExports) {
            StubData cur = (StubData) localExports.get(key);
            if (cur == null) {
                return false;
            }
            localExports.remove(key);
            globalExports.remove(key, myServerId);
            broadcast(new UnexportMsg(key, myServerId));
        }
        return true;
    }

    /**
     * Get the stub list registered under the specified key.
     * @param key key
     * @return <code>null<code> if not exported.
     * @throws RemoteException if an error is encountered
     */
    ServerStubList getGlobal(Serializable key) throws RemoteException {
        return globalExports.getStubList(key);
    }

    /**
     * Get only the standard stub registered in this instance of the DES.
     * @param key key
     * @return <code>null<code> if not exported.
     */
    Remote getLocal(Serializable key) {
        return (Remote) localExports.get(key);
    }

    /**
     * Get a set of the stub
     * @return set of stubs
     */
    Set keySet() {
        return globalExports.keySet();
    }
}
