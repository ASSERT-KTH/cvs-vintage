// Copyright (c) 1999 Brian Wellington (bwelling@xbill.org)
// Portions Copyright (c) 1999 Network Associates, Inc.

package org.xbill.DNS;

import java.io.*;
import java.util.*;

/**
 * A DNS Zone.  This encapsulates all data related to a Zone, and provides
 * convienient lookup methods.
 *
 * @author Brian Wellington
 */

public class Zone extends NameSet {

class AXFREnumeration implements Enumeration {
	private Enumeration znames;
	private Name currentName;
	private Object [] current;
	int count;
	boolean sentFirstSOA, sentNS, sentOrigin, sentLastSOA;

	AXFREnumeration() {
		znames = names();
	}

	public boolean
	hasMoreElements() {
		return (!sentLastSOA);
	}

	public Object
	nextElement() {
		if (sentLastSOA)
			return null;
		if (!sentFirstSOA) {
			sentFirstSOA = true;
			return (RRset) findExactSet(origin, Type.SOA);
		}
		if (!sentNS) {
			sentNS = true;
			return getNS();
		}
		if (!sentOrigin) {
			if (currentName == null) {
				currentName = getOrigin();
				TypeMap tm = findName(currentName);
				current = (Object []) tm.getMultiple(Type.ANY);
				count = 0;
			}
			while (count < current.length) {
				RRset rrset = (RRset) current[count];
				if (rrset.getType() != Type.SOA &&
				    rrset.getType() != Type.NS)
					return current[count++];
				count++;
			}
			current = null;
			sentOrigin = true;
		}
		if (current != null && count < current.length)
			return current[count++];
		while (znames.hasMoreElements()) {
			Name currentName = (Name) znames.nextElement();
			if (currentName.equals(getOrigin()))
				continue;
			TypeMap tm = findName(currentName);
			current = (Object []) tm.getMultiple(Type.ANY);
			count = 0;
			if (count < current.length)
				return current[count++];
		}
		sentLastSOA = true;
		RRset rrset = new RRset();
		rrset.addRR(getSOA());
		return rrset;
	}
}

/** A primary zone */
public static final int PRIMARY = 1;

/** A secondary zone (unimplemented) */
public static final int SECONDARY = 2;

private int type;
private Name origin;
private short dclass = DClass.IN;

private void
validate() throws IOException {
	if (getSOA() == null)
		throw new IOException(origin + ": no SOA specified");
	if (getNS() == null)
		throw new IOException(origin + ": no NS set specified");
}

/**
 * Creates a Zone from the records in the specified master file.  All
 * records that do not belong in the Zone are added to the specified Cache.
 * @see Cache
 * @see Master
 */
public
Zone(String file, Cache cache) throws IOException {
	super(false);
	type = PRIMARY;
	Master m = new Master(file);
	
	Record record;

	while ((record = m.nextRecord()) != null) {
		if (origin == null || record.getName().subdomain(origin)) {
			addRecord(record);
			if (origin == null) {
				if (record.getType() == Type.SOA) {
					origin = record.getName();
					setOrigin(origin);
				}
				else {
					String str = "No SOA at the top of the zone in file " + file;
					throw new IOException(str);
				}
			}
		}
		else if (cache != null)
			cache.addRecord(record, Credibility.ZONE_GLUE, m);
	}
	validate();
}

/**
 * Creates a Zone from the records in the specified master file.  All
 * records that do not belong in the Zone are added to the specified Cache.
 * @see Cache
 * @see Master
 */
public
Zone(Name zone, short _dclass, String remote, Cache cache)
throws IOException
{
	super(false);
	origin = zone;
	dclass = _dclass;
	type = SECONDARY;
	Resolver res = new SimpleResolver(remote);
	Record rec = Record.newRecord(zone, Type.AXFR, dclass);
	Message query = Message.newQuery(rec);
	Message response = res.send(query);
	Record [] recs = response.getSectionArray(Section.ANSWER);
	for (int i = 0; i < recs.length; i++) {
		if (!recs[i].getName().subdomain(origin)) {
			if (Options.check("verbose"))
				System.err.println(recs[i].getName() +
						   "is not in zone " + origin);
			continue;
		}
		addRecord(recs[i]);
	}
	if (cache != null) {
		recs = response.getSectionArray(Section.ADDITIONAL);
		for (int i = 0; i < recs.length; i++)
			cache.addRecord(recs[i], Credibility.ZONE_GLUE, recs);
	}
	validate();
}

/** Returns the Zone's origin */
public Name
getOrigin() {
	return origin;
}

/** Returns the Zone origin's NS records */
public RRset
getNS() {
	return (RRset) findExactSet(origin, Type.NS);
}

/** Returns the Zone's SOA record */
public SOARecord
getSOA() {
	RRset rrset = (RRset) findExactSet(origin, Type.SOA);
	if (rrset == null)
		return null;
	Enumeration e = rrset.rrs();
	return (SOARecord) e.nextElement();
}

/** Returns the Zone's class */
public short
getDClass() {
	return dclass;
}

/**     
 * Looks up Records in the Zone.  This follows CNAMEs and wildcards.
 * @param name The name to look up
 * @param type The type to look up
 * @return A SetResponse object
 * @see SetResponse
 */ 
public SetResponse
findRecords(Name name, short type) {
	SetResponse zr = null;

	Object o = findSets(name, type);
	if (o == null) {
		/* The name does not exist */
		if (name.isWild())
			return new SetResponse(SetResponse.NXDOMAIN);

		int labels = name.labels() - origin.labels();
		if (labels == 0)
			return new SetResponse(SetResponse.NXDOMAIN);
		SetResponse sr;
		Name tname = name;
		do {
			sr = findRecords(tname.wild(1), type);
			if (sr.isSuccessful())
				return sr;
			tname = new Name(tname, 1);
		} while (labels-- >= 1);
		return sr;
	}

	if (o.getClass() == TypeMap.class) {
		/* The name exists but the type does not. */
		return new SetResponse(SetResponse.NXRRSET);
	}

	Object [] objects = (Object []) o;

	RRset [] rrsets = new RRset[objects.length];
	System.arraycopy(objects, 0, rrsets, 0, objects.length);

	RRset rrset = rrsets[0];
	if (name.equals(rrset.getName())) {
		if (type != Type.CNAME && type != Type.ANY &&
		    rrset.getType() == Type.CNAME)
		{
			zr = new SetResponse(SetResponse.CNAME);
			zr.addCNAME((CNAMERecord) rrset.first());
		}
		else if (type != Type.NS && type != Type.ANY &&
			 rrset.getType() == Type.NS)
		{
			zr = new SetResponse(SetResponse.DELEGATION);
			zr.addNS(rrset);
		}
		else {
			zr = new SetResponse(SetResponse.SUCCESSFUL);
			for (int i = 0; i < rrsets.length; i++)
				zr.addRRset(rrsets[i]);
		}
	} else {
		if (rrset.getType() == Type.CNAME)
			return new SetResponse(SetResponse.NXDOMAIN);
		else if (rrset.getType() == Type.DNAME) {
			zr = new SetResponse(SetResponse.DNAME);
			zr.addDNAME((DNAMERecord) rrset.first());
		}
		else if (rrset.getType() == Type.NS) {
			zr = new SetResponse(SetResponse.DELEGATION);
			zr.addNS(rrset);
		}
	}
	return zr;
}

/**
 * Looks up Records in the zone, finding exact matches only.
 * @param name The name to look up
 * @param type The type to look up
 * @return The matching RRset
 * @see RRset
 */ 
public RRset
findExactMatch(Name name, short type) {
	return (RRset) findExactSet(name, type);
}

/**
 * Adds a record to the Zone
 * @param r The record to be added
 * @see Record
 */
public void
addRecord(Record r) {
	Name name = r.getName();
	short type = r.getRRsetType();
	RRset rrset = (RRset) findExactSet (name, type);
	if (rrset == null)
		addSet(name, type, rrset = new RRset());
	rrset.addRR(r);
}

public Enumeration
AXFR() {
	return new AXFREnumeration();
}

}
