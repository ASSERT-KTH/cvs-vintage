// Copyright (c) 1999-2004 Brian Wellington (bwelling@xbill.org)

package org.xbill.DNS;

import java.io.*;
import java.util.*;
import org.xbill.DNS.utils.*;

/**
 * Text - stores text strings
 *
 * @author Brian Wellington
 */

public class TXTRecord extends Record {

private static TXTRecord member = new TXTRecord();

private List strings;

private
TXTRecord() {}

private
TXTRecord(Name name, int dclass, long ttl) {
	super(name, Type.TXT, dclass, ttl);
}

static TXTRecord
getMember() {
	return member;
}

/**
 * Creates a TXT Record from the given data
 * @param strings The text strings
 * @throws IllegalArgumentException One of the strings has invalid escapes
 */
public
TXTRecord(Name name, int dclass, long ttl, List strings) {
	this(name, dclass, ttl);
	if (strings == null)
		throw new IllegalArgumentException
				("TXTRecord: strings must not be null");
	this.strings = new ArrayList(strings.size());
	Iterator it = strings.iterator();
	try {
		while (it.hasNext()) {
			String s = (String) it.next();
			this.strings.add(byteArrayFromString(s));
		}
	}
	catch (TextParseException e) {
		throw new IllegalArgumentException(e.getMessage());
	}
}

/**
 * Creates a TXT Record from the given data
 * @param string One text string
 * @throws IllegalArgumentException The string has invalid escapes
 */
public
TXTRecord(Name name, int dclass, long ttl, String string) {
	this(name, dclass, ttl, Collections.nCopies(1, string));
}

Record
rrFromWire(Name name, int type, int dclass, long ttl, DNSInput in)
throws IOException
{
	TXTRecord rec = new TXTRecord(name, dclass, ttl);
	if (in == null)
		return rec;
	rec.strings = new ArrayList(2);
	while (in.remaining() > 0) {
		byte [] b = in.readCountedString();
		rec.strings.add(b);
	}
	return rec;
}

Record
rdataFromString(Name name, int dclass, long ttl, Tokenizer st, Name origin)
throws IOException
{
	TXTRecord rec = new TXTRecord(name, dclass, ttl);
	rec.strings = new ArrayList(2);
	while (true) {
		Tokenizer.Token t = st.get();
		if (!t.isString())
			break;
		try {
			rec.strings.add(byteArrayFromString(t.value));
		}
		catch (TextParseException e) { 
			throw st.exception(e.getMessage());
		}

	}
	st.unget();
	return rec;
}

/** converts to a String */
public String
rdataToString() {
	StringBuffer sb = new StringBuffer();
	if (strings != null) {
		Iterator it = strings.iterator();
		while (it.hasNext()) {
			byte [] array = (byte []) it.next();
			sb.append(byteArrayToString(array, true));
			if (it.hasNext())
				sb.append(" ");
		}
	}
	return sb.toString();
}

/**
 * Returns the text strings
 * @return A list of Strings corresponding to the text strings.
 */
public List
getStrings() {
	List list = new ArrayList(strings.size());
	for (int i = 0; i < strings.size(); i++)
		list.add(byteArrayToString((byte []) strings.get(i), false));
	return list;
}

/**
 * Returns the text strings
 * @return A list of byte arrays corresponding to the text strings.
 */
public List
getStringsAsByteArrays() {
	return strings;
}

void
rrToWire(DataByteOutputStream out, Compression c, boolean canonical) {
	if (strings == null)
		return;

	Iterator it = strings.iterator();
	while (it.hasNext()) {
		byte [] b = (byte []) it.next();
		out.writeArray(b, true);
	}
}

}
