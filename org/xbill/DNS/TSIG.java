// Copyright (c) 1999 Brian Wellington (bwelling@anomaly.munge.com)
// Portions Copyright (c) 1999 Network Associates, Inc.

import java.io.*;
import java.net.*;
import java.util.*;

public class dnsTSIG {

private dnsName name;
private byte [] key;
private hmacSigner axfrSigner = null;

dnsTSIG(String name, byte [] key) {
	this.name = new dnsName(name);
	this.key = key;
}

void apply(dnsMessage m) throws IOException {
	Date timeSigned = new Date();
	short fudge = 300;
	hmacSigner h = new hmacSigner(key);

	dnsName alg = new dnsName(dns.HMAC);

	try {
		/* Digest the message */
		h.addData(m.toWire());

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		CountedDataOutputStream dout = new CountedDataOutputStream(out);
		name.toWireCanonical(dout);
		dout.writeShort(dns.ANY);	/* class */
		dout.writeInt(0);		/* ttl */
		alg.toWireCanonical(dout);
		long time = timeSigned.getTime() / 1000;
		short timeHigh = (short) (time >> 32);
		int timeLow = (int) (time);
		dout.writeShort(timeHigh);
		dout.writeInt(timeLow);
		dout.writeShort(fudge);

		dout.writeShort(0); /* No error */
		dout.writeShort(0); /* No other data */

		h.addData(out.toByteArray());
	}
	catch (IOException e) {
		return;
	}
	dnsRecord r = new dnsTSIGRecord(name, dns.ANY, 0, alg, timeSigned,
					fudge, h.sign(), m.getHeader().getID(),
					dns.NOERROR, null);
	m.addRecord(dns.ADDITIONAL, r);
}

/*
 * Since this is only called in the context where a TSIG is expected, it
 * is an error to not have one.  Note that we need to take an unparsed message
 * as input, since we can't recreate the wire format exactly (with the same
 * name compression).
 */
boolean verify(dnsMessage m, byte [] b, dnsTSIGRecord old) {
	dnsTSIGRecord tsig = m.getTSIG();
	hmacSigner h = new hmacSigner(key);
	if (tsig == null)
		return false;
/*System.out.println("found TSIG");*/

	try {
		if (old != null && tsig.getError() == dns.NOERROR) {
			ByteArrayOutputStream bs = new ByteArrayOutputStream();
			CountedDataOutputStream d =
						new CountedDataOutputStream(bs);
			d.writeShort((short)old.getSignature().length);
			h.addData(bs.toByteArray());
			h.addData(old.getSignature());
/*System.out.println("digested query TSIG");*/
		}
		m.getHeader().decCount(dns.ADDITIONAL);
		byte [] header = m.getHeader().toWire();
		m.getHeader().incCount(dns.ADDITIONAL);
		h.addData(header);

		int len = b.length - header.length;	
		len -= tsig.oLength;
		h.addData(b, header.length, len);
/*System.out.println("digested message");*/

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		CountedDataOutputStream dout = new CountedDataOutputStream(out);
		tsig.getName().toWireCanonical(dout);
		dout.writeShort(tsig.dclass);
		dout.writeInt(tsig.ttl);
		tsig.getAlg().toWireCanonical(dout);
		long time = tsig.getTimeSigned().getTime() / 1000;
		short timeHigh = (short) (time >> 32);
		int timeLow = (int) (time);
		dout.writeShort(timeHigh);
		dout.writeInt(timeLow);
		dout.writeShort(tsig.getFudge());
		dout.writeShort(tsig.getError());
		if (tsig.getOther() != null) {
			dout.writeShort(tsig.getOther().length);
			dout.write(tsig.getOther());
		}
		else
			dout.writeShort(0);

		h.addData(out.toByteArray());
/*System.out.println("digested variables");*/
	}
	catch (IOException e) {
		return false;
	}

	if (axfrSigner != null) {
		try {
			ByteArrayOutputStream bs = new ByteArrayOutputStream();
			CountedDataOutputStream d =
						new CountedDataOutputStream(bs);
			d.writeShort((short)tsig.getSignature().length);
			axfrSigner.addData(bs.toByteArray());
			axfrSigner.addData(tsig.getSignature());
		}
		catch (IOException e) {
		}
	}
	if (h.verify(tsig.getSignature()))
		return true;
	else
		return false;
}

void verifyAXFRStart() {
	axfrSigner = new hmacSigner(key);
}

boolean verifyAXFR(dnsMessage m, byte [] b, dnsTSIGRecord old,
		   boolean required, boolean first)
{
	dnsTSIGRecord tsig = m.getTSIG();
	hmacSigner h = axfrSigner;
	
	if (first)
		return verify(m, b, old);
	try {
		if (tsig != null)
			m.getHeader().decCount(dns.ADDITIONAL);
		byte [] header = m.getHeader().toWire();
		if (tsig != null)
			m.getHeader().incCount(dns.ADDITIONAL);
		h.addData(header);

		int len = b.length - header.length;	
		if (tsig != null)
			len -= tsig.oLength;
		h.addData(b, header.length, len);

		if (tsig == null) {
			if (required)
				return false;
			else
				return true;
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		CountedDataOutputStream dout = new CountedDataOutputStream(out);
		long time = tsig.getTimeSigned().getTime() / 1000;
		short timeHigh = (short) (time >> 32);
		int timeLow = (int) (time);
		dout.writeShort(timeHigh);
		dout.writeInt(timeLow);
		dout.writeShort(tsig.getFudge());
		h.addData(out.toByteArray());
	}
	catch (IOException e) {
		return false;
	}

	if (h.verify(tsig.getSignature()) == false) {
		return false;
	}

	h.clear();
	h.addData(tsig.getSignature());

	return true;
}

}
