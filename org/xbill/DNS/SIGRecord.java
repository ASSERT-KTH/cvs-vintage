// Copyright (c) 1999-2004 Brian Wellington (bwelling@xbill.org)

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

private int covered;
private int alg, labels;
private long origttl;
private Date expire, timeSigned;
private int footprint;
private Name signer;
private byte [] signature;

private
SIGRecord() {}

private
SIGRecord(Name name, int dclass, long ttl) {
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
SIGRecord(Name name, int dclass, long ttl, int covered, int alg, int origttl,
	  Date expire, Date timeSigned, int footprint, Name signer,
	  byte [] signature)
{
	this(name, dclass, ttl);
	Type.check(covered);
	checkU8("alg", alg);
	checkU8("labels", labels);
	TTL.check(origttl);
	checkU16("footprint", footprint);
	this.covered = covered;
	this.alg = alg;
	this.labels = name.labels();
	this.origttl = origttl;
	this.expire = expire;
	this.timeSigned = timeSigned;
	this.footprint = footprint;
	if (!signer.isAbsolute())
		throw new RelativeNameException(signer);
	this.signer = signer;
	this.signature = signature;
}

Record
rrFromWire(Name name, int type, int dclass, long ttl, int length,
	   DataByteInputStream in)
throws IOException
{
	SIGRecord rec = new SIGRecord(name, dclass, ttl);
	if (in == null)
		return rec;
	int start = in.getPos();
	rec.covered = in.readUnsignedShort();
	rec.alg = in.readByte();
	rec.labels = in.readByte();
	rec.origttl = in.readUnsignedInt();
	rec.expire = new Date(1000 * (long)in.readInt());
	rec.timeSigned = new Date(1000 * (long)in.readInt());
	rec.footprint = in.readShort();
	rec.signer = new Name(in);
	rec.signature = new byte[length - (in.getPos() - start)];
	in.read(rec.signature);
	return rec;
}

Record
rdataFromString(Name name, int dclass, long ttl, Tokenizer st, Name origin)
throws IOException
{
	SIGRecord rec = new SIGRecord(name, dclass, ttl);
	String typeString = st.getString();
	int covered = Type.value(typeString);
	if (covered < 0)
		throw st.exception("Invalid type: " + typeString);
	rec.covered = covered;
	String algString = st.getString();
	int alg = DNSSEC.Algorithm.value(algString);
	if (alg < 0)
		throw st.exception("Invalid algorithm: " + algString);
	rec.alg = alg;
	rec.labels = st.getUInt8();
	rec.origttl = st.getTTL();
	rec.expire = FormattedTime.parse(st.getString());
	rec.timeSigned = FormattedTime.parse(st.getString());
	rec.footprint = st.getUInt16();
	rec.signer = st.getName(origin);
	rec.signature = st.getBase64();
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
		sb.append (FormattedTime.format(expire));
		sb.append (" ");
		sb.append (FormattedTime.format(timeSigned));
		sb.append (" ");
		sb.append (footprint);
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
public int
getTypeCovered() {
	return covered;
}

/**
 * Returns the cryptographic algorithm of the key that generated the signature
 */
public int
getAlgorithm() {
	return alg;
}

/**
 * Returns the number of labels in the signed domain name.  This may be
 * different than the record's domain name if the record is a wildcard
 * record.
 */
public int
getLabels() {
	return labels;
}

/** Returns the original TTL of the RRset */
public long
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
public int
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

	out.writeUnsignedShort(covered);
	out.writeByte(alg);
	out.writeByte(labels);
	out.writeUnsignedInt(origttl);
	out.writeUnsignedInt(expire.getTime() / 1000);
	out.writeUnsignedInt(timeSigned.getTime() / 1000);
	out.writeShort(footprint);
	signer.toWire(out, null, canonical);
	out.writeArray(signature);
}

}
