// Copyright (c) 1999-2004 Brian Wellington (bwelling@xbill.org)

package org.xbill.DNS;

import java.io.*;
import java.text.*;
import org.xbill.DNS.utils.*;

/**
 * Geographical Location - describes the physical location of a host.
 *
 * @author Brian Wellington
 */

public class GPOSRecord extends Record {

private byte [] latitude, longitude, altitude;

GPOSRecord() {}

Record
getObject() {
	return new GPOSRecord();
}

/**
 * Creates an GPOS Record from the given data
 * @param longitude The longitude component of the location.
 * @param latitude The latitude component of the location.
 * @param altitude The altitude component of the location (in meters above sea
 * level).
*/
public
GPOSRecord(Name name, int dclass, long ttl, double longitude, double latitude,
	   double altitude)
{
	super(name, Type.GPOS, dclass, ttl);
	if (longitude < -90.0 || longitude > 90.0) {
		throw new IllegalArgumentException("illegal longitude " +
						   longitude);
	}
	if (latitude < -180.0 || latitude > 180.0) {
		throw new IllegalArgumentException("illegal latitude " +
						   latitude);
	}
	this.longitude = Double.toString(longitude).getBytes();
	this.latitude = Double.toString(latitude).getBytes();
	this.altitude = Double.toString(altitude).getBytes();
}

/**
 * Creates an GPOS Record from the given data
 * @param longitude The longitude component of the location.
 * @param latitude The latitude component of the location.
 * @param altitude The altitude component of the location (in meters above sea
 * level).
*/
public
GPOSRecord(Name name, int dclass, long ttl, String longitude, String latitude,
	   String altitude)
{
	super(name, Type.GPOS, dclass, ttl);
	try {
		this.longitude = byteArrayFromString(longitude);
		this.latitude = byteArrayFromString(latitude);
		this.altitude = byteArrayFromString(altitude);
	}
	catch (TextParseException e) {
		throw new IllegalArgumentException(e.getMessage());
	}
}

void
rrFromWire(DNSInput in) throws IOException {
	longitude = in.readCountedString();
	latitude = in.readCountedString();
	altitude = in.readCountedString();
}

void
rdataFromString(Tokenizer st, Name origin) throws IOException {
	try {
		this.longitude = byteArrayFromString(st.getString());
		this.latitude = byteArrayFromString(st.getString());
		this.altitude = byteArrayFromString(st.getString());
	}
	catch (TextParseException e) {
		throw st.exception(e.getMessage());
	}
}

/** Convert to a String */
String
rrToString() {
	StringBuffer sb = new StringBuffer();
	sb.append(byteArrayToString(longitude, true));
	sb.append(" ");
	sb.append(byteArrayToString(latitude, true));
	sb.append(" ");
	sb.append(byteArrayToString(altitude, true));
	return sb.toString();
}

/** Returns the longitude as a string */
public String
getLongitudeString() {
	return byteArrayToString(longitude, false);
}

/**
 * Returns the longitude as a double
 * @throws NumberFormatException The string does not contain a valid numeric
 * value.
 */
public double
getLongitude() {
	return Double.parseDouble(getLongitudeString());
}

/** Returns the latitude as a string */
public String
getLatitudeString() {
	return byteArrayToString(latitude, false);
}

/**
 * Returns the latitude as a double
 * @throws NumberFormatException The string does not contain a valid numeric
 * value.
 */
public double
getLatitude() {
	return Double.parseDouble(getLatitudeString());
}

/** Returns the altitude as a string */
public String
getAltitudeString() {
	return byteArrayToString(altitude, false);
}

/**
 * Returns the altitude as a double
 * @throws NumberFormatException The string does not contain a valid numeric
 * value.
 */
public double
getAltitude() {
	return Double.parseDouble(getAltitudeString());
}

void
rrToWire(DNSOutput out, Compression c, boolean canonical) {
	out.writeCountedString(longitude);
	out.writeCountedString(latitude);
	out.writeCountedString(altitude);
}

}
