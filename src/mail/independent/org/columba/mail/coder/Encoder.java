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


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.columba.core.command.WorkerStatusController;

public abstract class Encoder implements Cloneable
{
    protected String coding;

    public Encoder()
    {
        CoderRouter.addEncoder( this );
    }

	public String encode( String input, String charset ) throws UnsupportedEncodingException 
	{
		return null;		
	}

	public void encode( InputStream in, OutputStream out, WorkerStatusController workerStatusController ) throws IOException
	{
	}

    public String getCoding()
    {
        return coding;
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
