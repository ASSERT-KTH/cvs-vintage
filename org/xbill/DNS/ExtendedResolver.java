// Copyright (c) 1999 Brian Wellington (bwelling@xbill.org)
// Portions Copyright (c) 1999 Network Associates, Inc.

package DNS;

import java.util.*;
import java.io.*;
import java.net.*;

public class ExtendedResolver implements Resolver {

class QElement {
	Message m;
	int res;

	public
	QElement(Message _m, int _res) {
		m = _m;
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
	receiveMessage(int id, Message m) {
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
			QElement qe = new QElement(m, r);
			queue.addElement(qe);
			queue.notify();
		}
	}
}

static final int quantum = 20;

private Vector resolvers;

private void
init() {
	resolvers = new Vector();
}

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

public
ExtendedResolver(String [] servers) throws UnknownHostException {
	init();
	for (int i = 0; i < servers.length; i++) {
		Resolver r = new SimpleResolver(servers[i]);
		r.setTimeout(quantum);
		resolvers.addElement(r);
	}
}

public
ExtendedResolver(Resolver [] res) throws UnknownHostException {
	init();
	for (int i = 0; i < res.length; i++)
		resolvers.addElement(res[i]);
}

boolean
sendTo(Message query, Receiver receiver, Hashtable idMap, int r, int q) {
	q -= r;
	Resolver res = (Resolver) resolvers.elementAt(r);
	/* Three retries */
	if (q >= 0 && q < 3) {
		synchronized (idMap) {
			int id = res.sendAsync(query, receiver);
			idMap.put(new Integer(id), new Integer(r));
		}
	}
	if (q < 6)
		return true;
	return false;
}

public void
setPort(int port) {
	for (int i = 0; i < resolvers.size(); i++)
		((Resolver)resolvers.elementAt(i)).setPort(port);
}

public void
setTCP(boolean flag) {
	for (int i = 0; i < resolvers.size(); i++)
		((Resolver)resolvers.elementAt(i)).setTCP(flag);
}

public void
setIgnoreTruncation(boolean flag) {
	for (int i = 0; i < resolvers.size(); i++)
		((Resolver)resolvers.elementAt(i)).setIgnoreTruncation(flag);
}

public void
setEDNS(int level) {
	for (int i = 0; i < resolvers.size(); i++)
		((Resolver)resolvers.elementAt(i)).setEDNS(level);
}

public void
setTSIGKey(String name, String key) {
	for (int i = 0; i < resolvers.size(); i++)
		((Resolver)resolvers.elementAt(i)).setTSIGKey(name, key);
}

public void
setTSIGKey(String key) {
	for (int i = 0; i < resolvers.size(); i++)
		((Resolver)resolvers.elementAt(i)).setTSIGKey(key);
}

public void
setTimeout(int secs) {
	for (int i = 0; i < resolvers.size(); i++)
		((Resolver)resolvers.elementAt(i)).setTimeout(secs);
}

public Message
send(Message query) {
	int q, r;
	Message best = null;
	boolean [] invalid = new boolean[resolvers.size()];
	Vector queue = new Vector();
	Hashtable idMap = new Hashtable();
	Receiver receiver = new Receiver(queue, idMap);

	for (q = 0; q < 20; q++) {
		Message m;
		boolean ok = false;
		synchronized (queue) {
			for (r = 0; r < resolvers.size(); r++)
				if (!invalid[r])
					ok |= sendTo(query, receiver, idMap,
						     r, q);
			if (!ok)
				break;
			try {
				queue.wait(quantum * 1000);
			}
			catch (InterruptedException e) {
			}
			if (queue.size() == 0)
				continue;
			QElement qe = (QElement) queue.firstElement();
			queue.removeElement(qe);
			m = qe.m;
			r = qe.res;
		}
		if (m == null)
			invalid[r] = true;
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
	return best;
}

private int
uniqueID(Message m) {
	Record r = m.getQuestion();
	return (((r.getName().hashCode() & 0xFFFF) << 16) +
		(r.getType() << 8) +
		(hashCode() & 0xFF));
}

public int
sendAsync(final Message query, final ResolverListener listener) {
	final int id = uniqueID(query);
	WorkerThread.assignThread(this, query, id, listener);
	return id;
}

public
Message sendAXFR(Message query) {
	return ((Resolver)resolvers.elementAt(0)).sendAXFR(query);
}

public Resolver
getResolver(int i) {
	if (i < resolvers.size())
		return (Resolver)resolvers.elementAt(i);
	return null;
}

public Resolver []
getResolvers() {
	Resolver [] res = new Resolver[resolvers.size()];
	for (int i = 0; i < resolvers.size(); i++)
		res[i] = (Resolver) resolvers.elementAt(i);
	return res;
}

public void
addResolver(Resolver r) {
	resolvers.addElement(r);
}

public void
deleteResolver(Resolver r) {
	resolvers.removeElement(r);
}

}
