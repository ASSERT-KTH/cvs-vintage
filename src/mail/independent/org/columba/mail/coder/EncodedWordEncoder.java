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

import java.io.UnsupportedEncodingException;


public class EncodedWordEncoder extends Encoder {

	private final static byte[] special = { '?' };

    public EncodedWordEncoder()
    {
    	super();
        coding = new String("encoded-word");        
    }
    
	public String encode( String input, String charset) throws UnsupportedEncodingException 
	{	
		// If Charset is US-ASCII we don not need EncodedWords!
		if( charset.toLowerCase().equals( "us-ascii" ) ) return input;
		

		// QPEncoder with specialChar '?' and no linelengthlimit
		QuotedPrintableEncoder encoder = new QuotedPrintableEncoder();	
		encoder.setSpecial( special );
		encoder.setMaxLineLength( -1 );

		String encodedString = encoder.encode( input, charset );

		// finally repalce all ' ' with '_' (see EncodedWords RFC 2047)
		encodedString = encodedString.replace(' ', '_' );
		
		
		// Make portions of length <= 76 total
		String header = "=?"+charset+"?Q?";
		int maxLength = 76 - header.length() - 2;
		
		int pos = 0;
		int nextEnd;
		int lastIndex;
		StringBuffer output = new StringBuffer();
		
		// If longer than allowed -> cut
		while( encodedString.length() - pos > maxLength ) {
			output.append( header );		
			
			// ... but do not cut a '=??' QP-Code !
			nextEnd = pos+maxLength;
			lastIndex = encodedString.lastIndexOf( '=', nextEnd );
			if( (lastIndex > 0 ) && (nextEnd - lastIndex < 3 ) )
				nextEnd = lastIndex;
				
			output.append( encodedString.substring(pos, nextEnd) );
			pos = nextEnd;
			output.append( "?= \n\t" );			
		}		
		
		
		// don't forget the rest
		output.append( header );
		output.append( encodedString.substring(pos) );
		output.append( "?=" );				
		
		return output.toString();		
	}
    

}
