<%
    RequestDispatcher rd = application.getRequestDispatcher(
                "/dispatch/rdForward.jsp");
    if (null != rd)
        rd.include(request, response);
    else
        response.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "/dispatch/rdForward.jsp not found.");
%>