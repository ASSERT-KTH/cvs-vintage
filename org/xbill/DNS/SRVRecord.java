// Copyright (c) 1999 Brian Wellington (bwelling@xbill.org)
// Portions Copyright (c) 1999 Network Associates, Inc.

package org.xbill.DNS;

import java.io.*;
import java.util.*;
import org.xbill.DNS.utils.*;

/**
 * Server Selection Record  - finds hosts running services in a domain.  An
 * SRV record will normally be named <service>.<protocol>.domain - an
 * example would be http.tcp.example.com (if HTTP used SRV records)
 *
 * @author Brian Wellington
 */

public class SRVRecord extends Record {

private static SRVRecord member = new SRVRecord();

private short priority, weight, port;
private Name target;

private
SRVRecord() {}

private
SRVRecord(Name name, short dclass, int ttl) {
	super(name, Type.SRV, dclass, ttl);
}

static SRVRecord
getMember() {
	return member;
}

/**
 * Creates an SRV Record from the given data
 * @param priority The priority of this SRV.  Records with lower priority
 * are preferred.
 * @param weight The weight, used to select between records at the same
 * priority.
 * @param port The TCP/UDP port that the service uses
 * @param target The host running the service
 */
public
SRVRecord(Name _name, short _dclass, int _ttl, int _priority,
	  int _weight, int _port, Name _target)
{
	super(_name, Type.SRV, _dclass, _ttl);
	priority = (short) _priority;
	weight = (short) _weight;
	port = (short) _port;
	target = _target;
}

Record
rrFromWire(Name name, short type, short dclass, int ttl, int length,
	   DataByteInputStream in)
throws IOException
{
	SRVRecord rec = new SRVRecord(name, dclass, ttl);
	if (in == null)
		return rec;
	rec.priority = (short) in.readUnsignedShort();
	rec.weight = (short) in.readUnsignedShort();
	rec.port = (short) in.readUnsignedShort();
	rec.target = new Name(in);
	return rec;
}

Record
rdataFromString(Name name, short dclass, int ttl, MyStringTokenizer st,
		Name origin)
throws TextParseException
{
	SRVRecord rec = new SRVRecord(name, dclass, ttl);
	rec.priority = Short.parseShort(st.nextToken());
	rec.weight = Short.parseShort(st.nextToken());
	rec.port = Short.parseShort(st.nextToken());
	rec.target = Name.fromString(st.nextToken(), origin);
	return rec;
}

/** Converts rdata to a String */
public String
rdataToString() {
	StringBuffer sb = new StringBuffer();
	if (target != null) {
		sb.append(priority);
		sb.append(" ");
		sb.append(weight);
		sb.append(" ");
		sb.append(port);
		sb.append(" ");
		sb.append(target);
	}
	return sb.toString();
}

/** Returns the priority */
public short
getPriority() {
	return priority;
}

/** Returns the weight */
public short
getWeight() {
	return weight;
}

/** Returns the port that the service runs on */
public short
getPort() {
	return port;
}

/** Returns the host running that the service */
public Name
getTarget() {
	return target;
}

void
rrToWire(DataByteOutputStream out, Compression c) throws IOException {
	if (target == null)
		return;

	out.writeShort(priority);
	out.writeShort(weight);
	out.writeShort(port);
	target.toWire(out, null);
}

void
rrToWireCanonical(DataByteOutputStream out) throws IOException {
	if (target == null)
		return;

	out.writeShort(priority);
	out.writeShort(weight);
	out.writeShort(port);
	target.toWireCanonical(out);
}

}
