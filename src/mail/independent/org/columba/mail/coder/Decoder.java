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
 * @author 
 */

package org.columba.mail.coder;


import java.io.*;



public abstract class Decoder implements Cloneable
{

    protected String coding;

    public Decoder()
    {
        CoderRouter.addDecoder( this );
    }

    public String getCoding()
    {
        return coding;
    }
    
    public String decode( String input, String charset) throws UnsupportedEncodingException {
    	return null;
    }
    
    public void decode( InputStream in, OutputStream out ) throws IOException {    	
    }

    public Object clone()
    {
        try {
            return super.clone();
        }
        catch( CloneNotSupportedException e ) {
            System.out.println( e );
        }

        return null;
    }    
    
    
}
