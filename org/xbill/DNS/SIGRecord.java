// Copyright (c) 1999 Brian Wellington (bwelling@xbill.org)
// Portions Copyright (c) 1999 Network Associates, Inc.

package org.xbill.DNS;

import java.io.*;
import java.text.*;
import java.util.*;
import org.xbill.DNS.utils.*;

/**
 * Signature - A SIG provides the digital signature of an RRset, so that
 * the data can be authenticated by a DNSSEC-capable resolver.  The
 * signature is usually generated by a key contained in a KEYRecord
 * @see RRset
 * @see DNSSEC
 * @see KEYRecord
 *
 * @author Brian Wellington
 */

public class SIGRecord extends Record {

private static SIGRecord member = new SIGRecord();

private short covered;
private byte alg, labels;
private int origttl;
private Date expire, timeSigned;
private short footprint;
private Name signer;
private byte [] signature;

private
SIGRecord() {}

private
SIGRecord(Name name, short dclass, int ttl) {
	super(name, Type.SIG, dclass, ttl);
}

static SIGRecord
getMember() {
	return member;
}

/**
 * Creates an SIG Record from the given data
 * @param covered The RRset type covered by this signature
 * @param alg The cryptographic algorithm of the key that generated the
 * signature
 * @param origttl The original TTL of the RRset
 * @param expire The time at which the signature expires
 * @param timeSigned The time at which this signature was generated
 * @param footprint The footprint/key id of the signing key.
 * @param signer The owner of the signing key
 * @param signature Binary data representing the signature
 */
public
SIGRecord(Name name, short dclass, int ttl, int covered, int alg, int origttl,
	  Date expire, Date timeSigned, int footprint, Name signer,
	  byte [] signature)
{
	this(name, dclass, ttl);
	this.covered = (short) covered;
	this.alg = (byte) alg;
	this.labels = name.labels();
	this.origttl = origttl;
	this.expire = expire;
	this.timeSigned = timeSigned;
	this.footprint = (short) footprint;
	if (!signer.isAbsolute())
		throw new RelativeNameException(signer);
	this.signer = signer;
	this.signature = signature;
}

Record
rrFromWire(Name name, short type, short dclass, int ttl, int length,
	   DataByteInputStream in)
throws IOException
{
	SIGRecord rec = new SIGRecord(name, dclass, ttl);
	if (in == null)
		return rec;
	int start = in.getPos();
	rec.covered = in.readShort();
	rec.alg = in.readByte();
	rec.labels = in.readByte();
	rec.origttl = in.readInt();
	rec.expire = new Date(1000 * (long)in.readInt());
	rec.timeSigned = new Date(1000 * (long)in.readInt());
	rec.footprint = in.readShort();
	rec.signer = new Name(in);
	rec.signature = new byte[length - (in.getPos() - start)];
	in.read(rec.signature);
	return rec;
}

Record
rdataFromString(Name name, short dclass, int ttl, MyStringTokenizer st,
		Name origin)
throws TextParseException
{
	SIGRecord rec = new SIGRecord(name, dclass, ttl);
	rec.covered = Type.value(nextString(st));
	rec.alg = Byte.parseByte(nextString(st));
	rec.labels = Byte.parseByte(nextString(st));
	rec.origttl = TTL.parseTTL(nextString(st));
	rec.expire = parseDate(nextString(st));
	rec.timeSigned = parseDate(nextString(st));
	rec.footprint = (short) Integer.parseInt(nextString(st));
	rec.signer = Name.fromString(nextString(st), origin);
	rec.signer.checkAbsolute("read an SIG record");
	if (st.hasMoreTokens())
		rec.signature = base64.fromString(remainingStrings(st));
	return rec;
}

/** Converts rdata to a String */
public String
rdataToString() {
	StringBuffer sb = new StringBuffer();
	if (signature != null) {
		sb.append (Type.string(covered));
		sb.append (" ");
		sb.append (alg);
		sb.append (" ");
		sb.append (labels);
		sb.append (" ");
		sb.append (origttl);
		sb.append (" ");
		if (Options.check("multiline"))
			sb.append ("(\n\t");
		sb.append (formatDate(expire));
		sb.append (" ");
		sb.append (formatDate(timeSigned));
		sb.append (" ");
		sb.append ((int)footprint & 0xFFFF);
		sb.append (" ");
		sb.append (signer);
		if (Options.check("multiline")) {
			sb.append("\n");
			sb.append(base64.formatString(signature, 64, "\t",
				  true));
		} else {
			sb.append (" ");
			sb.append(base64.toString(signature));
		}
	}
	return sb.toString();
}

/** Returns the RRset type covered by this signature */
public short
getTypeCovered() {
	return covered;
}

/**
 * Returns the cryptographic algorithm of the key that generated the signature
 */
public byte
getAlgorithm() {
	return alg;
}

/**
 * Returns the number of labels in the signed domain name.  This may be
 * different than the record's domain name if the record is a wildcard
 * record.
 */
public byte
getLabels() {
	return labels;
}

/** Returns the original TTL of the RRset */
public int
getOrigTTL() {
	return origttl;
}

/** Returns the time at which the signature expires */
public Date
getExpire() {
	return expire;
}

/** Returns the time at which this signature was generated */
public Date
getTimeSigned() {
	return timeSigned;
}

/** Returns The footprint/key id of the signing key.  */
public short
getFootprint() {
	return footprint;
}

/** Returns the owner of the signing key */
public Name
getSigner() {
	return signer;
}

/** Returns the binary data representing the signature */
public byte []
getSignature() {
	return signature;
}

void
rrToWire(DataByteOutputStream out, Compression c, boolean canonical) {
	if (signature == null)
		return;

	out.writeShort(covered);
	out.writeByte(alg);
	out.writeByte(labels);
	out.writeInt(origttl);
	out.writeInt((int)(expire.getTime() / 1000));
	out.writeInt((int)(timeSigned.getTime() / 1000));
	out.writeShort(footprint);
	signer.toWire(out, null, canonical);
	out.writeArray(signature);
}

static String
formatDate(Date d) {
	Calendar c = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
	StringBuffer sb = new StringBuffer();
	NumberFormat w4 = new DecimalFormat();
	w4.setMinimumIntegerDigits(4);
	w4.setGroupingUsed(false);
	NumberFormat w2 = new DecimalFormat();
	w2.setMinimumIntegerDigits(2);

	c.setTime(d);
	sb.append(w4.format(c.get(c.YEAR)));
	sb.append(w2.format(c.get(c.MONTH)+1));
	sb.append(w2.format(c.get(c.DAY_OF_MONTH)));
	sb.append(w2.format(c.get(c.HOUR_OF_DAY)));
	sb.append(w2.format(c.get(c.MINUTE)));
	sb.append(w2.format(c.get(c.SECOND)));
	return sb.toString();
}

static Date
parseDate(String s) {
	Calendar c = new GregorianCalendar(TimeZone.getTimeZone("UTC"));

	int year = Integer.parseInt(s.substring(0, 4));
	int month = Integer.parseInt(s.substring(4, 6)) - 1;
	int date = Integer.parseInt(s.substring(6, 8));
	int hour = Integer.parseInt(s.substring(8, 10));
	int minute = Integer.parseInt(s.substring(10, 12));
	int second = Integer.parseInt(s.substring(12, 14));
	c.set(year, month, date, hour, minute, second);

	return c.getTime();
}

}
