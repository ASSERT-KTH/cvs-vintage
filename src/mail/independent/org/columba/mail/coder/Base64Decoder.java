// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

/** Document the purpose of this class.
 *
 * @version 1.0
 * @author Timo Stich
 */

package org.columba.mail.coder;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

public class Base64Decoder extends Decoder {

	// US-ASCII to Base64 Table

	private static byte[] table =
		{
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			62,
			000,
			000,
			000,
			63,
			52,
			53,
		// + / 0..
		54, 55, 56, 57, 58, 59, 60, 61, 000, 000, // ..9
		000, 0, 000, 000, 000, 0, 1, 2, 3, 4, // = A..
		5, 6, 7, 8, 9, 10, 11, 12, 13, 14, // ..
		15, 16, 17, 18, 19, 20, 21, 22, 23, 24, //
		25, 000, 000, 000, 000, 000, 000, 26, 27, 28, // ..Z a..
		29, 30, 31, 32, 33, 34, 35, 36, 37, 38, // ..
		39, 40, 41, 42, 43, 44, 45, 46, 47, 48, // ..
		49, 50, 51, 000, 000, 000, 000, 000 }; // ..z

	public Base64Decoder() {
		super();
		coding = new String("base64");
	}

	public String decode(String input, String charset)
		throws UnsupportedEncodingException {
		byte[] inBytes = null;

		// Clean \r and \n from String

		if ((input.indexOf('\r') != -1) || (input.indexOf('\n') != -1)) {
			StringTokenizer tokenizer = new StringTokenizer(input, "\r\n");
			StringBuffer clean = new StringBuffer();

			while (tokenizer.hasMoreTokens()) {
				clean.append(tokenizer.nextToken());
			}
			
			if( charset == null ) 
				inBytes = clean.toString().getBytes("US-ASCII");
			else 
				inBytes = clean.toString().getBytes(charset);

		} else {
			inBytes = input.getBytes("US-ASCII");
		}

		// Compute size of endbuffer

		int length = Array.getLength(inBytes);
		int error = length % 4;

		if (error != 0) {
			length = length - error;
		}

		int pads = 0;

		while (input.charAt(length - 1 - pads) == '=')
			pads++;

		byte[] outBytes = new byte[((int) (length * 0.75)) - pads];
		int[] lookedUp = new int[4];

		// Decode

		int inPos = 0;
		int outPos = 0;

		while (inPos < length - 4) {
			lookedUp[0] = table[inBytes[inPos]];
			inPos++;
			lookedUp[1] = table[inBytes[inPos]];
			inPos++;
			lookedUp[2] = table[inBytes[inPos]];
			inPos++;
			lookedUp[3] = table[inBytes[inPos]];
			inPos++;

			outBytes[outPos] = (byte) ((lookedUp[0] << 2) | (lookedUp[1] >> 4));
			outPos++;
			outBytes[outPos] = (byte) ((lookedUp[1] << 4) | (lookedUp[2] >> 2));
			outPos++;
			outBytes[outPos] = (byte) ((lookedUp[2] << 6) | (lookedUp[3]));
			outPos++;
		}

		// Last block with pads

		lookedUp[0] = table[inBytes[inPos]];
		inPos++;
		lookedUp[1] = table[inBytes[inPos]];
		inPos++;
		lookedUp[2] = table[inBytes[inPos]];
		inPos++;
		lookedUp[3] = table[inBytes[inPos]];
		inPos++;

		outBytes[outPos] = (byte) ((lookedUp[0] << 2) | (lookedUp[1] >> 4));
		if (pads < 2) {
			outPos++;
			outBytes[outPos] = (byte) ((lookedUp[1] << 4) | (lookedUp[2] >> 2));
			if (pads < 1) {
				outPos++;
				outBytes[outPos] = (byte) ((lookedUp[2] << 6) | (lookedUp[3]));
			}
		}

		// return result in charset

		if (charset != null) {
			return new String(outBytes, charset);
		}
		return new String(outBytes);
	}

	public void decode(InputStream in, OutputStream out) throws IOException {
		CLRFFilteredInputStream bufferedIn = new CLRFFilteredInputStream(in);
		BufferedOutputStream bufferedOut = new BufferedOutputStream(out);

		byte[] inBytes = new byte[4];
		int[] lookedUp = new int[4];
		byte[] outBytes = new byte[3];

		//int block;
		int read = bufferedIn.read(inBytes);

		while (read == 4) {

			lookedUp[0] = table[inBytes[0]];
			lookedUp[1] = table[inBytes[1]];
			lookedUp[2] = table[inBytes[2]];
			lookedUp[3] = table[inBytes[3]];

			outBytes[0] = (byte) ((lookedUp[0] << 2) | (lookedUp[1] >> 4));
			outBytes[1] = (byte) ((lookedUp[1] << 4) | (lookedUp[2] >> 2));
			outBytes[2] = (byte) ((lookedUp[2] << 6) | (lookedUp[3]));

			if (inBytes[3] != 61) {
				bufferedOut.write(outBytes);
			} else {
				int pads = 1;
				if (inBytes[2] == 61)
					pads++;

				bufferedOut.write(outBytes, 0, 3 - pads);
			}

			read = bufferedIn.read(inBytes);
		}
		bufferedIn.close();
		bufferedOut.close();
	}

}