// Copyright (c) 1999-2004 Brian Wellington (bwelling@xbill.org)

package org.xbill.DNS;

import java.io.*;
import org.xbill.DNS.utils.*;

/**
 * Implements MX and KX records, which have identical formats
 *
 * @author Brian Wellington
 */

abstract class MX_KXRecord extends Record {

protected int priority;
protected Name target;

protected
MX_KXRecord() {}

protected
MX_KXRecord(Name name, int type, int dclass, long ttl) {
	super(name, type, dclass, ttl);
}

public
MX_KXRecord(Name name, int type, int dclass, long ttl, int priority,
	    Name target)
{
	super(name, type, dclass, ttl);
	checkU16("priority", priority);
	this.priority = priority;
	if (!target.isAbsolute())
		throw new RelativeNameException(target);
	this.target = target;
}

void
rrFromWire(DNSInput in) throws IOException {
	if (in == null)
		return;
	priority = in.readU16();
	target = new Name(in);
}

void
rdataFromString(Tokenizer st, Name origin) throws IOException {
	priority = st.getUInt16();
	target = st.getName(origin);
}

/** Converts rdata to a String */
public String
rdataToString() {
	StringBuffer sb = new StringBuffer();
	if (target != null) {
		sb.append(priority);
		sb.append(" ");
		sb.append(target);
	}
	return sb.toString();
}

/** Returns the target of the record */
public Name
getTarget() {
	return target;
}

/** Returns the priority of this record */
public int
getPriority() {
	return priority;
}

void
rrToWire(DataByteOutputStream out, Compression c, boolean canonical) {
	if (target == null)
		return;

	out.writeShort(priority);
	if (type == Type.MX)
		target.toWire(out, c, canonical);
	else
		target.toWire(out, null, canonical);
}

public Name
getAdditionalName() {
	return target;
}

}
