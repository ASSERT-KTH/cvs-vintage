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

public class QuotedPrintableDecoder extends Decoder {


    public QuotedPrintableDecoder()
    {
        super();
        coding = new String("quoted-printable");

    }

    public String decode( String input, String charset) throws UnsupportedEncodingException {		
		byte[] inBytes;
		byte[] outBytes = new byte[input.length()];
		
		boolean hit = false;
		boolean code = false;
		
		byte codeByte = 0;
		int outPos = 0;
		
		//if( charset == null )
			inBytes = input.getBytes("US-ASCII");
		//else
		//	inBytes = input.getBytes(charset);
		
		for( int i=0; i<input.length(); i++ ) {
			
			if( inBytes[i] == '=' ) {
				hit = true;
			}
			else if( hit ) {
				if( inBytes[i] != '\n' ) {
					if( inBytes[i] >= 'a' ) codeByte = (byte)(16 * ( inBytes[i] - 'a' + 10 ));
					else if ( inBytes[i] >= 'A' ) codeByte = (byte)(16 * ( inBytes[i] - 'A' + 10 ));
					else codeByte = (byte)(16 * ( inBytes[i] - '0' ));
					code = true;
				}
				hit = false;
			}
			else if( code ) {
				if( inBytes[i] >= 'a' ) codeByte += (byte)( inBytes[i] - 'a' + 10 );
				else if ( inBytes[i] >= 'A' ) codeByte += (byte)( inBytes[i] - 'A' + 10 );
				else codeByte += (byte)( inBytes[i] - '0' );
			
				outBytes[outPos] = codeByte;
				outPos++;								
				
				code = false;
			}
			else {
				outBytes[outPos] = inBytes[i];
				outPos++;
			}				
		}
		
		if( charset != null ) {
	    	return new String( outBytes, 0, outPos, charset );
		}
		return new String( outBytes, 0, outPos );
    }
    
    public void decode( InputStream in, OutputStream out ) throws IOException {    	
		BufferedInputStream bufferedIn = new BufferedInputStream( in );
		BufferedOutputStream bufferedOut = new BufferedOutputStream( out );

		byte[] inBytes = new byte[1];
		int read = bufferedIn.read(inBytes);

		boolean hit = false;
		boolean code = false;
		
		byte[] codeByte = new byte[1];
		
		while( read == 1 ) {
			
			if( inBytes[0] == '=' ) {
				hit = true;
			}
			else if( hit ) {
				if( inBytes[0] != '\n' ) {
					if( inBytes[0] >= 'a' ) codeByte[0] = (byte)(16 * ( inBytes[0] - 'a' + 10 ));
					else if ( inBytes[0] >= 'A' ) codeByte[0] = (byte)(16 * ( inBytes[0] - 'A' + 10 ));
					else codeByte[0] = (byte)(16 * ( inBytes[0] - '0' ));
					code = true;
				}
				hit = false;
			}
			else if( code ) {
				if( inBytes[0] >= 'a' ) codeByte[0] += (byte)( inBytes[0] - 'a' + 10 );
				else if ( inBytes[0] >= 'A' ) codeByte[0] += (byte)( inBytes[0] - 'A' + 10 );
				else codeByte[0] += (byte)( inBytes[0] - '0' );
			
				bufferedOut.write( codeByte );
				
				code = false;
			}
			else {
				bufferedOut.write( inBytes[0] );
			}
			
			read = bufferedIn.read(inBytes);
		}
		
		in.close();
		out.close();
    }

    
    

}





