// Copyright (c) 1999 Brian Wellington (bwelling@xbill.org)
// Portions Copyright (c) 1999 Network Associates, Inc.

package DNS;

import java.util.*;
import java.io.*;

public class Zone extends NameSet {

public static final int CACHE = 1;
public static final int PRIMARY = 2;
public static final int SECONDARY = 3;

private int type;
private Name origin;
private short dclass = DClass.IN;

public
Zone(String file, Cache cache) throws IOException {
	super();
	type = PRIMARY;
	Master m = new Master(file);
	
	Record record;

	while ((record = m.nextRecord()) != null) {
		if (origin == null || record.getName().subdomain(origin)) {
			addRR(record);
			if (origin == null && record.getType() == Type.SOA)
				origin = record.getName();
		}
		else
			cache.addRecord(record, Credibility.ZONE_GLUE, m);
	}
}

public Name
getOrigin() {
	return origin;
}

public RRset
getNS() {
	return (RRset) findExactSet(origin, Type.NS, dclass);
}

public SOARecord
getSOA() {
	RRset rrset = (RRset) findExactSet(origin, Type.SOA, dclass);
	if (rrset == null)
		return null;
	Enumeration e = rrset.rrs();
	return (SOARecord) e.nextElement();
}

public short
getDClass() {
	return dclass;
}

public RRset
findRecords(Name name, short type) {
	return (RRset) findExactSet(name, type, dclass);
}

public Hashtable
findName(Name name) {
        return (Hashtable) super.findName(name);
}

public void
addRR(Record record) {
	Name name = record.getName();
	short type = record.getType();
	RRset rrset = (RRset) findExactSet (name, type, dclass);
	if (rrset == null)
		addSet(name, type, dclass, rrset = new RRset());
	rrset.addRR(record);
}

}
