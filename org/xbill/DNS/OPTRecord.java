// Copyright (c) 1999-2004 Brian Wellington (bwelling@xbill.org)

package org.xbill.DNS;

import java.io.*;
import java.util.*;
import org.xbill.DNS.utils.*;

/**
 * Options - describes Extended DNS (EDNS) properties of a Message.
 * No specific options are defined other than those specified in the
 * header.  An OPT should be generated by Resolver.
 *
 * EDNS is a method to extend the DNS protocol while providing backwards
 * compatibility and not significantly changing the protocol.  This
 * implementation of EDNS is mostly complete at level 0.
 *
 * @see Message
 * @see Resolver 
 *
 * @author Brian Wellington
 */

public class OPTRecord extends Record {

private static OPTRecord member = new OPTRecord();

private Map options;

private
OPTRecord() {}

private
OPTRecord(Name name, int dclass, long ttl) {
	super(name, Type.OPT, dclass, ttl);
}

static OPTRecord
getMember() {
	return member;
}

/**
 * Creates an OPT Record with no data.  This is normally called by
 * SimpleResolver, but can also be called by a server.
 */
public
OPTRecord(int payloadSize, int xrcode, int version, int flags) {
	this(Name.root, payloadSize, 0);
	checkU16("payloadSize", payloadSize);
	checkU8("xrcode", xrcode);
	checkU8("version", version);
	checkU16("flags", flags);
	ttl = ((long)xrcode << 24) + ((long)version << 16) + flags;
	options = null;
}

/**
 * Creates an OPT Record with no data.  This is normally called by
 * SimpleResolver, but can also be called by a server.
 */
public
OPTRecord(int payloadSize, int xrcode, int version) {
	this(payloadSize, xrcode, version, 0);
}

Record
rrFromWire(Name name, int type, int dclass, long ttl, DNSInput in)
throws IOException
{
	OPTRecord rec = new OPTRecord(name, dclass, ttl);
	if (in == null)
		return rec;
	if (in.remaining() > 0)
		rec.options = new HashMap();
	while (in.remaining() > 0) {
		int code = in.readU16();
		int len = in.readU16();
		rec.options.put(new Integer(code), in.readByteArray(len));
	}
	return rec;
}

Record
rdataFromString(Name name, int dclass, long ttl, Tokenizer st, Name origin)
throws IOException
{
	throw st.exception("no text format defined for OPT");
}

/** Converts rdata to a String */
public String
rdataToString() {
	StringBuffer sb = new StringBuffer();
	sb.append(getName());
	sb.append("\t");
	sb.append(Type.string(getType()));
	if (options != null) {
		Iterator it = options.keySet().iterator();
		while (it.hasNext()) {
			Integer i = (Integer) it.next();
			sb.append(i + " ");
		}
	}
	sb.append(" ; payload ");
	sb.append(getPayloadSize());
	sb.append(", xrcode ");
	sb.append(getExtendedRcode());
	sb.append(", version ");
	sb.append(getVersion());
	sb.append(", flags ");
	sb.append(getFlags());
	return sb.toString();
}

/** Returns the maximum allowed payload size. */
public int
getPayloadSize() {
	return dclass;
}

/**
 * Returns the extended Rcode
 * @see Rcode
 */
public int
getExtendedRcode() {
	return (int)(ttl >>> 24);
}

/** Returns the highest supported EDNS version */
public int
getVersion() {
	return (int)((ttl >>> 16) & 0xFF);
}

/** Returns the EDNS flags */
public int
getFlags() {
	return (int)(ttl & 0xFFFF);
}

void
rrToWire(DataByteOutputStream out, Compression c, boolean canonical) {
	if (options == null)
		return;
	Iterator it = options.keySet().iterator();
	while (it.hasNext()) {
		Integer i = (Integer) it.next();
		out.writeShort(i.intValue());
		byte [] data = (byte []) options.get(i);
		out.writeShort(data.length);
		out.writeArray(data);
	}
}

}
