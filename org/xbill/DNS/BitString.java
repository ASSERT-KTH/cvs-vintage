// Copyright (c) 1999 Brian Wellington (bwelling@xbill.org)
// Portions Copyright (c) 1999 Network Associates, Inc.

package org.xbill.DNS;

import java.io.*;
import java.util.*;
import org.xbill.DNS.utils.*;

/**
 * A representation of a bitstring label
 *
 * @author Brian Wellington
 */

class BitString {

int nbits;
byte [] data;

BitString(String s) throws TextParseException {
	if (Options.check("verbosebitstring"))
		System.err.println("parsing BitString" + s);
	if (s.length() < 4 || !s.startsWith("[") || !s.endsWith("]"))
		throw new TextParseException("Invalid encoding: " + s);
	if (Options.check("verbosebitstring"))
		System.err.println("basic encoding ok");
	int radix;
	switch (s.charAt(1)) {
		case 'x':
			radix = 16;
			break;
		case 'o':
			radix = 8;
			break;
		case 'b':
			radix = 2;
			break;
		case '0': case '1': case '2': case '3': case '4':
		case '5': case '6': case '7': case '8': case '9':
			radix = 0;
			break;
		default:
			throw new TextParseException("Invalid encoding: " + s);
	}
	if (Options.check("verbosebitstring"))
		System.err.println("radix = " + radix);

	int slash = s.indexOf('/');
	BitSet set = new BitSet();

	if (radix > 0) {
		for (int i = 2, j = 0;
		     i < s.length() - 1 && i != slash;
		     i++, j++)
		{
			int x = Character.digit(s.charAt(i), radix);
			if (x == -1)
				throw new TextParseException("Invalid digit: " +
							     s.charAt(i));
			switch (radix) {
				case 2:
					if (x == 1)
						set.set(j);
					nbits++;
					break;
				case 8:
					if ((x & 0x4) != 0)
						set.set(3 * j);
					if ((x & 0x2) != 0)
						set.set(3 * j + 1);
					if ((x & 0x1) != 0)
						set.set(3 * j + 2);
					nbits+=3;
					break;
				case 16:
					if ((x & 0x8) != 0)
						set.set(4 * j);
					if ((x & 0x4) != 0)
						set.set(4 * j + 1);
					if ((x & 0x2) != 0)
						set.set(4 * j + 2);
					if ((x & 0x1) != 0)
						set.set(4 * j + 3);
					nbits+=4;
					break;
			}
		}
	}
	else {
		int end;
		if (slash != -1) {
			end = slash;
		}
		else
			end = s.length() - 1;
		String quad = s.substring(1, end);
		StringTokenizer st = new StringTokenizer(quad, ".");
		for (int i = 0; i < 4; i++) {
			if (!st.hasMoreTokens())
				throw new TextParseException("Invalid dotted " +
							     "quad");
			String token = st.nextToken();
			try {
				int x = Integer.parseInt(token);
				for (int j = 0; j < 8; j++) {
					if ((x & 1) != 0)
						set.set(8 * i + 7 - j);
					x >>= 1;
				}
			}
			catch (NumberFormatException e) {
				throw new TextParseException("Invalid dotted " +
							     "quad");
			}
		}
		nbits = 32;
	}

	if (slash != -1) {
		String count = s.substring(slash + 1, s.length() - 1);
		try {
			int bitcount = Integer.parseInt(count);
			if (bitcount > nbits || bitcount < 0)
				throw new Exception();
			nbits = bitcount;
		}
		catch (Exception e) {
			throw new TextParseException("Invalid binary label: " +
						     s);
		}
	}
	data = new byte[bytes()];
	for (int i = 0; i < nbits; i++)
		data[i/8] |= ((set.get(i) ? 1 : 0) << (7 - i%8));
}

BitString(byte [] b) throws TextParseException {
	this(new String(b));
}

BitString(int _nbits, byte [] _data) {
	nbits = _nbits;
	data = _data;
}

int
bytes() {
	return (nbits + 7) / 8;
}

int
wireBits() {
	return (nbits == 256 ? 0 : nbits);
}

public String
toString() {
	StringBuffer sb = new StringBuffer();
	sb.append("\\[x");
	for (int i = 0; i < bytes(); i++) {
		int value = (int)(data[i] & 0xFF);
		int high = value >> 4;
		int low = value & 0xf;
		sb.append(Integer.toHexString(high).toUpperCase());
		if (low > 0 || i < bytes() - 1)
			sb.append(Integer.toHexString(low).toUpperCase());
	}
	sb.append("/");
	sb.append(nbits);
	sb.append("]");
	return sb.toString();
}

public boolean
equals(Object o) {
	if (!(o instanceof BitString))
		return false;
	BitString b = (BitString) o;
	if (nbits != b.nbits)
		return false;
	for (int i = 0; i < bytes(); i++)
		if (data[i] != b.data[i])
			return false;
	return true;
}

private static final int
getBit(byte [] data, int n) {
	return ((data[n / 8] & 0xFF) >> (7 - n % 8)) & 0x1;
}

private static final void
setBit(byte [] data, int n, int val) {
	if (val == 0)
		data[n / 8] &= ~(1 << (7 - n % 8));
	else
		data[n / 8] |= (1 << (7 - n % 8));
}

/** Compare two bitstrings. */
int
compareBits(BitString b, int n) {
	if (n > nbits || n > b.nbits)
		throw new IllegalArgumentException();


	for (int i = 0; i < n; i++) {
		int bit = getBit(data, i);
		int bbit = getBit(b.data, i);
		if (bit != bbit)
			return (bit - bbit);
	}
	return 0;
}

/** Compare two bitstrings. */
public int
compareTo(Object o) {
	BitString b = (BitString) o;

	int min = nbits > b.nbits ? b.nbits : nbits;
	int ret = compareBits(b, min);
	if (ret != 0)
		return ret;
	return nbits - b.nbits;
}

/** Joins a BitString with a less-significant BitString. */
void
join(BitString b) {
	if (nbits == 256)
		return;
	int total = nbits + b.nbits;
	int i;
	byte [] newdata;

	if (total <= 256) {
		newdata = new byte[(total + 7) / 8];
		for (i = nbits - 1; i >= 0; i--)
			setBit(newdata, i + b.nbits, getBit(data, i));
		for (i = 0; i < b.nbits; i++)
			setBit(newdata, i, getBit(b.data, i));
		nbits = total;
		data = newdata;
		b.nbits = 0;
		b.data = null;
	} else {
		newdata = new byte[32];
		byte [] newbdata = new byte[(total - 256 + 7) / 8];
		for (i = nbits; i >= 0; i--)
			setBit(newdata, 255 - i, getBit(data, nbits - i - 1));
		for (i = 0; i < 256 - nbits; i++)
			setBit(newdata, 255 - nbits - i,
			       getBit(b.data, b.nbits - i - 1));
		nbits = 256;
		data = newdata;
		b.nbits = total - 256;
		System.arraycopy(b.data, 0, newbdata, 0, newbdata.length);
		b.data = newbdata;
	}
}

}
