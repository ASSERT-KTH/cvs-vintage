/*
 *  Copyright 1999-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.tomcat.facade;

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.tomcat.core.OutputBuffer;
import org.apache.tomcat.core.Response;

/**
 *  Facade to the PrintWriter returned by Response.
 *  This will grow to include more support for JSPs ( and other templating
 *  systems ) buffering requirements, provide support for accounting
 *  and will allow more control over char-to-byte conversion ( if it proves
 *  that we spend too much time in that area ).
 *
 *  This will also help us control the multi-buffering ( since all writers have
 *  8k or more of un-recyclable buffers). 
 *
 * @author Costin Manolache [costin@eng.sun.com]
 */
// XXX hack - public will be removed after we add the CharBuffer and we fix the converter
public final class ServletWriterFacade extends PrintWriter {
    Response resA;
    OutputBuffer ob;
    
    public ServletWriterFacade( OutputBuffer ob, Response resp ) {
	super( ob );
	this.resA=resp;
	this.ob=ob;
    }

    // -------------------- Write methods --------------------

    public void flush() {
	super.flush();
    }

    public void print( String str ) {
	super.print( str );
   }

    public void println( String str ) {
	super.println( str );
   }

    public void write( char buf[], int offset, int count ) {
	super.write( buf, offset, count );
    }

    public void write( String str ) {
	super.write( str );
    }

    public void close() {
	// We don't close the PrintWriter - super() is not called,
	// so the stream can be reused. We close ob.
	try {
	    ob.close();
	} catch (IOException ex ) {
	    ex.printStackTrace();
	}
    }
    
    /** Reuse the object instance, avoid GC
     *  Called from BSOS
     */
    void recycle() {
    }

}

