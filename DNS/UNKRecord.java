// Copyright (c) 1999 Brian Wellington (bwelling@xbill.org)
// Portions Copyright (c) 1999 Network Associates, Inc.

package DNS;

import java.io.*;
import java.util.*;

public class dnsUNKRecord extends dnsRecord {

byte [] data;

public 
dnsUNKRecord(dnsName _name, short _type, short _dclass, int _ttl, int length,
	     CountedDataInputStream in, dnsCompression c) throws IOException
{
	super(_name, _type, _dclass, _ttl);
	if (in == null)
		return;
	data = new byte[length];
	in.read(data);
}

public 
dnsUNKRecord(dnsName _name, short _type, short _dclass, int _ttl,
	     MyStringTokenizer st, dnsName origin) throws IOException
{
	super(_name, _type, _dclass, _ttl);
	System.out.println("Unknown type: " + type);
	System.exit(-1);
}

public String
toString() {
	StringBuffer sb = toStringNoData();
	if (data != null)
		sb.append("<unknown format>");
	return sb.toString();
}

byte []
rrToWire(dnsCompression c) {
	return data;
}

}
