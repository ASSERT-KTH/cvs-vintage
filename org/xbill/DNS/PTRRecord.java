// Copyright (c) 1999-2004 Brian Wellington (bwelling@xbill.org)

package org.xbill.DNS;

import java.io.*;
import org.xbill.DNS.utils.*;

/**
 * Pointer Record  - maps a domain name representing an Internet Address to
 * a hostname.
 *
 * @author Brian Wellington
 */

public class PTRRecord extends NS_CNAME_PTRRecord {

private static PTRRecord member = new PTRRecord();

private
PTRRecord() {}

private
PTRRecord(Name name, int dclass, long ttl) {
	super(name, Type.PTR, dclass, ttl);
}

static PTRRecord
getMember() {
	return member;
}

/** 
 * Creates a new PTR Record with the given data
 * @param target The name of the machine with this address
 */
public
PTRRecord(Name name, int dclass, long ttl, Name target) {
	super(name, Type.PTR, dclass, ttl, target);
}

Record
rrFromWire(Name name, int type, int dclass, long ttl, DNSInput in)
throws IOException
{
	return rrFromWire(new PTRRecord(name, dclass, ttl), in);
}

Record
rdataFromString(Name name, int dclass, long ttl, Tokenizer st, Name origin)
throws IOException
{
	return rdataFromString(new PTRRecord(name, dclass, ttl), st, origin);
}

}
