// Copyright (c) 1999 Brian Wellington (bwelling@xbill.org)

package org.xbill.DNS;

import java.util.*;
import java.io.*;
import java.net.*;

/**
 * The Lookup object performs queries at a high level.  The input consists
 * of a name, an optional type, and an optional class.  Caching is used
 * when possible to reduce the number of DNS requests, and a Resolver
 * is used to perform the queries.  A search path can be set or determined
 * by FindServer, which allows lookups of unqualified names.
 * @see Cache
 * @see Resolver
 * @see FindServer
 *
 * @author Brian Wellington
 */

public final class Lookup {

private static Resolver defaultResolver;
private static Name [] defaultSearchPath;
private static Map defaultCaches;

private Resolver resolver;
private Name [] searchPath;
private Cache cache;
private byte credibility;
private Name name;
private short type;
private short dclass;
private boolean verbose;
private int iterations;
private boolean done;
private Record [] answers;
private int result;
private String error;
private boolean nxdomain;
private boolean badresponse;
private boolean networkerror;

/** The lookup was successful. */
public static final int SUCCESSFUL = 0;

/**
 * The lookup failed due to a data or server error. Repeating the lookup
 * would not be helpful.
 */
public static final int UNRECOVERABLE = 1;

/**
 * The lookup failed due to a network error. Repeating the lookup may be
 * helpful.
 */
public static final int TRY_AGAIN = 2;

/** The host does not exist. */
public static final int HOST_NOT_FOUND = 3;

/** The host exists, but has no records associated with the queried type. */
public static final int TYPE_NOT_FOUND = 4;

static {
	try {
		defaultResolver = new ExtendedResolver();
	}
	catch (UnknownHostException e) {
		throw new RuntimeException("Failed to initialize resolver");
	}
	defaultSearchPath = FindServer.searchPath();
	defaultCaches = new HashMap();
}

private static synchronized Cache
getCache(short dclass) {
	Cache c = (Cache) defaultCaches.get(DClass.toShort(dclass));
	if (c == null) {
		c = new Cache(dclass);
		defaultCaches.put(DClass.toShort(dclass), c);
	}
	return c;
}

/**
 * Create a Lookup object that will find records of the given name, type,
 * and class.  The lookup will use the default cache, resolver, and search
 * path, and look for records that are reasonably credible.
 * @param name The name of the desired records
 * @param type The type of the desired records
 * @param dclass The class of the desired records
 * @throws IllegalArgumentException The type is a meta type other than ANY.
 * @see Cache
 * @see Resolver
 * @see Credibility
 * @see Name
 * @see Type
 * @see DClass
 */
public
Lookup(Name name, short type, short dclass) {
	if (!Type.isRR(type) && type != Type.ANY)
		throw new IllegalArgumentException("Cannot query for " +
						   "meta-types other than ANY");
	this.name = name;
	this.type = type;
	this.dclass = dclass;
	this.resolver = defaultResolver;
	this.searchPath = defaultSearchPath;
	this.cache = getCache(dclass);
	this.credibility = Credibility.NORMAL;
	this.verbose = Options.check("verbose");
	this.result = -1;
}

/**
 * Create a Lookup object that will find records of the given name and type
 * in the IN class.
 * @param name The name of the desired records
 * @param type The type of the desired records
 * @throws IllegalArgumentException The type is a meta type other than ANY.
 * @see #Lookup(Name,short,short)
 */
public
Lookup(Name name, short type) {
	this(name, type, DClass.IN);
}

/**
 * Create a Lookup object that will find records of type A at the given name
 * in the IN class.
 * @param name The name of the desired records
 * @see #Lookup(Name,short,short)
 */
public
Lookup(Name name) {
	this(name, Type.A, DClass.IN);
}

/**
 * Create a Lookup object that will find records of the given name, type,
 * and class.
 * @param name The name of the desired records
 * @param type The type of the desired records
 * @param dclass The class of the desired records
 * @throws TextParseException The name is not a valid DNS name
 * @throws IllegalArgumentException The type is a meta type other than ANY.
 * @see #Lookup(Name,short,short)
 */
public
Lookup(String name, short type, short dclass) throws TextParseException {
	this(Name.fromString(name), type, dclass);
}

/**
 * Create a Lookup object that will find records of the given name and type
 * in the IN class.
 * @param name The name of the desired records
 * @param type The type of the desired records
 * @throws TextParseException The name is not a valid DNS name
 * @throws IllegalArgumentException The type is a meta type other than ANY.
 * @see #Lookup(Name,short,short)
 */
public
Lookup(String name, short type) throws TextParseException {
	this(Name.fromString(name), type, DClass.IN);
}

/**
 * Create a Lookup object that will find records of type A at the given name
 * in the IN class.
 * @param name The name of the desired records
 * @throws TextParseException The name is not a valid DNS name
 * @see #Lookup(Name,short,short)
 */
public
Lookup(String name) throws TextParseException {
	this(Name.fromString(name), Type.A, DClass.IN);
}

/**
 * Sets the resolver to use when performing the lookup.
 * @param resolver The resolver to use.
 */
public void
setResolver(Resolver resolver) {
	this.resolver = resolver;
}

/**
 * Sets the search path to use when performing the lookup.
 * @param domains An array of names containing the search path.
 */
public void
setSearchPath(Name [] domains) {
	this.searchPath = domains;
}

/**
 * Sets the search path to use when performing the lookup.
 * @param domains An array of names containing the search path.
 * @throws TextParseException A name in the array is not a valid DNS name.
 */
public void
setSearchPath(String [] domains) throws TextParseException {
	Name [] newdomains = new Name[domains.length];
	for (int i = 0; i < domains.length; i++)
		newdomains[i] = Name.fromString(domains[i], Name.root);
	this.searchPath = newdomains;
}

/**
 * Sets the cache to use when performing the lookup.
 * @param cache The cache to use.
 */
public void
setCache(Cache cache) {
	this.cache = cache;
}

/**
 * Sets the minimum credibility level that will be accepted when performing
 * the lookup.
 * @param credibility The minimum credibility level.
 */
public void
setCredibility(byte credibility) {
	this.credibility = credibility;
}

private void
processResponse(SetResponse response) {
	if (response.isSuccessful()) {
		RRset [] rrsets = response.answers();
		List l = new ArrayList();
		Iterator it;
		int i;

		for (i = 0; i < rrsets.length; i++) {
			it = rrsets[i].rrs();
			while (it.hasNext())
				l.add(it.next());
		}

		result = SUCCESSFUL;
		answers = (Record []) l.toArray(new Record[l.size()]);
		done = true;
	} else if (response.isNXDOMAIN()) {
		nxdomain = true;
	} else if (response.isNXRRSET()) {
		result = TYPE_NOT_FOUND;
		answers = null;
		done = true;
	} else if (response.isCNAME()) {
		CNAMERecord cname = response.getCNAME();
		Name newname = cname.getTarget();
		iterations++;
		lookup(newname, null);
		done = true;
	} else if (response.isDNAME()) {
		DNAMERecord dname = response.getDNAME();
		Name newname = null;
		try {
			newname = name.fromDNAME(dname);
		} catch (NameTooLongException e) {
			result = UNRECOVERABLE;
			error = "Invalid DNAME target";
			done = true;
			return;
		}
		iterations++;
		lookup(newname, null);
		done = true;
	}
}

private void
lookup(Name current, Name suffix) {
	Name tname = null;
	if (suffix == null)
		tname = current;
	else {
		try {
			tname = Name.concatenate(current, suffix);
		}
		catch (NameTooLongException e) {
			return;
		}
	}

	if (verbose)
		System.err.println("lookup " + tname + " " + Type.string(type));

	SetResponse sr = cache.lookupRecords(tname, type, credibility);
	if (verbose)
		System.err.println(sr);
	processResponse(sr);
	if (done)
		return;

	Record question = Record.newRecord(tname, type, dclass);
	Message query = Message.newQuery(question);
	Message response = null;
	try {
		response = resolver.send(query);
	}
	catch (IOException e) {
		// A network error occurred.  Press on.
		networkerror = true;
		return;
	}
	short rcode = response.getHeader().getRcode();
	if (rcode != Rcode.NOERROR && rcode != Rcode.NXDOMAIN) {
		// The server we contacted is broken or otherwise unhelpful.
		// Press on.
		badresponse = true;
		return;
	}

	sr = cache.addMessage(response);
	if (sr == null)
		sr = cache.lookupRecords(tname, type, credibility);
	processResponse(sr);
}

/**
 * Performs the lookup.
 * @return The answers, or null if none are found.
 */
public Record []
run() {
	if (name.isAbsolute())
		lookup(name, null);
	else if (searchPath == null)
		lookup(name, Name.root);
	else {
		if (name.labels() > 1)
			lookup(name, Name.root);
		if (done)
			return answers;

		for (int i = 0; i < searchPath.length; i++) {
			lookup(name, searchPath[i]);
			if (done)
				return answers;
		}

		if (name.labels() <= 1) {
			lookup(name, Name.root);
		}
	}
	if (!done) {
		if (nxdomain) {
			result = HOST_NOT_FOUND;
			done = true;
		} else if (badresponse) {
			result = UNRECOVERABLE;
			error = "bad response";
			done = true;
		} else if (networkerror) {
			result = TRY_AGAIN;
			error = "bad response";
			done = true;
		}
	}
	return answers;
}

/**
 * Returns the answers from the lookup.
 * @return The answers, or null if none are found.
 * @throws IllegalStateException The lookup has not completed.
 */
public Record []
getAnswers() {
	if (!done || result == -1)
		throw new IllegalStateException("Lookup isn't done");
	return answers;
}

/**
 * Returns the result code of the lookup.
 * @return The result code, which can be SUCCESSFUL, UNRECOVERABLE, TRY_AGAIN,
 * HOST_NOT_FOUND, or TYPE_NOT_FOUND.
 * @throws IllegalStateException The lookup has not completed.
 */
public int
getResult() {
	if (!done || result == -1)
		throw new IllegalStateException("Lookup isn't done");
	return result;
}

/**
 * Returns an error string describing the result code of this lookup.
 * @return A string, which may either directly correspond the result code
 * or be more specific.
 * @throws IllegalStateException The lookup has not completed.
 */
public String
getErrorString() {
	if (!done || result == -1)
		throw new IllegalStateException("Lookup isn't done");
	if (error != null)
		return error;
	switch (result) {
		case SUCCESSFUL:	return "successful";
		case UNRECOVERABLE:	return "unrecoverable error";
		case TRY_AGAIN:		return "try again";
		case HOST_NOT_FOUND:	return "host not found";
		case TYPE_NOT_FOUND:	return "type not found";
	}
	return null;
}

}
