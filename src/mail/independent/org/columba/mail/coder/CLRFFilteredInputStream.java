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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CLRFFilteredInputStream extends FilterInputStream {

protected byte[] buffer = new byte[1024];
protected int pos = 0;
protected int count = -1;

public CLRFFilteredInputStream( InputStream in ) {
	super( in );
}	

public int read( byte[] out ) throws IOException{
	int outCount = 0;
	byte b; 
		
	while( outCount < 4 ) {
		if( count <= pos ) {
			count = in.read( buffer );
			if( count == -1 ) break;
			pos = 0;
		}
		
		b = buffer[pos];
		pos++;
		
		if( b > 13 ) {
			out[outCount] = b;
			outCount++;
		}
	}
	
	return outCount;	
}


}
