<% 
        response.setContentType("text/plain");
        java.io.PrintWriter outW = response.getWriter();

	// No parameter is read 

	String uri="/servlet/params.InputStreamParams/include1?a=b";
	outW.println("Calling RD.include for: " + uri);
	// The POST body should be available in the included
	// servlet - it should not be read before the first
	// getParameter.
	RequestDispatcher rd=request.getRequestDispatcher(uri);

	rd.include( request, response );

	ServletUtil.printParamValues( "", " ]",
				      "postInclude1:", " = [ ",
				      "", "",
				      " , ",
				      request, outW );
%>