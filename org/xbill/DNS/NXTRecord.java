// Copyright (c) 1999-2004 Brian Wellington (bwelling@xbill.org)

package org.xbill.DNS;

import java.io.*;
import java.util.*;
import org.xbill.DNS.utils.*;

/**
 * Next name - this record contains the following name in an ordered list
 * of names in the zone, and a set of types for which records exist for
 * this name.  The presence of this record in a response signifies a
 * failed query for data in a DNSSEC-signed zone. 
 *
 * @author Brian Wellington
 */

public class NXTRecord extends Record {

private static NXTRecord member = new NXTRecord();

private Name next;
private BitSet bitmap;

private
NXTRecord() {}

private
NXTRecord(Name name, int dclass, long ttl) {
	super(name, Type.NXT, dclass, ttl);
}

static NXTRecord
getMember() {
	return member;
}

/**
 * Creates an NXT Record from the given data
 * @param next The following name in an ordered list of the zone
 * @param bitmap The set of type for which records exist at this name
*/
public
NXTRecord(Name name, int dclass, long ttl, Name next, BitSet bitmap) {
	this(name, dclass, ttl);
	if (!next.isAbsolute())
		throw new RelativeNameException(next);
	this.next = next;
	this.bitmap = bitmap;
}

Record
rrFromWire(Name name, int type, int dclass, long ttl, DNSInput in)
throws IOException
{
	NXTRecord rec = new NXTRecord(name, dclass, ttl);
	if (in == null)
		return rec;
	rec.next = new Name(in);
	rec.bitmap = new BitSet();
	int bitmapLength = in.remaining();
	for (int i = 0; i < bitmapLength; i++) {
		int t = in.readU8();
		for (int j = 0; j < 8; j++)
			if ((t & (1 << (7 - j))) != 0)
				rec.bitmap.set(i * 8 + j);
	}
	return rec;
}

Record
rdataFromString(Name name, int dclass, long ttl, Tokenizer st, Name origin)
throws IOException
{
	NXTRecord rec = new NXTRecord(name, dclass, ttl);
	rec.next = st.getName(origin);
	rec.bitmap = new BitSet();
	while (true) {
		Tokenizer.Token t = st.get();
		if (!t.isString())
			break;
		int type = Type.value(t.value, true);
		if (type <= 0 || type > 128)
			throw st.exception("Invalid type: " + t.value);
		rec.bitmap.set(type);
	}
	st.unget();
	return rec;
}

/** Converts rdata to a String */
public String
rdataToString() {
	StringBuffer sb = new StringBuffer();
	if (next != null) {
		sb.append(next);
		int length = bitmap.length();
		for (short i = 0; i < length; i++)
			if (bitmap.get(i)) {
				sb.append(" ");
				sb.append(Type.string(i));
			}
	}
	return sb.toString();
}

/** Returns the next name */
public Name
getNext() {
	return next;
}

/** Returns the set of types defined for this name */
public BitSet
getBitmap() {
	return bitmap;
}

void
rrToWire(DataByteOutputStream out, Compression c, boolean canonical) {
	if (next == null)
		return;

	next.toWire(out, null, canonical);
	int length = bitmap.length();
	for (int i = 0, t = 0; i < length; i++) {
		t |= (bitmap.get(i) ? (1 << (7 - i % 8)) : 0);
		if (i % 8 == 7 || i == length - 1) {
			out.writeByte(t);
			t = 0;
		}
	}
}

}
