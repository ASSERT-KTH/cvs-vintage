/**
 * 
 * Copyright 2001 Sun Microsystems(TM), Inc. All Rights Reserved.
 * 
 * The contents of this file are made available under and subject to the
 * Research Use Rights of the Sun(TM) Community Source License v 3.0 (the
 * "License"). Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations under
 * the License.
 * 
 * Contributor(s): Sun Microsystems, Inc.
 *
 */
package org.columba.calendar.base;

import java.security.SecureRandom;

/**
 * A universally unique identifier (UUID). A UUID is a 128-bit value.
 * <p>
 * Standard RFC:
 * http://www.ietf.org/internet-drafts/draft-mealling-uuid-urn-05.txt
 * <p>
 */
public class UUIDGenerator {

	/**
	 * random number generator for UUID generation
	 */
	private final SecureRandom secRand = new SecureRandom();

	/**
	 * 128-bit buffer for use with secRand
	 */
	private final byte[] secRandBuf16 = new byte[16];

	public UUIDGenerator() {
		super();
	}

	/**
	 * @return	uuid as String
	 */
	public String newUUID() {
		secRand.nextBytes(secRandBuf16);
		secRandBuf16[6] &= 0x0f;
		secRandBuf16[6] |= 0x40; /* version 4 */
		secRandBuf16[8] &= 0x3f;
		secRandBuf16[8] |= 0x80; /* IETF variant */
		secRandBuf16[10] |= 0x80; /* multicast bit */
		long mostSig = 0;
		for (int i = 0; i < 8; i++) {
			mostSig = (mostSig << 8) | (secRandBuf16[i] & 0xff);
		}
		long leastSig = 0;
		for (int i = 8; i < 16; i++) {
			leastSig = (leastSig << 8) | (secRandBuf16[i] & 0xff);
		}
		return (digits(mostSig >> 32, 8) + "-" + digits(mostSig >> 16, 4) + "-"
				+ digits(mostSig, 4) + "-" + digits(leastSig >> 48, 4) + "-" + digits(
				leastSig, 12));
	}

	/** Returns val represented by the specified number of hex digits. */
	private static String digits(long val, int digits) {
		long hi = 1L << (digits * 4);
		return Long.toHexString(hi | (val & (hi - 1))).substring(1);
	}

}
