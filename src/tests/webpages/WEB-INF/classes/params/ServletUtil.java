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

package params;

import java.io.*;
import java.text.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 */
public class ServletUtil {


    public static void printParams(HttpServletRequest request,
				   PrintWriter out )
        throws IOException, ServletException
    {
	printParamNames( "Params = [", " ]"," , ",request, out );
	printParamValues( "", " ]",
			  "", " = [ " ,
			  "", "",
			  " , ",request, out );
	out.println("Query = " + request.getQueryString());

    }
    
    public static void printParamNames( String prefix, String sufix,
					String sep,
					HttpServletRequest request,
					PrintWriter out )
        throws IOException, ServletException
    {
	Enumeration paramN=request.getParameterNames();
	out.print( prefix );
	
	while( paramN.hasMoreElements() ) {
	    String name=(String)paramN.nextElement();
	    String all[]=request.getParameterValues(name);
	    out.print(name);
	    if( paramN.hasMoreElements()) out.print( sep );
	}
	
	out.println( sufix );
    }

    

    public static void printParamValues( String prefix,
					 String sufix,
					 String prefixN,
					 String sufixN,
					 String prefixV,
					 String sufixV,
					 String sepV,
					 HttpServletRequest request,
					 PrintWriter out )
        throws IOException, ServletException
    {
	Enumeration  paramN=request.getParameterNames();

	out.print( prefix );
	while( paramN.hasMoreElements() ) {
	    String name=(String)paramN.nextElement();
	    String all[]=request.getParameterValues(name);

	    out.print(prefixN);
	    out.print(name);
	    out.print(sufixN );
	    for( int i=0; i<all.length; i++ ) {
		if( i>0 ) out.print( sepV );
		out.print( prefixV );
		out.print( all[i] );
		out.print( sufixV );
	    }
	    out.println( sufix );
	    
	}
	
    }

    public static void printBody( HttpServletRequest request,
				  PrintWriter out )
        throws IOException, ServletException
    {
	InputStream in=request.getInputStream();
	
	out.println("START BODY: " );
	int c;
	while( (c=in.read()) >= 0 ) {
	    out.write( (char)c );
	}
	out.println();
	out.println("END BODY");
    }

}



