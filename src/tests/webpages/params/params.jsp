<% 
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();

	out.println("Request parameters: " );
	Enumeration paramN=request.getParameterNames();

	while( paramN.hasMoreElements() ) {
	    String name=(String)paramN.nextElement();
	    String all[]=request.getParameterValues(name);

	    out.print(name);
	    out.print(" = [ " );
	    for( int i=0; i<all.length; i++ ) {
		if( i>0 ) out.print( " , ");
		out.print( all[i] );
	    }
	    out.println( " ]");
	    
	}
        out.println(request.getQueryString());
%>