// Copyright (c) 1999 Brian Wellington (bwelling@xbill.org)
// Portions Copyright (c) 1999 Network Associates, Inc.

package org.xbill.DNS;

import java.util.*;

/**
 * Boolean options:<BR>
 * bindttl - Print TTLs in BIND format<BR>
 * noprintin - Don't print the class of a record if it's IN<BR>
 * nohex - Don't print anything in hex (KEY flags, for example)<BR>
 * pqdn - Allow partially qualified domain names<BR>
 * verbose - Turn on general debugging statements<BR>
 * verbosehmac - Print all data digested by the HMAC routines<BR>
 * verbosemsg - Print all messages sent or received by SimpleResolver<BR>
 * verbosecompression - Print messages related to name compression<BR>
 * verbosebitstring - Print messages related to bitstring labels<BR>
 * verbosesec - Print messages related to signature verification<BR>
 * <BR>
 * Valued options:<BR>
 * tsigfudge=n - Sets the default TSIG fudge value (in seconds)<BR>
 * sig0validity=n - Sets the default SIG(0) validity period (in seconds)<BR>
 * bind9sig0 - Use the broken BIND 9.0/9.1.0 SIG(0) format<BR>
 *
 * @author Brian Wellington
 */

public final class Options {

private static Map table;

static {
	table = new HashMap();
	String s = System.getProperty("dnsjava.options");
	if (s != null) {
		StringTokenizer st = new StringTokenizer(s, ",");
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			int index = token.indexOf('=');
			if (index == -1)
				set(token);
			else {
				String option = token.substring(0, index);
				String value = token.substring(index + 1);
				set(option, value);
			}
		}
	}
}

private
Options() {}

/** Sets an option to "true" */
public static void
set(String option) {
	table.put(option.toLowerCase(), "true");
}

/** Sets an option to the the supplied value */
public static void
set(String option, String value) {
	table.put(option.toLowerCase(), value.toLowerCase());
}

/** Removes an option */
public static void
unset(String option) {
	table.remove(option.toLowerCase());
}

/** Checks if an option is defined */
public static boolean
check(String option) {
	if (table.size() == 0)
		return (false);
	return (table.get(option.toLowerCase()) != null);
}

/** Returns the value of an option */
public static String
value(String option) {
	return ((String)table.get(option.toLowerCase()));
}

}
