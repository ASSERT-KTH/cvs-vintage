// Copyright (c) 1999 Brian Wellington (bwelling@xbill.org)
// Portions Copyright (c) 1999 Network Associates, Inc.

package org.xbill.DNS;

import java.net.*;
import java.io.*;
import org.xbill.DNS.utils.*;

/**
 * Address Record - maps a domain name to an Internet address
 *
 * @author Brian Wellington
 */

public class ARecord extends Record {

private static ARecord member = new ARecord();

private int addr;

private
ARecord() {}

private
ARecord(Name name, int dclass, long ttl) {
	super(name, Type.A, dclass, ttl);
}

static ARecord
getMember() {
	return member;
}

private static final int
fromBytes(byte b1, byte b2, byte b3, byte b4) {
	return (((b1 & 0xFF) << 24) |
		((b2 & 0xFF) << 16) |
		((b3 & 0xFF) << 8) |
		(b4 & 0xFF));
}

private static final int
fromArray(byte [] array) {
	return (fromBytes(array[0], array[1], array[2], array[3]));
}

private static final String
toDottedQuad(int addr) {
	StringBuffer sb = new StringBuffer();
	sb.append(((addr >>> 24) & 0xFF));
	sb.append(".");
	sb.append(((addr >>> 16) & 0xFF));
	sb.append(".");
	sb.append(((addr >>> 8) & 0xFF));
	sb.append(".");
	sb.append((addr & 0xFF));
	return sb.toString();
}

/**
 * Creates an A Record from the given data
 * @param address The address that the name refers to
 */
public
ARecord(Name name, int dclass, long ttl, InetAddress address) {
	this(name, dclass, ttl);
	addr = fromArray(address.getAddress());
}

Record
rrFromWire(Name name, int type, int dclass, long ttl, int length,
	   DataByteInputStream in)
throws IOException
{
	ARecord rec = new ARecord(name, dclass, ttl);

	if (in == null)
		return rec;

	byte b1 = in.readByte();
	byte b2 = in.readByte();
	byte b3 = in.readByte();
	byte b4 = in.readByte();
	rec.addr = fromBytes(b1, b2, b3, b4);
	return rec;
}

Record
rdataFromString(Name name, int dclass, long ttl, Tokenizer st, Name origin)
throws IOException
{
	ARecord rec = new ARecord(name, dclass, ttl);
	String s = st.getString();
	try {
		InetAddress address;
		if (s.equals("@me@")) {
			address = InetAddress.getLocalHost();
			if (address.equals(InetAddress.getByName("127.0.0.1")))
			{
				String msg = "InetAddress.getLocalHost() is " +
					     "broken.  Don't use @me@.";
				throw new RuntimeException(msg);
			}
			rec.addr = fromArray(address.getAddress());
		}
	}
	catch (UnknownHostException e) {
		throw st.exception("invalid address");
	}

	int [] addr = Address.toArray(s);
	if (addr == null)
		throw st.exception("invalid dotted quad");
	rec.addr = fromBytes((byte)addr[0], (byte)addr[1], (byte)addr[2],
			     (byte)addr[3]);
	return rec;
}

/** Converts rdata to a String */
public String
rdataToString() {
	return (toDottedQuad(addr));
}

/** Returns the Internet address */
public InetAddress
getAddress() {
	String s = toDottedQuad(addr);
	try {
		return InetAddress.getByName(s);
	}
	catch (UnknownHostException e) {
		return null;
	}
}

void
rrToWire(DataByteOutputStream out, Compression c, boolean canonical) {
	out.writeByte(((addr >>> 24) & 0xFF));
	out.writeByte(((addr >>> 16) & 0xFF));
	out.writeByte(((addr >>> 8) & 0xFF));
	out.writeByte((addr & 0xFF));
}

}
