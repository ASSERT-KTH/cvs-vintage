// Copyright (c) 1999 Brian Wellington (bwelling@xbill.org)
// Portions Copyright (c) 1999 Network Associates, Inc.

package org.xbill.DNS;

import java.util.*;
import java.io.*;
import java.net.*;
import org.xbill.Task.*;

/**
 * An implementation of Resolver that can send queries to multiple servers,
 * sending the queries multiple times if necessary.
 * @see Resolver
 *
 * @author Brian Wellington
 */

public class ExtendedResolver implements Resolver {

class QElement {
	Object obj;
	int res;

	public
	QElement(Object _obj, int _res) {
		obj = _obj;
		res = _res;
	}
}

class Receiver implements ResolverListener {
	Vector queue;
	Hashtable idMap;

	public
	Receiver(Vector _queue, Hashtable _idMap) {
		queue = _queue;
		idMap = _idMap;
	}

	public void
	enqueueInfo(int id, Object obj) {
		Integer ID, R;
		int r;
		synchronized (idMap) {
			ID = new Integer(id);
			R = (Integer)idMap.get(ID);
			if (R == null)
				return;
			r = R.intValue();
			idMap.remove(ID);
		}
		synchronized (queue) {
			QElement qe = new QElement(obj, r);
			queue.addElement(qe);
			queue.notify();
		}
	}


	public void
	receiveMessage(int id, Message m) {
		enqueueInfo(id, m);
	}

	public void
	handleException(int id, Exception e) {
		System.out.println("got an exception: " + e);
		enqueueInfo(id, e);
	}
}

private static final int quantum = 30;
private static final byte retries = 3;
private Vector resolvers;

private void
init() {
	resolvers = new Vector();
}

/**
 * Creates a new Extended Resolver.  FindServer is used to locate the servers
 * for which SimpleResolver contexts should be initialized.
 * @see SimpleResolver
 * @see FindServer
 * @exception UnknownHostException Failure occured initializing SimpleResolvers
 */
public
ExtendedResolver() throws UnknownHostException {
	init();
	String [] servers = FindServer.servers();
	if (servers != null) {
		for (int i = 0; i < servers.length; i++) {
			Resolver r = new SimpleResolver(servers[i]);
			r.setTimeout(quantum);
			resolvers.addElement(r);
		}
	}
	else
		resolvers.addElement(new SimpleResolver());
}

/**
 * Creates a new Extended Resolver
 * @param servers  An array of server names for which SimpleResolver
 * contexts should be initialized.
 * @see SimpleResolver
 * @exception UnknownHostException Failure occured initializing SimpleResolvers
 */
public
ExtendedResolver(String [] servers) throws UnknownHostException {
	init();
	for (int i = 0; i < servers.length; i++) {
		Resolver r = new SimpleResolver(servers[i]);
		r.setTimeout(quantum);
		resolvers.addElement(r);
	}
}

/**
 * Creates a new Extended Resolver
 * @param res An array of pre-initialized Resolvers is provided.
 * @see SimpleResolver
 * @exception UnknownHostException Failure occured initializing SimpleResolvers
 */
public
ExtendedResolver(Resolver [] res) throws UnknownHostException {
	init();
	for (int i = 0; i < res.length; i++)
		resolvers.addElement(res[i]);
}

private void
sendTo(Message query, Receiver receiver, Hashtable idMap, int r) {
	Resolver res = (Resolver) resolvers.elementAt(r);
	synchronized (idMap) {
		int id = res.sendAsync(query, receiver);
		idMap.put(new Integer(id), new Integer(r));
	}
}

/** Sets the port to communicate with on the servers */
public void
setPort(int port) {
	for (int i = 0; i < resolvers.size(); i++)
		((Resolver)resolvers.elementAt(i)).setPort(port);
}

/** Sets whether TCP connections will be sent by default */
public void
setTCP(boolean flag) {
	for (int i = 0; i < resolvers.size(); i++)
		((Resolver)resolvers.elementAt(i)).setTCP(flag);
}

/** Sets whether truncated responses will be returned */
public void
setIgnoreTruncation(boolean flag) {
	for (int i = 0; i < resolvers.size(); i++)
		((Resolver)resolvers.elementAt(i)).setIgnoreTruncation(flag);
}

/** Sets the EDNS version used on outgoing messages (only 0 is meaningful) */
public void
setEDNS(int level) {
	for (int i = 0; i < resolvers.size(); i++)
		((Resolver)resolvers.elementAt(i)).setEDNS(level);
}

/** Specifies the TSIG key that messages will be signed with */
public void
setTSIGKey(String name, String key) {
	for (int i = 0; i < resolvers.size(); i++)
		((Resolver)resolvers.elementAt(i)).setTSIGKey(name, key);
}

/**
 * Specifies the TSIG key (with the same name as the local host) that messages
 * will be signed with
 */
public void
setTSIGKey(String key) {
	for (int i = 0; i < resolvers.size(); i++)
		((Resolver)resolvers.elementAt(i)).setTSIGKey(key);
}

/** Sets the amount of time to wait for a response before giving up */
public void
setTimeout(int secs) {
	for (int i = 0; i < resolvers.size(); i++)
		((Resolver)resolvers.elementAt(i)).setTimeout(secs);
}

/**
 * Sends a message, and waits for a response.  Multiple servers are queried,
 * and queries are sent multiple times until either a successful response
 * is received, or it is clear that there is no successful response.
 * @return The response
 */
public Message
send(Message query) throws IOException {
	int q, r;
	Message best = null;
	IOException bestException = null;
	boolean [] invalid = new boolean[resolvers.size()];
	byte [] sent = new byte[resolvers.size()];
	Vector queue = new Vector();
	Hashtable idMap = new Hashtable();
	Receiver receiver = new Receiver(queue, idMap);

	while (true) {
		Message m;
		boolean waiting = false;
		QElement qe;
		synchronized (queue) {
			for (r = 0; r < resolvers.size(); r++) {
				if (sent[r] == 0) {
					sendTo(query, receiver, idMap, r);
					sent[r]++;
					waiting = true;
					break;
				}
				if (!invalid[r] && sent[r] < retries)
					waiting = true;
			}
			if (!waiting)
				break;

			try {
				queue.wait();
			}
			catch (InterruptedException e) {
			}
			if (queue.size() == 0)
				continue;
			qe = (QElement) queue.firstElement();
			queue.removeElement(qe);
			if (qe.obj instanceof Message)
				m = (Message) qe.obj;
			else
				m = null;
			r = qe.res;
		}
		if (m == null) {
			IOException e = (IOException) qe.obj;
			if (!(e instanceof InterruptedIOException))
				invalid[r] = true;
			if (bestException == null)
				bestException = e;
		}
		else {
			byte rcode = m.getHeader().getRcode();
			if (rcode == Rcode.NOERROR)
				return m;
			else {
				if (best == null)
					best = m;
				else {
					byte bestrcode;
					bestrcode = best.getHeader().getRcode();
					if (rcode == Rcode.NXDOMAIN &&
					    bestrcode != Rcode.NXDOMAIN)
						best = m;
				}
				invalid[r] = true;
			}
		}
	}
	if (best != null)
		return best;
	throw bestException;
}

private int
uniqueID(Message m) {
	Record r = m.getQuestion();
	return (((r.getName().hashCode() & 0xFFFF) << 16) +
		(r.getType() << 8) +
		(hashCode() & 0xFF));
}

/**
 * Asynchronously sends a message, registering a listener to receive a callback
 * Multiple asynchronous lookups can be performed in parallel.
 * @return An identifier
 */
public int
sendAsync(final Message query, final ResolverListener listener) {
	final int id = uniqueID(query);
	String name = this.getClass() + ": " + query.getQuestion().getName();
	WorkerThread.assignThread(new ResolveThread(this, query, id, listener),
				  name);
	return id;
}

/**
 * Sends a zone transfer message to the first known server, and waits for a
 * response.  This should be further tuned later.
 * @return The response
 */
public
Message sendAXFR(Message query) throws IOException {
	return ((Resolver)resolvers.elementAt(0)).sendAXFR(query);
}

/** Returns the i'th resolver used by this ExtendedResolver */
public Resolver
getResolver(int i) {
	if (i < resolvers.size())
		return (Resolver)resolvers.elementAt(i);
	return null;
}

/** Returns all resolvers used by this ExtendedResolver */
public Resolver []
getResolvers() {
	Resolver [] res = new Resolver[resolvers.size()];
	for (int i = 0; i < resolvers.size(); i++)
		res[i] = (Resolver) resolvers.elementAt(i);
	return res;
}

/** Adds a new resolver to be used by this ExtendedResolver */
public void
addResolver(Resolver r) {
	resolvers.addElement(r);
}

/** Deletes a resolver used by this ExtendedResolver */
public void
deleteResolver(Resolver r) {
	resolvers.removeElement(r);
}

}
