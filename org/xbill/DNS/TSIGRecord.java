// Copyright (c) 1999 Brian Wellington (bwelling@xbill.org)
// Portions Copyright (c) 1999 Network Associates, Inc.

package org.xbill.DNS;

import java.io.*;
import java.util.*;
import java.text.*;
import org.xbill.DNS.utils.*;

/**
 * Transaction Signature - this record is automatically generated by the
 * resolver.  TSIG records provide transaction security between the
 * sender and receiver of a message, using a shared key.
 * @see Resolver
 * @see TSIG
 *
 * @author Brian Wellington
 */

public class TSIGRecord extends Record {

private static TSIGRecord member = new TSIGRecord();

private Name alg;
private Date timeSigned;
private short fudge;
private byte [] signature;
private int originalID;
private short error;
private byte [] other;

private
TSIGRecord() {} 

private
TSIGRecord(Name name, short dclass, int ttl) {
        super(name, Type.TSIG, dclass, ttl);
}

static TSIGRecord
getMember() {
        return member;
}

/**
 * Creates a TSIG Record from the given data.  This is normally called by
 * the TSIG class
 * @param alg The shared key's algorithm
 * @param timeSigned The time that this record was generated
 * @param fudge The fudge factor for time - if the time that the message is
 * received is not in the range [now - fudge, now + fudge], the signature
 * fails
 * @param signature The signature
 * @param originalID The message ID at the time of its generation
 * @param error The extended error field.  Should be 0 in queries.
 * @param other The other data field.  Currently used only in BADTIME
 * responses.
 * @see TSIG
 */
public
TSIGRecord(Name _name, short _dclass, int _ttl, Name _alg,
	   Date _timeSigned, short _fudge, byte [] _signature,
	   int _originalID, short _error, byte _other[]) throws IOException
{
	super(_name, Type.TSIG, _dclass, _ttl);
	alg = _alg;
	timeSigned = _timeSigned;
	fudge = _fudge;
	signature = _signature;
	originalID = _originalID;
	error = _error;
	other = _other;
}

Record
rrFromWire(Name name, short type, short dclass, int ttl, int length,
	   DataByteInputStream in)
throws IOException
{
	TSIGRecord rec = new TSIGRecord(name, dclass, ttl);
	if (in == null)
		return rec;
	rec.alg = new Name(in);

	short timeHigh = in.readShort();
	int timeLow = in.readInt();
	long time = ((long)timeHigh & 0xFFFF) << 32;
	time += (long)timeLow & 0xFFFFFFFF;
	rec.timeSigned = new Date(time * 1000);
	rec.fudge = in.readShort();

	int sigLen = in.readUnsignedShort();
	rec.signature = new byte[sigLen];
	in.read(rec.signature);

	rec.originalID = in.readUnsignedShort();
	rec.error = in.readShort();

	int otherLen = in.readUnsignedShort();
	if (otherLen > 0) {
		rec.other = new byte[otherLen];
		in.read(rec.other);
	}
	else
		rec.other = null;
	return rec;
}

Record
rdataFromString(Name name, short dclass, int ttl, MyStringTokenizer st,
                Name origin)
throws TextParseException
{
	throw new TextParseException("no text format defined for TSIG");
}

/** Converts rdata to a String */
public String
rdataToString() {
	StringBuffer sb = new StringBuffer();
	if (alg == null)
		return sb.toString();

	sb.append(alg);
	sb.append(" (\n\t");

	sb.append (timeSigned.getTime() / 1000);
	sb.append (" ");
	sb.append (Rcode.TSIGstring(error));
	sb.append ("\n");
	sb.append (base64.formatString(signature, 64, "\t", false));
	if (other != null) {
		sb.append("\n\t <");
		if (error == Rcode.BADTIME) {
			try {
				DataByteInputStream is;
				is = new DataByteInputStream(other);
				long time = is.readUnsignedShort();
				time <<= 32;
				time += ((long)is.readInt() & 0xFFFFFFFF);
				sb.append("Server time: ");
				sb.append(new Date(time * 1000));
			}
			catch (IOException e) {
				sb.append("Truncated BADTIME other data");
			}
		}
		else
			sb.append(base64.toString(other));
		sb.append(">");
	}
	sb.append(" )");
	return sb.toString();
}

/** Returns the shared key's algorithm */
public Name
getAlgorithm() {
	return alg;
}

/** Returns the time that this record was generated */
public Date
getTimeSigned() {
	return timeSigned;
}

/** Returns the time fudge factor */
public short
getFudge() {
	return fudge;
}

/** Returns the signature */
public byte []
getSignature() {
	return signature;
}

/** Returns the original message ID */
public int
getOriginalID() {
	return originalID;
}

/** Returns the extended error */
public short
getError() {
	return error;
}

/** Returns the other data */
public byte []
getOther() {
	return other;
}

void
rrToWire(DataByteOutputStream out, Compression c) throws IOException {
	if (alg == null)
		return;

	alg.toWire(out, null);

	long time = timeSigned.getTime() / 1000;
	short timeHigh = (short) (time >> 32);
	int timeLow = (int) (time);
	out.writeShort(timeHigh);
	out.writeInt(timeLow);
	out.writeShort(fudge);

	out.writeShort((short)signature.length);
	out.write(signature);

	out.writeShort(originalID);
	out.writeShort(error);

	if (other != null) {
		out.writeShort((short)other.length);
		out.write(other);
	}
	else
		out.writeShort(0);
}

void
rrToWireCanonical(DataByteOutputStream out) throws IOException {
	throw new IOException("A TSIG should never be converted to canonical");
}

}
