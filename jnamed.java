import java.lang.reflect.*;
import java.io.*;
import java.net.*;
import java.util.*;
import DNS.*;
import DNS.utils.*;

public class jnamed {

Cache cache;
Hashtable znames;
Hashtable TSIGs;

public
jnamed(String conffile) throws IOException {
	FileInputStream fs;
	try {
		fs = new FileInputStream(conffile);
	}
	catch (Exception e) {
		System.out.println("Cannot open " + conffile);
		return;
	}

	cache = null;
	znames = new Hashtable();
	TSIGs = new Hashtable();

	BufferedReader br = new BufferedReader(new InputStreamReader(fs));
	String line = null;
	while ((line = br.readLine()) != null) {
		StringTokenizer st = new StringTokenizer(line);
		if (!st.hasMoreTokens())
			continue;
		String keyword = st.nextToken();
		if (!st.hasMoreTokens()) {
			System.out.println("Invalid line: " + line);
			continue;
		}
		if (keyword.equals("primary"))
			addZone(st.nextToken());
		else if (keyword.equals("cache"))
			cache = new Cache(st.nextToken());
		else if (keyword.equals("key"))
			addTSIG(st.nextToken(), st.nextToken());

	}

	if (cache == null) {
		System.out.println("no cache specified");
		System.exit(-1);
	}
	addUDP((short)12345);
	addTCP((short)12345);
};

public void
addZone(String zonefile) throws IOException {
	Zone newzone = new Zone(zonefile, cache);
	znames.put(newzone.getOrigin(), newzone);
/*System.out.println("Adding zone named <" + newzone.getOrigin() + ">");*/
};

public void
addTSIG(String name, String key) {
	TSIGs.put(new Name(name), base64.fromString(key));
}

public Zone
findBestZone(Name name) {
	Zone foundzone = null;
	Name tname = name;
	do {
/*System.out.println("Looking for zone named <" + tname + ">");*/
		foundzone = (Zone) znames.get(tname);
		if (foundzone != null)
			return foundzone;
		tname = new Name(tname, 1);
	} while (!tname.equals(Name.root));
	return null;
}

public RRset
findExactMatch(Name name, short type, short dclass, boolean glue) {
	Zone zone = findBestZone(name);
	if (zone != null)
		return zone.findExactMatch(name, type);
	else {
		RRset [] rrsets;
		if (glue)
			rrsets = cache.findAnyRecords(name, type, dclass);
		else 
			rrsets = cache.findRecords(name, type, dclass);
		if (rrsets == null)
			return null;
		else
			return rrsets[0]; /* not quite right */
	}
}

void
addRRset(Message response, RRset rrset) {
	Enumeration e = rrset.rrs();
	while (e.hasMoreElements()) {
		Record r = (Record) e.nextElement();
		response.addRecord(r, Section.ANSWER);
	}
}


void
addAuthority(Message response, Name name, Zone zone) {
	if (response.getHeader().getCount(Section.ANSWER) > 0 || zone == null) {
		RRset nsRecords = findExactMatch(name, Type.NS, DClass.IN,
						 false);
		if (nsRecords == null) {
			if (zone != null)
				nsRecords = zone.getNS();
			else {
				RRset [] rrsets;
				rrsets = cache.findRecords(Name.root, Type.NS,
							   DClass.IN);
				if (rrsets == null)
					nsRecords = null;
				else
					nsRecords = rrsets[0];
			}
		}
		if (nsRecords == null)
			return;
		Enumeration e = nsRecords.rrs();
		while (e.hasMoreElements()) {
			Record r = (Record) e.nextElement();
			if (response.findRecord(r, Section.ANSWER) == false)
				response.addRecord(r, Section.AUTHORITY);
		}
	}
	else {
		SOARecord soa = (SOARecord) zone.getSOA();
		response.addRecord(soa, Section.AUTHORITY);
	}
}

private void
addGlue(Message response, Name name) {
	RRset a = findExactMatch(name, Type.A, DClass.IN, true);
	Enumeration e = a.rrs();
	while (e.hasMoreElements()) {
			Record r = (Record) e.nextElement();
			if (response.findRecord(r) == false)
				response.addRecord(r, Section.ADDITIONAL);
	}
}

private void
addAdditional2(Message response, int section) {
	Enumeration e = response.getSection(section);
	while (e.hasMoreElements()) {
		Record r = (Record) e.nextElement();
		try {
			Method m = r.getClass().getMethod("getTarget", null);
			Name glueName = (Name) m.invoke(r, null);
			addGlue(response, glueName);
		}
		catch (Exception ex) {
		}
	}
	
}

void
addAdditional(Message response) {
	addAdditional2(response, Section.ANSWER);
	addAdditional2(response, Section.AUTHORITY);
}

TSIG
findTSIG(Name name) {
	byte [] key = (byte []) TSIGs.get(name);
	if (key != null)
		return new TSIG(name.toString(), key);
	else
		return null;
}

Message
generateReply(Message query, byte [] in, int maxLength) {
	if (query.getHeader().getOpcode() != Opcode.QUERY)
		return notimplMessage(query);
	Record queryRecord = query.getQuestion();

	TSIGRecord queryTSIG = query.getTSIG();
	TSIG tsig = null;
	if (queryTSIG != null) {
		tsig = findTSIG(queryTSIG.getName());
		if (!tsig.verify(query, in, null))
			return formerrMessage(in);
	}
	Message response = new Message();
	response.getHeader().setID(query.getHeader().getID());
	response.getHeader().setFlag(Flags.AA);
	response.getHeader().setFlag(Flags.QR);
	response.addRecord(queryRecord, Section.QUESTION);

	Name name = queryRecord.getName();
	short type = queryRecord.getType();
	short dclass = queryRecord.getDClass();
	Zone zone = findBestZone(name);
	if (zone != null) {
		ZoneResponse zr = zone.findRecords(name, type);
		if (zr.isNXDOMAIN())
			response.getHeader().setRcode(Rcode.NXDOMAIN);
		Vector backtrace = zr.backtrace();
		if (backtrace != null) {
			Enumeration e = backtrace.elements();
			while (e.hasMoreElements()) {
				Record cname = (Record)e.nextElement();
				response.addRecord(cname, Section.ANSWER);
			}
		}
		if (zr.isSuccessful()) {
			RRset [] rrsets = zr.answers();
			for (int i = 0; i < rrsets.length; i++)
				addRRset(response, rrsets[i]);
		}
	}
	else {
		CacheResponse cr;
		cr = cache.lookupRecords(name, type, dclass,
					 Credibility.NONAUTH_ANSWER);
		Vector backtrace = cr.backtrace();
		if (backtrace != null) {
			Enumeration e = backtrace.elements();
			while (e.hasMoreElements()) {
				Record cname = (Record)e.nextElement();
				response.addRecord(cname,
						   Section.ANSWER);
			}
			if (!cr.isSuccessful())
				response.getHeader().setRcode(Rcode.NXDOMAIN);
		}
		if (cr.isSuccessful()) {
			RRset [] rrsets = cr.answers();
			for (int i = 0; i < rrsets.length; i++)
				addRRset(response, rrsets[i]);
		}
	}
	addAuthority(response, name, zone);
	addAdditional(response);
	if (queryTSIG != null) {
		try {
			if (tsig != null)
				tsig.apply(response, queryTSIG);
		}
		catch (IOException e) {
		}
	}
	try {
		byte [] out = response.toWire();
		if (out.length > maxLength) {
			truncate(response, out.length, maxLength);
			if (tsig != null)
				tsig.apply(response, queryTSIG);
		}
	}
	catch (IOException e) {
	}
	return response;
}

public int
truncateSection(Message in, int maxLength, int length, int section) {
	int removed = 0;
	Record [] records = in.getSectionArray(section);
	for (int i = records.length - 1; i >= 0; i--) {
		Record r = records[i];
		removed += r.getWireLength();
		length -= r.getWireLength();
		in.removeRecord(r, section);
		if (length > maxLength)
			continue;
		else {
			for (int j = i - 1; j >= 0; j--) {
				Record r2 = records[j];
				if (!r.getName().equals(r2.getName()) ||
				    r.getType() != r2.getType() ||
				    r.getDClass() != r2.getDClass())
					break;
				removed += r2.getWireLength();
				length -= r2.getWireLength();
				in.removeRecord(r2, section);
			}
			return removed;
		}
	}
	return removed;
}

public void
truncate(Message in, int length, int maxLength) {
	TSIGRecord tsig = in.getTSIG();
	if (tsig != null)
		maxLength -= tsig.getWireLength();

	length -= truncateSection(in, maxLength, length, Section.ADDITIONAL);
	if (length < maxLength)
		return;

	in.getHeader().setFlag(Flags.TC);
	if (tsig != null) {
		in.removeAllRecords(Section.ANSWER);
		in.removeAllRecords(Section.AUTHORITY);
		return;
	}
	length -= truncateSection(in, maxLength, length, Section.AUTHORITY);
	if (length < maxLength)
		return;
	length -= truncateSection(in, maxLength, length, Section.ANSWER);
}

public Message
formerrMessage(byte [] in) {
	Header header;
	try {
		header = new Header(new DataByteInputStream(in));
	}
	catch (IOException e) {
		header = new Header(0);
	}
	Message response = new Message();
	response.setHeader(header);
	for (int i = 0; i < 4; i++)
		response.removeAllRecords(i);
	header.setRcode(Rcode.FORMERR);
	return response;
}

public Message
notimplMessage(Message query) {
	Header header = query.getHeader();
	Message response = new Message();
	response.setHeader(header);
	for (int i = 0; i < 4; i++)
		response.removeAllRecords(i);
	header.setRcode(Rcode.NOTIMPL);
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
			Message query, response;
			try {
				query = new Message(in);
				response = generateReply(query, in, 65535);
			}
			catch (IOException e) {
				response = formerrMessage(in);
			}
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
			short udpLength = 512;
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
			Message query, response;
			try {
				query = new Message(in);
				OPTRecord opt = query.getOPT();
				if (opt != null)
					udpLength = opt.getPayloadSize();

				response = generateReply(query, in, udpLength);
			}
			catch (IOException e) {
				response = formerrMessage(in);
			}
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
	t = new Thread(new Runnable() {public void run() {serveTCP(port);}});
	t.start();
}

public void
addUDP(final short port) {
	Thread t;
	t = new Thread(new Runnable() {public void run() {serveUDP(port);}});
	t.start();
}

public static void main(String [] args) {
	if (args.length != 1) {
		System.out.println("usage: server conf");
		System.exit(0);	
	}
	jnamed s;
	try {
		s = new jnamed(args[0]);
	}
	catch (IOException e) {
		System.out.println(e);
	}
}

}
