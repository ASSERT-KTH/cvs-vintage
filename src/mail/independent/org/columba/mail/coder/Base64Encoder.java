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

import org.columba.core.command.WorkerStatusController;


public class Base64Encoder extends Encoder {


	private static final byte[] table = { 
		 65, 66, 67, 68, 69, 70, 71, 72, 73, 74,
		 75, 76, 77, 78, 79, 80, 81, 82, 83, 84,
		 85, 86, 87, 88, 89, 90, 97, 98, 99,100,
		101,102,103,104,105,106,107,108,109,110,
		111,112,113,114,115,116,117,118,119,120,
		121,122, 48, 49, 50, 51, 52, 53, 54, 55,
		 56, 57, 43, 47 };        


    public Base64Encoder()
    {
        super();
        coding = new String("base64");
    }
    

    
    

	public String encode( String input, String charset ) throws UnsupportedEncodingException {
		
		byte[] inBytes;
		
		if( charset != null ) {
			inBytes = input.getBytes(charset);
		}
		else {
			inBytes = input.getBytes();
		}

		int length = Array.getLength( inBytes );
		int outLength;		
		if( (length % 3) != 0 ) outLength = ((length /3) + 1) * 4;
		else outLength = (length/3) * 4;
		byte[] outBytes = new byte[outLength+((outLength/76)*2)];
		
		int j=0;
		int bCount = 0;
		int i;
		
		for( i=0; i<length-2; i+=3 ) {			
            outBytes[j] = table[(byte)(0x03F & (inBytes[i]>>2))];
            int test = inBytes[i]>>2;
            j++;
            outBytes[j] = table[(byte)((0x03F & (inBytes[i]<<4)) | (0x00F & (inBytes[i+1]>>4)))];            
            j++;
            outBytes[j] = table[(byte)((0x03F & (inBytes[i+1]<<2)) | (0x003 & (inBytes[i+2]>>6)))];            
            j++;
            outBytes[j] = table[(byte)(0x03F & inBytes[i+2])];
            j++;
            
            bCount++;
            if( bCount > 25 ) {
            	outBytes[j] = 13;
            	j++;
            	bCount = 0;
            }
		}

		int pads = 3 - (length % 3);

		if( pads != 3 ) {			

            outBytes[j] = table[(byte)(0x03F & (inBytes[i]>>2))];
            j++;
            
            if( pads == 1 ) {
	            outBytes[j] = table[(byte)((0x03F & (inBytes[i]<<4)) | (0x00F & (inBytes[i+1]>>4)))];            
    	        j++;
    	        outBytes[j] = table[(byte)(0x03F & (inBytes[i+1]<<2))];
        	    j++;
	            outBytes[j] = 0;
            } else {
	            outBytes[j] = table[(byte)(0x03F & (inBytes[i]<<4))];
	            j++;
    	        outBytes[j] = 0;
        	    j++;
	            outBytes[j] = 0;
            }
			
			for( int n=0; n<pads; n++ ) {
				outBytes[j-n] = 61;
			}
		}
		
		return new String( outBytes, "US-ASCII" );	
	}

	public void encode( InputStream in, OutputStream out, WorkerStatusController workerStatusController ) throws IOException {		
		BufferedInputStream bufferedIn = new BufferedInputStream( in );
		BufferedOutputStream bufferedOut = new BufferedOutputStream( out );
		
		byte[] inBytes = new byte[3];
		byte[] outBytes = new byte[4];
		byte[] crlf = { 13 };
		
		int read = bufferedIn.read( inBytes );
		int block;
		int bCount = 0;
		int progressCounter = read;
		
		while( read == 3 ) {
            outBytes[0] = table[(byte)(0x03F & (inBytes[0]>>2))];
            outBytes[1] = table[(byte)((0x03F & (inBytes[0]<<4)) | (0x00F & (inBytes[1]>>4)))];            
            outBytes[2] = table[(byte)((0x03F & (inBytes[1]<<2)) | (0x003 & (inBytes[2]>>6)))];            
            outBytes[3] = table[(byte)(0x03F & inBytes[2])];
            
            bufferedOut.write( outBytes );

            bCount++;
            if( bCount > 24 ) {
            	bufferedOut.write( crlf );
            	bCount = 0;
            }

            read = bufferedIn.read( inBytes );
            progressCounter += read;
            if( progressCounter > 1024 ) {
            	progressCounter %= 1024;
            	workerStatusController.incProgressBarValue();
            }
		}
		
		if( read > 0 ) {
            outBytes[0] = table[(byte)(0x03F & (inBytes[0]>>2))];
            
            if( read == 2 ) {
	            outBytes[1] = table[(byte)((0x03F & (inBytes[0]<<4)) | (0x00F & (inBytes[1]>>4)))];            
    	        outBytes[2] = table[(byte)(0x03F & (inBytes[1]<<2) )];
        	    outBytes[3] = 0;
            } else {
	            outBytes[1] = table[(byte)(0x03F & (inBytes[0]<<4))];
    	        outBytes[2] = 0;
        	    outBytes[3] = 0;
            }
			
			for( int i=0; i<3-read; i++ ) {
				outBytes[3-i] = 61;
			}
			bufferedOut.write( outBytes );
		}
		
		bufferedIn.close();
		bufferedOut.close();		
	}

}
