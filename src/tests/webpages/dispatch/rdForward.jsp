<%
    String type = request.getParameter("type");
    String pg = "foo.html";
    if ("jsp".equals(type))
        pg = "foo.jsp";
    RequestDispatcher rd = application.getRequestDispatcher(
                "/dispatch/" + pg);
    if (null != rd)
        rd.forward(request, response);
    else
        response.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "/dispatch/" + pg + " not found.");
%>