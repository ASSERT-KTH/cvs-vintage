// Copyright (c) 1999-2004 Brian Wellington (bwelling@xbill.org)

package org.xbill.DNS;

import java.io.*;
import java.util.*;
import org.xbill.DNS.utils.*;

/**
 * The base class for SIG/RRSIG records, which have identical formats 
 *
 * @author Brian Wellington
 */

abstract class SIGBase extends Record {

protected int covered;
protected int alg, labels;
protected long origttl;
protected Date expire, timeSigned;
protected int footprint;
protected Name signer;
protected byte [] signature;

protected
SIGBase() {}

protected
SIGBase(Name name, int type, int dclass, long ttl) {
	super(name, type, dclass, ttl);
}

public
SIGBase(Name name, int type, int dclass, long ttl, int covered, int alg,
	long origttl, Date expire, Date timeSigned, int footprint, Name signer,
	byte [] signature)
{
	super(name, type, dclass, ttl);
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

void
rrFromWire(DNSInput in) throws IOException {
	if (in == null)
		return;
	covered = in.readU16();
	alg = in.readU8();
	labels = in.readU8();
	origttl = in.readU32();
	expire = new Date(1000 * in.readU32());
	timeSigned = new Date(1000 * in.readU32());
	footprint = in.readU16();
	signer = new Name(in);
	signature = in.readByteArray();
}

void
rdataFromString(Tokenizer st, Name origin) throws IOException {
	String typeString = st.getString();
	covered = Type.value(typeString);
	if (covered < 0)
		throw st.exception("Invalid type: " + typeString);
	String algString = st.getString();
	alg = DNSSEC.Algorithm.value(algString);
	if (alg < 0)
		throw st.exception("Invalid algorithm: " + algString);
	labels = st.getUInt8();
	origttl = st.getTTL();
	expire = FormattedTime.parse(st.getString());
	timeSigned = FormattedTime.parse(st.getString());
	footprint = st.getUInt16();
	signer = st.getName(origin);
	signature = st.getBase64();
}

/** Converts the RRSIG/SIG Record to a String */
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
