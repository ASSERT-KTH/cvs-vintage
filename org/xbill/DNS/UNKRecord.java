// Copyright (c) 1999 Brian Wellington (bwelling@xbill.org)
// Portions Copyright (c) 1999 Network Associates, Inc.

package org.xbill.DNS;

import java.io.*;
import org.xbill.DNS.utils.*;

/**
 * A class implementing Records of unknown and/or unimplemented types.  This
 * class can only be initialized using static Record initializers.
 *
 * @author Brian Wellington
 */

public class UNKRecord extends Record {

private static UNKRecord member = new UNKRecord();

private byte [] data;

private
UNKRecord() {}

private
UNKRecord(Name name, int type, int dclass, long ttl) {
	super(name, type, dclass, ttl);
}

static UNKRecord
getMember() {
	return member;
}

Record
rrFromWire(Name name, int type, int dclass, long ttl, int length,
	   DataByteInputStream in)
throws IOException
{
	UNKRecord rec = new UNKRecord(name, type, dclass, ttl);
	if (in == null)
		return rec;
	if (length > 0) {
		rec.data = new byte[length];
		in.read(rec.data);
	}
	else
		rec.data = null;
	return rec;
}

Record
rdataFromString(Name name, int dclass, long ttl, Tokenizer st, Name origin)
throws IOException
{
	throw st.exception("invalid unknown RR encoding");
}

/** Converts this Record to the String "unknown format" */
public String
rdataToString() {
	StringBuffer sb = new StringBuffer();
	if (data != null) {
		sb.append("\\# ");
		sb.append(data.length);
		sb.append(" ");
		sb.append(base16.toString(data));
	}
	return sb.toString();
}

void
rrToWire(DataByteOutputStream out, Compression c, boolean canonical) {
	if (data != null)
		out.writeArray(data);
}

}
