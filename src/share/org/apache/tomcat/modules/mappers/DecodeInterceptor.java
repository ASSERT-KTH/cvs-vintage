/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:  
 *       "This product includes software developed by the 
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * [Additional notices, if required by prior licensing conditions]
 *
 */ 

package org.apache.tomcat.modules.mappers;

import org.apache.tomcat.core.*;
import org.apache.tomcat.util.buf.*;
import org.apache.tomcat.util.http.*;
import java.util.*;
import java.io.*;

/**
 * Default actions after receiving the request: get charset, unescape,
 * pre-process.
 * 
 */
public class DecodeInterceptor extends  BaseInterceptor  {
    private String defaultEncoding=null;
    // debug, default will be false, null, null
    private boolean useSessionEncoding=true; 
    private String charsetAttribute="charset";
    private String charsetURIAttribute=";charset=";

    // Note ids
    private int encodingInfoNote;
    private int sessionEncodingNote;

    
    public DecodeInterceptor() {
    }

    /* -------------------- Config  -------------------- */

    /** Set server-wide default encoding. 
     *  UTF-8 is recommended ( if you want to brake the standard spec, which
     *  requires 8859-1 )
     */
    public void setDefaultEncoding( String s ) {
	defaultEncoding=s;
    }

    public void setUseSessionEncoding( boolean b ) {
	useSessionEncoding=b;
    }

    public void setCharsetAttribute( String s ) {
	charsetAttribute=s;
	charsetURIAttribute=";" + charsetAttribute + "=";
    }
    
    /* -------------------- Initialization -------------------- */
    
    public void engineInit( ContextManager cm )
	throws TomcatException
    {
	encodingInfoNote=cm.getNoteId(ContextManager.REQUEST_NOTE,
				  "req.encoding" );
	sessionEncodingNote=cm.getNoteId(ContextManager.SESSION_NOTE,
				  "session.encoding" );
    }
    /* -------------------- Request mapping -------------------- */

    public int postReadRequest( Request req ) {
	MessageBytes pathMB = req.requestURI();
	// copy the request 
	
	if( pathMB.isNull())
	    throw new RuntimeException("ASSERT: null path in request URI");

	//if( path.indexOf("?") >=0 )
	//   throw new RuntimeException("ASSERT: ? in requestURI");
	
	// Set the char encoding first
	String charEncoding=null;	
	MimeHeaders headers=req.getMimeHeaders();

	MessageBytes contentType = req.contentType();
	if( contentType != null ) {
	    // XXX use message bytes, optimize !!!
	    String contentTypeString=contentType.toString();
	    charEncoding = ContentType.
		getCharsetFromContentType(contentTypeString);
	    if( debug > 0 ) log( "Got encoding from content-type " +
				 charEncoding + " " + contentTypeString  );
	}

	if( debug > 99 ) dumpHeaders(headers);
	
	// No explicit encoding - try to guess it from Accept-Language
	//MessageBytes acceptC= headers.getValue( "Accept-Charset" );

	// No explicit encoding - try to guess it from Accept-Language
	// MessageBytes acceptL= headers.getValue( "Accept-Language" );

	// Special trick: ;charset= attribute ( similar with sessionId )
	// That's perfect for multibyte chars in URLs
	if(charEncoding==null && charsetURIAttribute != null ) {
	    int idxCharset=req.requestURI().indexOf( charsetURIAttribute );
	    if( idxCharset >= 0 ) {
		String uri=req.requestURI().toString();
		int nextAtt=uri.indexOf( ';', idxCharset + 1 );
		String next=null;
		if( nextAtt > 0 ) {
		    next=uri.substring( nextAtt );
		    charEncoding=
			uri.substring(idxCharset+
				      charsetURIAttribute.length(),nextAtt);
		    req.requestURI().
			setString(uri.substring(0, idxCharset) + next);
		} else {
		    charEncoding=uri.substring(idxCharset+
					       charsetURIAttribute.length());
		    req.requestURI().
			setString(uri.substring(0, idxCharset));
		}
		
		if( debug > 0 )
		    log("ReqAtt= " + charEncoding + " " +
			req.requestURI() );
	    }
	}
	
	
	// Global Default 
	if( charEncoding==null ) {
	    if( debug > 0 ) log( "Default encoding " + defaultEncoding );
	    if( defaultEncoding != null )
		charEncoding=defaultEncoding;
	}

	if( charEncoding != null )
	    req.setCharEncoding( charEncoding );

	// Decode request, save the original for the facade
	
	if (pathMB.indexOf('%') >= 0 || pathMB.indexOf( '+' ) >= 0) {
	    try {
		req.unparsedURI().duplicate( pathMB );
		req.getURLDecoder().convert( pathMB );
		if( pathMB.indexOf( '\0' ) >=0 ) {
		    return 404; // XXX should be 400 
		}
	    } catch( IOException ex ) {
		log( "Error decoding request ", ex);
		return 400;
	    }
	}

	return 0;
    }

    /** Hook - before the response is sent, get the response encoding
     *  and save it per session ( if we are in a session ). All browsers
     *  I know will use the same encoding in the next request.
     *  Since this is not part of the spec, it's disabled by default.
     *  
     */
    public int beforeBody( Request req, Response res ) {
	if( useSessionEncoding ) {
	    ServerSession sess=req.getSession( false );
	    if( sess!=null ) {
		String charset=res.getCharacterEncoding();
		if( charset!=null ) {
		    sess.setNote( sessionEncodingNote, charset );
		    if( debug > 0 )
			log( "Setting per session encoding " + charset);
		}
	    }
	}
	return DECLINED;
    }

    
    public Object getInfo( Context ctx, Request req, int info, String k ) {
	// Try to get the encoding info ( this is called later )
	if( info == encodingInfoNote ) {
	    // Second attempt to guess the encoding, the request is processed
	    String charset=null;

	    // Use request attributes
	    if( charset==null && charsetAttribute != null ) {
		charset=(String)req.getAttribute( charsetAttribute );
		if( debug>0 && charset != null )
		    log( "Charset from attribute " + charsetAttribute + " "
			 + charset );
	    }
	    
	    // Use session attributes
	    if( charset==null && useSessionEncoding ) {
		ServerSession sess=req.getSession( false );
		if( sess!=null ) {
		    charset=(String)sess.getNote( sessionEncodingNote );
		    if( debug>0 && charset!=null )
			log("Charset from session " + charset );
		}
	    }

	    // Per context default
	    
	    if( charset != null ) return charset;
	    
	    log( "Default getInfo UTF-8" );
	    // Use per context default
	    return "UTF-8";
	}
	return null;
    }

    public int setInfo( Context ctx, Request req, int info,
			 String k, Object v )
    {
	return DECLINED;
    }

    private void dumpHeaders( MimeHeaders mh ) {
	for( int i=0; i<mh.size(); i++ ) {
	    log( mh.getName(i) + ": " + mh.getValue( i ) );
	}

    }
}

