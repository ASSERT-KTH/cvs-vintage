// Copyright (c) 1999 Brian Wellington (bwelling@xbill.org)
// Portions Copyright (c) 1999 Network Associates, Inc.

package org.xbill.DNS;

import java.io.*;
import java.util.*;
import org.xbill.DNS.utils.*;

/**
 * Name Server Record  - contains the name server serving the named zone
 *
 * @author Brian Wellington
 */

public class NSRecord extends NS_CNAME_PTRRecord {

private static NSRecord member = new NSRecord();

private
NSRecord() {}

private
NSRecord(Name name, short dclass, int ttl) {
        super(name, Type.NS, dclass, ttl);
}

static NSRecord
getMember() {
        return member;
}

/** 
 * Creates a new NS Record with the given data
 * @param target The name server for the given domain
 */
public
NSRecord(Name _name, short _dclass, int _ttl, Name _target)
throws IOException
{
        super(_name, Type.NS, _dclass, _ttl, _target);
}

Record
rrFromWire(Name name, short type, short dclass, int ttl, int length,
	   DataByteInputStream in)
throws IOException
{
        return rrFromWire(new NSRecord(name, dclass, ttl), in);
}

Record
rdataFromString(Name name, short dclass, int ttl, MyStringTokenizer st,
                Name origin)
throws TextParseException
{
        return rdataFromString(new NSRecord(name, dclass, ttl), st, origin);
}

}
