//The contents of this file are subject to the Mozilla Public License Version 1.1
//(the "License"); you may not use this file except in compliance with the 
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License 
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.

package org.columba.mail.coder;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;

public class QuotedPrintableEncoder extends Encoder {

	private byte[] special;
	private int specialSize;
	private int maxLineLength;

	public QuotedPrintableEncoder() {
		super();
		coding = new String("quoted-printable");
		
		special = null;
		specialSize = 0;
		
		maxLineLength = 74;
	}

	public void setSpecial( byte[] special ) {
		this.special = special;
		specialSize = Array.getLength( special );
	}


	private boolean checkSpecial( byte op ) {
			
		for( int i=0; i<specialSize; i++ ) {
			if( special[i] == op ) return true;	
		}
			
		return false;
	}

	public String encode(String input, String charset)
		throws UnsupportedEncodingException {
		StringBuffer output = new StringBuffer();
		byte[] inBytes;
		byte[] code = { 61, 47, 47 }; // =00 

		if (charset == null) {
			inBytes = input.getBytes();
		} else {
			inBytes = input.getBytes(charset);
		}

		int length = Array.getLength(inBytes);
		int lineLength = 0;

		for (int i = 0; i < length; i++) {
			if ((((inBytes[i] >= 33) && (inBytes[i] <= 60))
				|| ((inBytes[i] >= 62) && (inBytes[i] <= 126))
				|| (inBytes[i] == 9)
				|| (inBytes[i] == 32)
				|| (inBytes[i] == 10)
				|| (inBytes[i] == 13) )
				&& !checkSpecial(inBytes[i]) ) {
				output.append( new String( inBytes, i, 1, "US-ASCII") );
			}
			else {				
				if( ((0x0f) & (inBytes[i] >> 4)) > 9 ) {
					code[1] = (byte)(((0x0f) & (inBytes[i] >> 4)) + 55); // A..F
				} else {
					code[1] = (byte)(((0x0f) & (inBytes[i] >> 4)) + 48); // 0..9
				}

				if( (inBytes[i] & 0x0F) > 9 ) {
					code[2] = (byte)((inBytes[i] & 0x0F) + 55); // A..F
				} else {
					code[2] = (byte)((inBytes[i] & 0x0F) + 48); // 0..9
				}
								
				output.append( new String(code, "US-ASCII") );					
			}
			
			if( inBytes[i] != 13 ) {
				lineLength++;
				if( (lineLength > maxLineLength) && (maxLineLength > 0 ) ) {
					output.append( "=\n" );
					lineLength = 0;
				}	
			} else {
				lineLength = 0;
			}
		}

		return output.toString();
	}

	public void encode( InputStream in, OutputStream out, int workerStatusController ) throws IOException
	{
		BufferedInputStream bufferedIn = new BufferedInputStream( in );
		BufferedOutputStream bufferedOut = new BufferedOutputStream( out );

		byte[] inBytes = new byte[1];
		byte[] code = { 61, 47, 47 }; // =00 
		byte[] softBreak = { 61, 13 }; // =\n

		int read = bufferedIn.read( inBytes );
		int lineLength = 0;
		

		while( read > 0 ) {
			if (((inBytes[0] >= 33) && (inBytes[0] <= 60))
				|| ((inBytes[0] >= 62) && (inBytes[0] <= 126))
				|| (inBytes[0] == 9)
				|| (inBytes[0] == 32)
				|| (inBytes[0] == 10)
				|| (inBytes[0] == 13) ) {
				bufferedOut.write(inBytes[0]);
			}
			else {				
				if( ((0x0f) & (inBytes[0] >> 4)) > 9 ) {
					code[1] = (byte)(((0x0f) & (inBytes[0] >> 4)) + 55); // A..F
				} else {
					code[1] = (byte)(((0x0f) & (inBytes[0] >> 4)) + 48); // 0..9
				}

				if( (inBytes[0] & 0x0F) > 9 ) {
					code[2] = (byte)((inBytes[0] & 0x0F) + 55); // A..F
				} else {
					code[2] = (byte)((inBytes[0] & 0x0F) + 48); // 0..9
				}
								
				bufferedOut.write( code );					
			}
			
			if( inBytes[0] != 13 ) {
				lineLength++;
				if( (lineLength > maxLineLength) && (maxLineLength > 0 ) ) {
					bufferedOut.write( softBreak );
					lineLength = 0;
				}	
			} else {
				lineLength = 0;
			}
			
			read = bufferedIn.read( inBytes );
		}

		
	}


	/**
	 * Sets the maxLineLength.
	 * @param maxLineLength The maxLineLength to set
	 */
	public void setMaxLineLength(int maxLineLength) {
		this.maxLineLength = maxLineLength;
	}

}