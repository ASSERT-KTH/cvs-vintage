/*
 * Copyright (C) 2002-2003, Simon Nieuviarts
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
 */
package org.objectweb.carol.cmi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Vector;

import org.javagroups.Address;
import org.javagroups.Channel;
import org.javagroups.JChannel;
import org.javagroups.Message;
import org.javagroups.SuspectEvent;
import org.javagroups.View;
import org.objectweb.carol.util.configuration.TraceCarol;

class ExportMsg implements Serializable {
    public transient ClusterId i;
    public transient Serializable k;
    public transient Remote stub;

    public ExportMsg(ClusterId serverId, Serializable key, Remote stub) {
        i = serverId;
        k = key;
        this.stub = stub;
    }

    private void writeObject(java.io.ObjectOutputStream out)
        throws IOException {
        i.write(out);
        out.writeObject(k);
        out.writeObject(stub);
    }

    private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        i = ClusterId.read(in);
        k = (Serializable) in.readObject();
        stub = (Remote) in.readObject();
    }
}

class RequestExportsMsg implements Serializable {
}

class UnexportMsg implements Serializable {
    public transient ClusterId i;
    public transient Serializable k;

    public UnexportMsg(ClusterId serverId, Serializable key) {
        i = serverId;
        k = key;
    }

    private void writeObject(java.io.ObjectOutputStream out)
        throws IOException {
        i.write(out);
        out.writeObject(k);
    }

    private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        i = ClusterId.read(in);
        k = (Serializable) in.readObject();
    }
}

/**
 * TODO Should be used instead of the loop on localExports. To rewrite and test. 
 * @author nieuviar
 */
//class ExportsMsg implements Serializable {
//    public byte[] i;
//    public byte[] b;
//
//    public ExportsMsg(byte[] serverId, LocalExports reg) throws IOException {
//        i = serverId;
//        b = reg.serialized();
//    }
//
//    public HashMap getMap() {
//        ByteArrayInputStream ins = new ByteArrayInputStream(b);
//        try {
//            MulticastInputStream in = new MulticastInputStream(ins);
//            HashMap h = new HashMap();
//            Object o = in.readObject();
//            if (o instanceof Integer) {
//                return h;
//            } else if (o instanceof String) {
//                Object o2 = in.readObject();
//                if (o2 instanceof Remote) {
//                    h.put(o, o2);
//                    return h;
//                }
//            }
//            return null;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//}

class GlobalExports {
    private HashMap table = new HashMap();

    public GlobalExports() {
    }

    private static Class[] cnstr_params =
        new Class[] { ClusterId.class, Remote.class };

    public synchronized void put(
        ClusterId serverId,
        Serializable key,
        Remote stub)
        throws RemoteException {
        ClusterStub cs = (ClusterStub) table.get(key);
        if (cs == null) {
            Class clusterStubClass =
                ClusterObject.getClusterStubClass(stub.getClass());
            if (clusterStubClass == null)
                throw new RemoteException("no valid cluster stub class");

            java.lang.reflect.Constructor cnstr;
            try {
                cnstr = clusterStubClass.getConstructor(cnstr_params);
                cs =
                    (ClusterStub) cnstr.newInstance(
                        new Object[] { serverId, stub });
            } catch (Exception e) {
                throw new RemoteException(
                    "Can not instanciate cluster stub",
                    e);
            }
            table.put(key, cs);
        } else if (!cs.setStub(serverId, stub))
            if (Trace.DES)
                Trace.out(
                    "DES: Warning: Object registered in the cluster as two distinct types");
    }

    public synchronized void remove(ClusterId serverId, Serializable key) {
        ClusterStub cs = (ClusterStub) table.get(key);
        if (cs == null)
            return;
        if (cs.removeStub(serverId))
            return;
        table.remove(key);
    }

    //    public synchronized void addExports(ClusterId serverId, HashMap reg) {
    //        Iterator i = reg.entrySet().iterator();
    //        while (i.hasNext()) {
    //            Map.Entry e = (Map.Entry) i.next();
    //            try {
    //                put(serverId, (Serializable) e.getKey(), (Remote) e.getValue());
    //            } catch (RemoteException ex) {
    //                ex.printStackTrace();
    //            }
    //        }
    //    }

    public synchronized void zapExports(ClusterId serverId) {
        Iterator i = table.values().iterator();
        while (i.hasNext()) {
            ClusterStub cs = (ClusterStub) i.next();
            if (!cs.removeStub(serverId))
                i.remove();
        }
    }

    public ClusterStub getClusterStub(Serializable key) {
        synchronized (this) {
            return (ClusterStub) table.get(key);
        }
    }

    public synchronized Serializable[] list() {
        int n = table.size();
        Serializable[] lst = new Serializable[n];
        Iterator it = table.keySet().iterator();
        for (int i = 0; i < n; i++) {
            lst[i] = (Serializable) it.next();
        }
        return lst;
    }
}

class LocalExports {
    private HashMap map = new HashMap();
    //    private ByteArrayOutputStream outs = new ByteArrayOutputStream();
    private byte[] buf = null;

    public synchronized void put(Serializable key, Remote obj) {
        if ((key == null) || (obj == null))
            throw new NullPointerException();
        map.put(key, obj);
        buf = null;
    }

    public synchronized Object get(Serializable key) {
        if (key == null)
            throw new NullPointerException();
        return map.get(key);
    }

    public synchronized void remove(Serializable key) {
        if (map.remove(key) != null)
            buf = null;
    }

    public HashMap getmap() {
        return map;
    }

    //    public synchronized byte[] serialized() throws java.io.IOException {
    //        if (buf != null) {
    //            return buf;
    //        }
    //        MulticastOutputStream out = new MulticastOutputStream(outs);
    //        Iterator i = map.entrySet().iterator();
    //        if (i.hasNext()) {
    //            Map.Entry e = (Map.Entry) i.next();
    //            out.writeObject((String) e.getKey());
    //            out.writeObject((Remote) e.getValue());
    //        }
    //        out.writeObject(new Integer(1));
    //        out.flush();
    //        buf = outs.toByteArray();
    //        return buf;
    //    }
}

class BindAddressChooser extends Thread {
    private MulticastSocket sock;
    private InetAddress group;
    private int port;
    static final int TIMEOUT = 10;
    static final int RETRIES = 20;

    BindAddressChooser(MulticastSocket sock, InetAddress group, int port) {
        this.sock = sock;
        this.group = group;
        this.port = port;
    }

    public void run() {
        for (int i = 0; i < RETRIES; i++) {
            byte[] msg = { 0 };
            DatagramPacket pkt =
                new DatagramPacket(msg, msg.length, group, port);
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

class DistributedEquivSystem {
    private String chan_props;
    private String groupname;
    private Channel chan;
    private MessageDequeuer mdq;
    private View view;
    private Address my_addr;
    private ClusterId my_id;
    private LocalExports localExports = new LocalExports();
    private GlobalExports globalExports = new GlobalExports();
    private HashMap idmap = new HashMap();

    /*
     * The message dequeuer must not be multithreaded, to preserve message
     * processing order. So, we do not need to synchronize message handling
     * functions.
     */
    private class MessageDequeuer extends Thread {
        public void run() {
            if (Trace.DES)
                Trace.out("DES: Message dequeuer started");
            Object msg;
            try {
                do {
                    if (isInterrupted())
                        break; // XXX Awful. why not checked by Receive() ?
                    msg = chan.receive(0);
                    if (msg == null)
                        continue;
                    else if (msg instanceof Message)
                        receive((Message) msg);
                    else if (msg instanceof View)
                        viewAccepted((View) msg);
                    else if (msg instanceof SuspectEvent);
                    else if (TraceCarol.isInfoCmiCarol())
                        TraceCarol.infoCmiCarol(
                            "DES: Received but not supported : "
                                + msg.getClass());
                } while (true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (Trace.DES)
                Trace.out("DES: Message dequeuer finished.");
        }
    }

    private static String chooseBindAddress2(
        String groupname_or_ip,
        int port) {
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
        long end =
            System.currentTimeMillis()
                + BindAddressChooser.RETRIES * BindAddressChooser.TIMEOUT;
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

    private static String chooseBindAddress() {
        String s = Config.getMulticastItf();
        if (s == null) {
            return null;
        }
        LinkedList l = new LinkedList();
        try {
            InetMask m = new InetMask(s);
            Enumeration enum;
            Class cl;
            Method getInet;
            Object[] obj0 = {
            };
            cl = Class.forName("java.net.NetworkInterface");
            Method meth = cl.getMethod("getNetworkInterfaces", new Class[0]);
            getInet = cl.getMethod("getInetAddresses", new Class[0]);
            enum = (Enumeration) meth.invoke(cl, obj0);
            while (enum.hasMoreElements()) {
                Object o = enum.nextElement();
                Enumeration enum2 = (Enumeration) getInet.invoke(o, obj0);
                while (enum2.hasMoreElements()) {
                    InetAddress a = (InetAddress) enum2.nextElement();
                    if (m.match(a)) {
                        l.add(a);
                    }
                }
            }
        } catch (Exception e) {
            return null;
        }
        if (l.size() != 1) {
            return null;
        }
        return ((InetAddress) l.getFirst()).getHostAddress();
    }

    DistributedEquivSystem()
        throws
            ConfigException,
            ClusterException,
            org.javagroups.ChannelException,
            org.javagroups.ChannelClosedException {
        ClusterIdFactory.generate();
        String mcast_addr = Config.getMulticastAddress();
        int mcast_port = Config.getMulticastPort();
        String bind_addr = chooseBindAddress();
        if (bind_addr != null) {
            bind_addr = ";bind_addr=" + bind_addr;
        } else {
            bind_addr = "";
        }

        chan_props =
            "UDP(mcast_addr="
                + mcast_addr
                + ";mcast_port="
                + mcast_port
                + bind_addr
                + ";ip_ttl=32;"
                + "mcast_send_buf_size=150000;mcast_recv_buf_size=80000):"
                + "PING(num_initial_members=2;timeout=3000):"
            // + "MERGE2(min_interval=1000;max_interval=2000):"
    +"FD(timeout=2000;max_tries=2):"
        + "STABLE:"
        + "NAKACK:"
        + "VERIFY_SUSPECT(timeout=1500):"
        + "UNICAST:"
        + "FRAG(frag_size=4096;down_thread=false;up_thread=false):"
        + "FLUSH:"
        + "GMS:"
        + "VIEW_ENFORCER:"
        + "STATE_TRANSFER:"
        + "QUEUE:";
        //org.javagroups.log.Trace.init();
        groupname = Config.getMulticastGroupName();
        chan = new JChannel(chan_props);
        chan.connect(groupname);
        my_addr = chan.getLocalAddress();

        my_id = ClusterIdFactory.getLocalId();
        if (Trace.DES)
            Trace.out("DES: Cluster ID: " + my_id);
        idmap.put(my_addr, my_id);

        Vector v = new Vector();
        v.add(my_addr);
        view = new View(my_addr, 0, v);

        mdq = new MessageDequeuer();
        mdq.setContextClassLoader(
            Thread.currentThread().getContextClassLoader());
        mdq.start();
        if (Trace.DES)
            Trace.out("DES: sending RequestExportsMsg");
        broadcast(new RequestExportsMsg());
        if (TraceCarol.isInfoCmiCarol())
            TraceCarol.infoCmiCarol(
                "DistributedEquivSystem started on "
                    + Config.getMulticastAddress()
                    + ":"
                    + Config.getMulticastPort()
                    + "/"
                    + Config.getMulticastGroupName()
                    + ", cluster Id "
                    + ClusterIdFactory.getLocalId());
    }

    private void broadcast(Serializable msg) {
        ByteArrayOutputStream outs = new ByteArrayOutputStream();
        try {
            MulticastOutputStream out = new MulticastOutputStream(outs);
            out.writeObject(msg);
            Message m = new Message(null, my_addr, outs.toByteArray());
            chan.send(m);
            if (Trace.DES)
                Trace.out("DES: broadcast sent");
        } catch (Exception e) {
            //TODO
            e.printStackTrace();
        }
    }

    //TODO remove
    public static String idToString(byte id[]) {
        String s = "";
        int i;
        for (i = 0; i < id.length; i++) {
            int n = id[i];
            if (n < 0)
                n += 256;
            if (i > 0)
                s += "-";
            s += n;
        }
        return s;
    }

    private void viewAccepted(View v2) {
        if (Trace.DES)
            Trace.out("DES: New view accepted : " + v2);
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
                if (a2 != null)
                    d = a1.compareTo(a2);
                else
                    d = -1;
            } else {
                if (a2 == null)
                    break;
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
            ClusterId id = (ClusterId) idmap.get(a);
            if (id != null)
                globalExports.zapExports(id);
            idmap.remove(a);
            if (Trace.DES) {
                if (id == null)
                    Trace.out("DES: Member " + a + " removed");
                else
                    Trace.out(
                        "DES: Member "
                            + a
                            + " removed (server id : "
                            + id
                            + ")");
            }
        }

        if (newMembers.size() > 0) {
            // Now done when receiving RequestExportsMsg
            /*
                	    if (Trace.DES) Trace.out("sending local exports");
            	    try {
            	    	Thread.sleep(3000);
            	    } catch (Exception e) {
            	    	e.printStackTrace();
            	    }
            	    broadcast(new ExportsMsg(my_id, localExports));
            */
        }

        if (Trace.DES) {
            while (newMembers.size() > 0) {
                Address a = (Address) newMembers.removeFirst();
                Trace.out("DES: New member " + a);
            }
        }
    }

    private ClusterId checkServer(ClusterId id, Address ad) {
        // Check if this server is allowed in the group ?
        ClusterId i = (ClusterId) idmap.get(ad);
        if (i == null) {
            i = id;
            idmap.put(ad, id);
            return id;
        } else if (i.equals(id)) {
            return id;
        }
        if (Trace.DES)
            Trace.out("DES: Message ignored (server rejected)");
        return null;
    }

    private boolean self(ClusterId id) {
        return my_id.equals(id);
    }

    private void receive(Message m) {
        Object o;
        byte[] buf = m.getBuffer();
        if (buf == null) {
            //TODO message
            if (Trace.DES)
                Trace.out("buf == null");
            o = null;
        } else {
            try {
                ByteArrayInputStream in_stream = new ByteArrayInputStream(buf);
                MulticastInputStream in = new MulticastInputStream(in_stream);
                o = in.readObject();
            } catch (Exception e) {
                //TODO message
                if (Trace.DES)
                    Trace.out(e.toString());
                o = null;
            }
        }

        Address from = m.getSrc();
        //        if (o instanceof ExportsMsg) {
        //            ExportsMsg rm = (ExportsMsg) o;
        //            ClusterId id = checkServer(rm.i, from);
        //            if (id == null)
        //                return;
        //            if (Trace.DES)
        //                Trace.out(
        //                    "DES: Received exports from server " + from + " " + m);
        //            if (!self(id)) {
        //                globalExports.addExports(id, rm.getMap());
        //                if (Trace.DES)
        //                    Trace.out("DES: Exports added (" + from + ")");
        //            }
        //        } else
        if (o instanceof ExportMsg) {
            ExportMsg pm = (ExportMsg) o;
            ClusterId id = checkServer(pm.i, from);
            if (id == null)
                return;
            if (Trace.DES)
                Trace.out(
                    "DES: Put message received from server "
                        + from
                        + ", ID : "
                        + pm.k);
            if (!self(id)) {
                try {
                    Remote stub = pm.stub;
                    if (stub != null)
                        globalExports.put(id, pm.k, stub);
                } catch (RemoteException e) {
                }
            }
        } else if (o instanceof UnexportMsg) {
            UnexportMsg rm = (UnexportMsg) o;
            ClusterId id = checkServer(rm.i, from);
            if (id == null)
                return;
            if (Trace.DES)
                Trace.out(
                    "DES: Remove message received from server "
                        + from
                        + ", ID : "
                        + rm.k);
            if (!self(id))
                globalExports.remove(id, rm.k);
        } else if (o instanceof RequestExportsMsg) {
            if (Trace.DES)
                Trace.out("sending local exports");

            HashMap h = localExports.getmap();
            Iterator i = h.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry e = (Map.Entry) i.next();
                broadcast(
                    new ExportMsg(
                        my_id,
                        (String) e.getKey(),
                        (Remote) e.getValue()));
            }

            //	    broadcast(new ExportsMsg(my_id, localExports));
        } else if (Trace.DES) {
            Trace.out(
                "DES: Message of unknown type received from server " + from);
        }
    }

    void terminate() {
        mdq.interrupt();
    }

    /*
     * DistributedExports interface
     */
    boolean exportObject(Serializable key, Remote obj) throws RemoteException {
        if (Trace.DES)
            Trace.out(
                "DES: exportObject("
                    + key
                    + ", "
                    + obj.getClass().getName()
                    + ")");
        synchronized (localExports) {
            Object cur = localExports.get(key);
            if (cur != null)
                return false;
            localExports.put(key, obj);
            globalExports.put(my_id, key, obj);
        }
        broadcast(new ExportMsg(my_id, key, obj));
        return true;
    }

    boolean unexportObject(Serializable key) {
        if (Trace.DES)
            Trace.out("DES: unexportObject(" + key + ")");
        synchronized (localExports) {
            Object cur = localExports.get(key);
            if (cur == null)
                return false;
            localExports.remove(key);
            globalExports.remove(my_id, key);
        }
        broadcast(new UnexportMsg(my_id, key));
        return true;
    }

    /**
     * Get a cluster stub (stub of all equivalent objects).
     * @return <code>null<code> if not exported.
     */
    ClusterStub getGlobal(Serializable key) throws RemoteException {
        if (Trace.DES)
            Trace.out("DES: getGlobal(" + key + ")");

        ClusterStub cs;
        cs = globalExports.getClusterStub(key);
        //XXX should apply the stub filter for objects returned to clients
        return cs;
    }

    /**
     * Get only the standard stub registered in this instance of the DES.
     * @return <code>null<code> if not exported.
     */
    Remote getLocal(Serializable key) {
        if (Trace.DES)
            Trace.out("DES: getLocal(" + key + ")");
        return (Remote) localExports.get(key);
    }
}
