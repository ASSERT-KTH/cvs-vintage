<% 
        response.setContentType("text/plain");
        PrintWriter outW = response.getWriter();

	params.ServletUtil.printBody( request, outW );
	outW.flush();

	String pi=(String)request.
	    getAttribute( "javax.servlet.include.path_info");
	if( pi==null ) pi="";
	params.ServletUtil.printParamValues( "", " ]",
				      pi + ":", " = [ ",
				      "", "",
				      " , ",
				      request, outW );
%>