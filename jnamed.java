import java.io.*;
import java.net.*;
import java.util.*;
import DNS.*;

public class dnsServer {

dnsZone [] zones;
int zcount;
Hashtable znames;

public
dnsServer() {
	zones = new dnsZone[20];
	zcount = 0;
	znames = new Hashtable();
};

public void
addZone(String zonefile) throws IOException {
	dnsZone newzone = new dnsZone(zonefile);
	znames.put(newzone.getOrigin(), newzone);
	zones[zcount++] = newzone;
};

public dnsZone
findBestZone(dnsName name) {
	dnsZone foundzone = null;
	dnsName tname = name;
	do {
		if (tname.equals(dnsName.root))
			return null;
		tname = new dnsName(tname, 1);
		foundzone = (dnsZone) znames.get(tname);
	} while (foundzone == null);
	return foundzone;
}

public dnsMessage
generateReply(dnsMessage query) {
	Enumeration qds = query.getSection(dns.QUESTION);
	dnsRecord queryRecord = (dnsRecord) qds.nextElement();

	dnsMessage response = new dnsMessage();
	response.getHeader().setID(query.getHeader().getID());
	response.getHeader().setFlag(dns.AA);
	response.addRecord(dns.QUESTION, queryRecord);

	dnsName name = queryRecord.getName();
	dnsZone zone = findBestZone(name);
	if (zone == null) {
		response.getHeader().setRcode(dns.SERVFAIL);
	}
	else {
		Vector responseRecords = zone.findName(name);
		if (responseRecords == null) {
			response.getHeader().setRcode(dns.NXDOMAIN);
		}
		else {
			int added = 0;
			Enumeration e = responseRecords.elements();
			while (e.hasMoreElements()) {
				dnsRecord r = (dnsRecord) e.nextElement();
				if (r.getType() == queryRecord.getType() &&
				    r.getDClass() == queryRecord.getDClass())
				{
					response.addRecord(dns.ANSWER, r);
					added++;
				}
			}
		}
	}
	return response;
}

public void
serveTCP(short port) {
	try {
		ServerSocket sock = new ServerSocket(port);
		while (true) {
			Socket s = sock.accept();
			int inLength;
			DataInputStream dataIn;
			DataOutputStream dataOut;
			byte [] in;

			try {
				InputStream is = s.getInputStream();
				dataIn = new DataInputStream(is);
				inLength = dataIn.readUnsignedShort();
				in = new byte[inLength];
				dataIn.readFully(in);
			}
			catch (InterruptedIOException e) {
				s.close();
				continue;
			}
			dnsMessage query = new dnsMessage(in);
			dnsMessage response = generateReply(query);
			byte [] out = response.toWire();
			dataOut = new DataOutputStream(s.getOutputStream());
			dataOut.writeShort(out.length);
			dataOut.write(out);
			s.close();
		}
	}
	catch (IOException e) {
		System.out.println("serveTCP: " + e);
	}
}

public void
serveUDP(short port) {
	try {
		DatagramSocket sock = new DatagramSocket(port);
		while (true) {
			DatagramPacket dp = new DatagramPacket(new byte[512],
							       512);
			try {
				sock.receive(dp);
			}
			catch (InterruptedIOException e) {
				continue;
			}
			byte [] in = new byte[dp.getLength()];
			System.arraycopy(dp.getData(), 0, in, 0, in.length);
			dnsMessage query = new dnsMessage(in);
			dnsMessage response = generateReply(query);
			byte [] out = response.toWire();
			dp = new DatagramPacket(out, out.length,
						dp.getAddress(), dp.getPort());
			sock.send(dp);
		}
	}
	catch (IOException e) {
		System.out.println("serveUDP: " + e);
	}
}

public void
addTCP(final short port) {
	Thread t;
	t = new Thread(new Runnable() {public void run() {serveUDP(port);}});
	t.start();
}

public void
addUDP(final short port) {
	Thread t;
	t = new Thread(new Runnable() {public void run() {serveTCP(port);}});
	t.start();
}

public static void main(String [] args) {
	if (args.length == 0) {
		System.out.println("usage: server zone");
		System.exit(0);	
	}
	dnsServer s;
	try {
		s = new dnsServer();
		for (int i = 0; i < args.length; i++)
			s.addZone(args[i]);
		s.addUDP((short)12345);
		s.addTCP((short)12345);
/*
		while (true) {
			try {
				Thread.sleep(1000);
			}
			catch (InterruptedException e) {
			}
		}
*/
	}
	catch (IOException e) {
		System.out.println(e);
	}
}

}
