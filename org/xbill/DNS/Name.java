// Copyright (c) 1999 Brian Wellington (bwelling@xbill.org)
// Portions Copyright (c) 1999 Network Associates, Inc.

package org.xbill.DNS;

import java.io.*;
import java.util.*;
import org.xbill.DNS.utils.*;

/**
 * A representation of a domain name. 
 *
 * @author Brian Wellington
 */


public class Name {

class BitString {
	int nbits;
	byte [] data;

	BitString(int _nbits, byte [] _data) {
		nbits = _nbits;
		data = _data;
	}

	int
	bytes() {
		return (nbits + 7) & ~7;
	}

	public String
	toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("\\[x");
		for (int i = 0; i < bytes(); i++) {
			int high = data[i] >>> 4;
			int low = data[i];
			sb.append(Integer.toHexString(high));
			sb.append(Integer.toHexString(low));
		}
		sb.append("]");
		return sb.toString();
	}
}

private Object [] name;
private byte labels;
private boolean qualified;

/** The root name */
public static Name root = new Name("");

/** The maximum number of labels in a Name */
static final int MAXLABELS = 256;

/**
 * Create a new name from a string and an origin
 * @param s  The string to be converted
 * @param origin  If the name is unqalified, the origin to be appended
 */
public
Name(String s, Name origin) {
	labels = 0;
	name = new Object[MAXLABELS];

	if (s.equals("@") && origin != null) {
		append(origin);
		qualified = true;
		return;
	}
	try {
		MyStringTokenizer st = new MyStringTokenizer(s, ".");

		while (st.hasMoreTokens())
			name[labels++] = st.nextToken();

		if (st.hasMoreDelimiters())
			qualified = true;
		else {
			if (origin != null) {
				append(origin);
				qualified = true;
			}
			else {
				/* This isn't exactly right, but it's close.
				 * Partially qualified names are evil.
				 */
				if (Options.check("pqdn"))
					qualified = false;
				else
					qualified = (labels > 1);
			}
		}
	}
	catch (ArrayIndexOutOfBoundsException e) {
		StringBuffer sb = new StringBuffer();
		sb.append("String ");
		sb.append(s);
		if (origin != null) {
			sb.append(".");
			sb.append(origin);
		}
		sb.append(" has too many labels");
		System.out.println(sb.toString());
		name = null;
		labels = 0;
	}
	
}

/**
 * Create a new name from a string
 * @param s  The string to be converted
 */
public
Name(String s) {
	this (s, null);
}

Name(DataByteInputStream in, Compression c) throws IOException {
	int len, start, count = 0;

	labels = 0;
	name = new String[MAXLABELS];

	start = in.getPos();
loop:
	while ((len = in.readUnsignedByte()) != 0) {
		switch(len & 0xC0) {
		case 0:
		{
			byte [] b = new byte[len];
			in.read(b);
			name[labels++] = new String(b);
			count++;
			break;
		}
		case 0xC0:
		{
			int pos = in.readUnsignedByte();
			pos += ((len & ~0xC0) << 8);
			Name name2 = (c == null) ? null : c.get(pos);
/*System.out.println("Looking for name at " + pos + ", found " + name2);*/
			if (name2 == null)
				throw new WireParseException("bad compression");
			else {
				System.arraycopy(name2.name, 0, name, labels,
						 name2.labels);
				labels += name2.labels;
			}
			break loop;
		}
		case 0x40:
		{
			int type = len & 0x3F;
			switch (type) {
			case 0:
				int pos = in.readUnsignedShort();
				Name name2 = (c == null) ? null : c.get(pos);
/*System.out.println("Looking for name at " + pos + ", found " + name2);*/
				if (name2 == null)
					throw new WireParseException(
							"bad compression");
				else {
					System.arraycopy(name2.name, 0, name,
							 labels, name2.labels);
					labels += name2.labels;
				}
				break loop;
			case 1:
			{
				int bits = in.readUnsignedByte();
				int bytes = (bits + 7) & ~7;
				byte [] data = new byte[bytes];
				in.read(data);
				name[labels++] = new BitString(bits, data);
				count++;
				break;
			}
			case 2:
				throw new WireParseException(
						"Long local compression");
			default:
				throw new WireParseException(
						"Unknown name format");
			} /* switch */
		}
		case 0x80:
			throw new WireParseException("Local compression");
		} /* switch */
	}
	if (c != null) 
		for (int i = 0, pos = start; i < count; i++) {
			Name tname = new Name(this, i);
			c.add(pos, tname);
/*System.out.println("(D) Adding " + tname + " at " + pos);*/
			if (name[i] instanceof String)
				pos += (((String)name[i]).length() + 1);
			else
				pos += (((BitString)name[i]).nbits + 2);
		}
	qualified = true;
}

/**
 * Create a new name by removing labels from the beginning of an existing Name
 * @param d  An existing Name
 * @param n  The number of labels to remove from the beginning in the copy
 */
/* Skips n labels and creates a new name */
public
Name(Name d, int n) {
	name = new String[MAXLABELS];

	labels = (byte) (d.labels - n);
	System.arraycopy(d.name, n, name, 0, labels);
	qualified = d.qualified;
}

/**
 * Generates a new Name with the first label replaced by a wildcard 
 * @return The wildcard name
 */
public Name
wild() {
	Name wild = new Name(this, 0);
	wild.name[0] = "*";
	return wild;
}

/**
 * Is this name a wildcard?
 */
public boolean
isWild() {
	return name[0].equals("*");
}

/**
 * Is this name fully qualified?
 */
public boolean
isQualified() {
	return qualified;
}

/**
 * Appends the specified name to the end of the current Name
 */
public void
append(Name d) {
	System.arraycopy(d.name, 0, name, labels, d.labels);
	labels += d.labels;
}

/**
 * The length
 */
public short
length() {
	short total = 0;
	for (int i = 0; i < labels; i++) {
		if (name[i] instanceof String)
			total += (((String)name[i]).length() + 1);
		else
			total += (((BitString)name[i]).bytes() + 2);
	}
	return ++total;
}

/**
 * The number of labels
 */
public byte
labels() {
	return labels;
}

/**
 * Is the current Name a subdomain of the specified name?
 */
public boolean
subdomain(Name domain) {
	if (domain == null || domain.labels > labels)
		return false;
	int i = labels, j = domain.labels;
	while (j > 0)
		if (!name[--i].equals(domain.name[--j]))
			return false;
	return true;
}

/**
 * Convert Name to a String
 */
public String
toString() {
	StringBuffer sb = new StringBuffer();
	if (labels == 0)
		sb.append(".");
	for (int i = 0; i < labels; i++) {
		sb.append(name[i]);
		if (qualified || i < labels - 1)
			sb.append(".");
	}
	return sb.toString();
}

/**
 * Convert Name to DNS wire format
 */
public void
toWire(DataByteOutputStream out, Compression c) throws IOException {
	for (int i = 0; i < labels; i++) {
		Name tname = new Name(this, i);
		int pos;
		if (c != null)
			pos = c.get(tname);
		else
			pos = -1;
/*System.out.println("Looking for compressed " + tname + ", found " + pos);*/
		if (pos >= 0) {
			pos |= (0xC0 << 8);
			out.writeShort(pos);
			return;
		}
		else {
			if (c != null)
				c.add(out.getPos(), tname);
/*System.out.println("(C) Adding " + tname + " at " + out.getPos());*/
			if (name[i] instanceof String)
				out.writeString((String)name[i]);
			else {
				out.writeByte(0xC0 | 1);
				out.writeByte(((BitString)name[i]).nbits);
				out.write(((BitString)name[i]).data);
			}
		}
	}
	out.writeByte(0);
}

/**
 * Convert Name to canonical DNS wire format (all lowercase)
 */
public void
toWireCanonical(DataByteOutputStream out) throws IOException {
	for (int i = 0; i < labels; i++) {
		if (name[i] instanceof String)
			out.writeStringCanonical((String)name[i]);
		else {
			out.writeByte(0xC0 | 1);
			out.writeByte(((BitString)name[i]).nbits);
			out.write(((BitString)name[i]).data);
		}
	}
	out.writeByte(0);
}

/**
 * Are these two Names equivalent?
 */
public boolean
equals(Object arg) {
	if (arg == null || !(arg instanceof Name))
		return false;
	Name d = (Name) arg;
	if (d.labels != labels)
		return false;
	for (int i = 0; i < labels; i++) {
		if (name[i].getClass() != d.name[i].getClass())
			return false;
		if (name[i] instanceof String) {
			String s1 = (String) name[i];
			String s2 = (String) d.name[i];
			if (!s1.equalsIgnoreCase(s2))
				return false;
		}
		else {
			BitString b1 = (BitString) name[i];
			BitString b2 = (BitString) d.name[i];
			if (b1.nbits != b2.nbits)
				return false;
			for (int j = 0; j < b1.bytes(); j++)
				if (b1.data[j] != b2.data[j])
					return false;
		}
	}
	return true;
}

/**
 * Computes a hashcode based on the value
 */
public int
hashCode() {
	int code = labels;
	for (int i = 0; i < labels; i++) {
		if (name[i] instanceof String) {
			String s = (String) name[i];
			for (int j = 0; j < s.length(); j++)
				code += Character.toLowerCase(s.charAt(j));
		}
		else {
			BitString b = (BitString) name[i];
			for (int j = 0; j < b.bytes(); j++)
				code += b.data[i];
		}
	}
	return code;
}

}
