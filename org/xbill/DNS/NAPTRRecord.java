// Copyright (c) 2000 Brian Wellington (bwelling@xbill.org)

package org.xbill.DNS;

import java.io.*;
import org.xbill.DNS.utils.*;

/**
 * Name Authority Pointer Record  - specifies rewrite rule, that when applied
 * to an existing string will produce a new domain.
 *
 * @author Chuck Santos
 */

public class NAPTRRecord extends Record {

private static NAPTRRecord member = new NAPTRRecord();

private short order, preference;
private byte [] flags, service, regexp;
private Name replacement;

private NAPTRRecord() {}

private
NAPTRRecord(Name name, int dclass, int ttl) {
	super(name, Type.NAPTR, dclass, ttl);
}

static NAPTRRecord
getMember() {
	return member;
}

/**
 * Creates an NAPTR Record from the given data
 * @param order The order of this NAPTR.  Records with lower order are
 * preferred.
 * @param preference The preference, used to select between records at the
 * same order.
 * @param flags The control aspects of the NAPTRRecord.
 * @param service The service or protocol available down the rewrite path.
 * @param regexp The regular/substitution expression.
 * @param replacement The domain-name to query for the next DNS resource
 * record, depending on the value of the flags field.
 * @throws IllegalArgumentException One of the strings has invalid escapes
 */
public
NAPTRRecord(Name name, int dclass, int ttl, int order, int preference,
	    String flags, String service, String regexp, Name replacement)
{
	this(name, dclass, ttl);
	this.order = (short) order;
	this.preference = (short) preference;
	try {
		this.flags = byteArrayFromString(flags);
		this.service = byteArrayFromString(service);
		this.regexp = byteArrayFromString(regexp);
	}
	catch (TextParseException e) {
		throw new IllegalArgumentException(e.getMessage());
	}
	if (!replacement.isAbsolute())
		throw new RelativeNameException(replacement);
	this.replacement = replacement;
}

Record
rrFromWire(Name name, int type, int dclass, int ttl, int length,
	   DataByteInputStream in)
throws IOException
{
	NAPTRRecord rec = new NAPTRRecord(name, dclass, ttl);
	if (in == null)
		return rec;
	rec.order = (short) in.readUnsignedShort();
	rec.preference = (short) in.readUnsignedShort();
	rec.flags = in.readStringIntoArray();
	rec.service = in.readStringIntoArray();
	rec.regexp = in.readStringIntoArray();
	rec.replacement = new Name(in);
	return rec;
}

Record
rdataFromString(Name name, int dclass, int ttl, Tokenizer st, Name origin)
throws IOException
{
	NAPTRRecord rec = new NAPTRRecord(name, dclass, ttl);
	rec.order = (short) st.getUInt16();
	rec.preference = (short) st.getUInt16();
	try {
		rec.flags = byteArrayFromString(st.getString());
		rec.service = byteArrayFromString(st.getString());
		rec.regexp = byteArrayFromString(st.getString());
	}
	catch (TextParseException e) {
		throw st.exception(e.getMessage());
	}
	rec.replacement = st.getName(origin);
	return rec;
}

/** Converts rdata to a String */
public String
rdataToString() {
	StringBuffer sb = new StringBuffer();
	if (replacement != null) {
		sb.append(order);
		sb.append(" ");
		sb.append(preference);
		sb.append(" ");
		sb.append(byteArrayToString(flags, true));
		sb.append(" ");
		sb.append(byteArrayToString(service, true));
		sb.append(" ");
		sb.append(byteArrayToString(regexp, true));
		sb.append(" ");
		sb.append(replacement);
	}
	return sb.toString();
}

/** Returns the order */
public short
getOrder() {
	return order;
}

/** Returns the preference */
public short
getPreference() {
	return preference;
}

/** Returns flags */
public String
getFlags() {
	return byteArrayToString(flags, false);
}

/** Returns service */
public String
getService() {
	return byteArrayToString(service, false);
}

/** Returns regexp */
public String
getRegexp() {
	return byteArrayToString(regexp, false);
}

/** Returns the replacement domain-name */
public Name
getReplacement() {
	return replacement;
}

void
rrToWire(DataByteOutputStream out, Compression c, boolean canonical) {
	if (replacement == null && regexp == null)
		return;
	out.writeShort(order);
	out.writeShort(preference);
	out.writeArray(flags, true);
	out.writeArray(service, true);
	out.writeArray(regexp, true);
	replacement.toWire(out, null, canonical);
}

public Name
getAdditionalName() {
	return replacement;
}

}
