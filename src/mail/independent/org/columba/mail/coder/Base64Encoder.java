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
