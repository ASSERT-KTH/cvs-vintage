// Copyright (c) 1999-2004 Brian Wellington (bwelling@xbill.org)

package org.xbill.DNS;

import java.io.*;
import org.xbill.DNS.utils.*;

/**
 * CNAME Record  - maps an alias to its real name
 *
 * @author Brian Wellington
 */

public class CNAMERecord extends SingleCompressedNameBase {

CNAMERecord() {}

Record
getObject() {
	return new CNAMERecord();
}

/**
 * Creates a new CNAMERecord with the given data
 * @param alias The name to which the CNAME alias points
 */
public
CNAMERecord(Name name, int dclass, long ttl, Name alias) {
	super(name, Type.CNAME, dclass, ttl, alias, "alias");
}

/**
 * Gets the target of the CNAME Record
 */
public Name
getTarget() {
	return getSingleName();
}

/** Gets the alias specified by the CNAME Record */
public Name
getAlias() {
	return getSingleName();
}

}
