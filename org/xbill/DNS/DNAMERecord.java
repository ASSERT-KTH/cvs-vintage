// Copyright (c) 1999-2004 Brian Wellington (bwelling@xbill.org)

package org.xbill.DNS;

import java.io.*;
import org.xbill.DNS.utils.*;

/**
 * DNAME Record  - maps a nonterminal alias (subtree) to a different domain
 *
 * @author Brian Wellington
 */

public class DNAMERecord extends NS_CNAME_PTRRecord {

private static DNAMERecord member = new DNAMERecord();

private
DNAMERecord() {}

private
DNAMERecord(Name name, int dclass, long ttl) {
	super(name, Type.DNAME, dclass, ttl);
}

static DNAMERecord
getMember() {
	return member;
}

/**
 * Creates a new DNAMERecord with the given data
 * @param target The name to which the DNAME alias points
 */
public
DNAMERecord(Name name, int dclass, long ttl, Name target) {
	super(name, Type.DNAME, dclass, ttl, target);
}

Record
rrFromWire(Name name, int type, int dclass, long ttl, DNSInput in)
throws IOException
{
	return rrFromWire(new DNAMERecord(name, dclass, ttl), in);
}

Record
rdataFromString(Name name, int dclass, long ttl, Tokenizer st, Name origin)
throws IOException
{
	return rdataFromString(new DNAMERecord(name, dclass, ttl), st, origin);
}

}
