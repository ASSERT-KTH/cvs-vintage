import java.util.*;
import java.io.*;
import java.net.*;

public class dnsResolver {

InetAddress addr;
int port = dns.PORT;

public dnsResolver(String hostname) {
	try {
		addr = InetAddress.getByName(hostname);
	}
	catch (UnknownHostException e) {
		System.out.println("Unknown host " + hostname);
		return;
	}
}

public void setPort(int port) {
	this.port = port;
}

private byte [] toBytes(dnsMessage m) throws IOException {
	ByteArrayOutputStream os;

	os = new ByteArrayOutputStream();
	m.toBytes(new DataOutputStream(os));
	return os.toByteArray();
}

private dnsMessage parse(byte [] in) throws IOException {
	ByteArrayInputStream is;

	is = new ByteArrayInputStream(in);
	return new dnsMessage(new CountedDataInputStream(is));
}


public dnsMessage sendTCP(dnsMessage inMessage) throws IOException {
	byte [] out, in;
	Socket s;
	int inLength;
	DataInputStream dataIn;

	try {
		s = new Socket(addr, port);
	}
	catch (SocketException e) {
		System.out.println(e);
		return null;
	}

	out = toBytes(inMessage);
	new DataOutputStream(s.getOutputStream()).writeShort(out.length);
	s.getOutputStream().write(out);

	dataIn = new DataInputStream(s.getInputStream());
	inLength = dataIn.readUnsignedShort();
	in = new byte[inLength];
	dataIn.readFully(in);

	s.close();
	return parse(in);
}

public dnsMessage send(dnsMessage inMessage) throws IOException {
	byte [] out, in;
	dnsMessage outMessage;
	DatagramSocket s;

	try {
		s = new DatagramSocket();
	}
	catch (SocketException e) {
		System.out.println(e);
		return null;
	}

	out = toBytes(inMessage);
	s.send(new DatagramPacket(out, out.length, addr, port));

	in = new byte[512];
	s.receive(new DatagramPacket(in, in.length));
	outMessage = parse(in);
	s.close();
	if (outMessage.getHeader().getFlag(dns.TC))
		return sendTCP(inMessage);
	else
		return outMessage;
}

public dnsMessage sendAXFR(dnsMessage inMessage) throws IOException {
	byte [] out, in;
	Socket s;
	int inLength;
	DataInputStream dataIn;
	int soacount = 0;
	dnsMessage outMessage;

	try {
		s = new Socket(addr, dns.PORT);
	}
	catch (SocketException e) {
		System.out.println(e);
		return null;
	}

	out = toBytes(inMessage);
	new DataOutputStream(s.getOutputStream()).writeShort(out.length);
	s.getOutputStream().write(out);

	outMessage = new dnsMessage();
	outMessage.getHeader().setID(inMessage.getHeader().getID());
	while (true) {
		dataIn = new DataInputStream(s.getInputStream());
		inLength = dataIn.readUnsignedShort();
		in = new byte[inLength];
		dataIn.readFully(in);
		dnsMessage m = parse(in);
		if (m.getHeader().getCount(dns.QUESTION) != 0 ||
		    m.getHeader().getCount(dns.ANSWER) <= 0 ||
		    m.getHeader().getCount(dns.AUTHORITY) != 0 ||
		    m.getHeader().getCount(dns.ADDITIONAL) != 0)
			throw new IOException("Invalid AXFR message");
		Vector v = m.getSection(dns.ANSWER);
		Enumeration e = v.elements();
		while (e.hasMoreElements()) {
			dnsRecord r = (dnsRecord)e.nextElement();
			outMessage.addRecord(dns.ANSWER, r);
			if (r instanceof dnsSOARecord)
				soacount++;
		}
		if (soacount > 1)
			break;
	}

	s.close();
	return outMessage;
}


}
