// Copyright (c) 1999-2004 Brian Wellington (bwelling@xbill.org)

package org.xbill.DNS;

import java.net.*;
import java.net.Inet6Address;

/**
 * Routines dealing with IP addresses.  Includes functions similar to
 * those in the java.net.InetAddress class.
 *
 * @author Brian Wellington
 */

public final class Address {

public static final int IPv4 = 1;
public static final int IPv6 = 2;

private
Address() {}

/**
 * Convert a string containing an IP address to an array of 4 integers.
 * @param s The string
 * @return The address
 */
public static int []
toArray(String s) {
	int numDigits;
	int currentOctet;
	int [] values = new int[4];
	int length = s.length();

	currentOctet = 0;
	numDigits = 0;
	for (int i = 0; i < length; i++) {
		char c = s.charAt(i);
		if (c >= '0' && c <= '9') {
			/* Can't have more than 3 digits per octet. */
			if (numDigits == 3)
				return null;
			/* Octets shouldn't start with 0, unless they are 0. */
			if (numDigits > 0 && values[currentOctet] == 0)
				return null;
			numDigits++;
			values[currentOctet] *= 10;
			values[currentOctet] += (c - '0');
			/* 255 is the maximum value for an octet. */
			if (values[currentOctet] > 255)
				return null;
		} else if (c == '.') {
			/* Can't have more than 3 dots. */
			if (currentOctet == 3)
				return null;
			/* Two consecutive dots are bad. */
			if (numDigits == 0)
				return null;
			currentOctet++;
			numDigits = 0;
		} else
			return null;
	}
	/* Must have 4 octets. */
	if (currentOctet != 3)
		return null;
	/* The fourth octet can't be empty. */
	if (numDigits == 0)
		return null;
	return values;
}

/**
 * Convert a string containing an IP address to an array of 4 integers.
 * @param s The string
 * @return The address
 */
public static byte []
toByteArray(String s) {
	int [] intArray = toArray(s);
	if (intArray == null)
		return null;
	byte [] byteArray = new byte[intArray.length];
	for (int i = 0; i < intArray.length; i++)
		byteArray[i] = (byte) intArray[i];
	return byteArray;
}

/**
 * Determines if a string contains a valid IP address.
 * @param s The string
 * @return Whether the string contains a valid IP address
 */
public static boolean
isDottedQuad(String s) {
	int [] address = Address.toArray(s);
	return (address != null);
}

/**
 * Converts a byte array containing an IPv4 address into a dotted quad string.
 * @param addr The array
 * @return The string representation
 */
public static String
toDottedQuad(byte [] addr) {
	return ((addr[0] & 0xFF) + "." + (addr[1] & 0xFF) + "." +
		(addr[2] & 0xFF) + "." + (addr[3] & 0xFF));
}

/**
 * Converts an int array containing an IPv4 address into a dotted quad string.
 * @param addr The array
 * @return The string representation
 */
public static String
toDottedQuad(int [] addr) {
	return (addr[0] + "." + addr[1] + "." + addr[2] + "." + addr[3]);
}

private static Record []
lookupHostName(String name) throws UnknownHostException {
	try {
		Record [] records = new Lookup(name).run();
		if (records == null)
			throw new UnknownHostException("unknown host");
		return records;
	}
	catch (TextParseException e) {
		throw new UnknownHostException("invalid name");
	}
}

/**
 * Determines the IP address of a host
 * @param name The hostname to look up
 * @return The first matching IP address
 * @exception UnknownHostException The hostname does not have any addresses
 */
public static InetAddress
getByName(String name) throws UnknownHostException {
	if (isDottedQuad(name))
		return InetAddress.getByName(name);
	Record [] records = lookupHostName(name);
	ARecord a = (ARecord) records[0];
	return a.getAddress();
}

/**
 * Determines all IP address of a host
 * @param name The hostname to look up
 * @return All matching IP addresses
 * @exception UnknownHostException The hostname does not have any addresses
 */
public static InetAddress []
getAllByName(String name) throws UnknownHostException {
	if (isDottedQuad(name))
		return InetAddress.getAllByName(name);
	Record [] records = lookupHostName(name);
	InetAddress [] addrs = new InetAddress[records.length];
	for (int i = 0; i < records.length; i++) {
		ARecord a = (ARecord) records[i];
		addrs[i] = a.getAddress();
	}
	return addrs;
}

/**
 * Determines the hostname for an address
 * @param addr The address to look up
 * @return The associated host name
 * @exception UnknownHostException There is no hostname for the address
 */
public static String
getHostName(InetAddress addr) throws UnknownHostException {
	Name name = ReverseMap.fromAddress(addr);
	Record [] records = new Lookup(name, Type.PTR).run();
	if (records == null)
		throw new UnknownHostException("unknown address");
	PTRRecord ptr = (PTRRecord) records[0];
	return ptr.getTarget().toString();
}

/**
 * Returns the family of an InetAddress.
 * @param address The supplied address.
 * @return The family, either IPv4 or IPv6.
 */
public static int
familyOf(InetAddress address) {
	if (address instanceof Inet4Address)
		return IPv4;
	if (address instanceof Inet6Address)
		return IPv6;
	throw new IllegalArgumentException("unknown address family");
}

/**
 * Returns the family of an InetAddress.
 * @param address The address family, either IPv4 or IPv6.
 * @return The length of addresses in that family.
 */
public static int
addressLength(int family) {
	if (family == IPv4)
		return 4;
	if (family == IPv6)
		return 16;
	throw new IllegalArgumentException("unknown address family");
}

}
