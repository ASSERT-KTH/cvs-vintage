// Copyright (c) 1999 Brian Wellington (bwelling@xbill.org)
// Portions Copyright (c) 1999 Network Associates, Inc.

package org.xbill.DNS;

import java.io.*;
import java.util.*;
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
PTRRecord(Name name, short dclass, int ttl) {
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
PTRRecord(Name _name, short _dclass, int _ttl, Name _target)
throws IOException
{
        super(_name, Type.PTR, _dclass, _ttl, _target);
}

Record
rrFromWire(Name name, short type, short dclass, int ttl, int length,
	   DataByteInputStream in)
throws IOException
{
	return rrFromWire(new PTRRecord(name, dclass, ttl), in);
}

Record
rdataFromString(Name name, short dclass, int ttl, MyStringTokenizer st,
		Name origin)
throws TextParseException
{
	return rdataFromString(new PTRRecord(name, dclass, ttl), st, origin);
}

}
