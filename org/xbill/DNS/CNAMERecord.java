// Copyright (c) 1999-2004 Brian Wellington (bwelling@xbill.org)

package org.xbill.DNS;

import java.io.*;
import org.xbill.DNS.utils.*;

/**
 * CNAME Record  - maps an alias to its real name
 *
 * @author Brian Wellington
 */

public class CNAMERecord extends NS_CNAME_PTRRecord {

private static CNAMERecord member = new CNAMERecord();

private
CNAMERecord() {}

private
CNAMERecord(Name name, int dclass, long ttl) {
	super(name, Type.CNAME, dclass, ttl);
}

static CNAMERecord
getMember() {
	return member;
}

/**
 * Creates a new CNAMERecord with the given data
 * @param target The name to which the CNAME alias points
 */
public
CNAMERecord(Name name, int dclass, long ttl, Name target) {
	super(name, Type.CNAME, dclass, ttl, target);
}

Record
rrFromWire(Name name, int type, int dclass, long ttl, int length,
	   DataByteInputStream in)
throws IOException
{
	return rrFromWire(new CNAMERecord(name, dclass, ttl), in);
}

Record
rdataFromString(Name name, int dclass, long ttl, Tokenizer st, Name origin)
throws IOException
{
	return rdataFromString(new CNAMERecord(name, dclass, ttl), st, origin);
}

}
