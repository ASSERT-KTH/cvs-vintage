<html>
<!--
-->
<%@ page errorPage="errorpge.jsp" %>
<%! public class ErrTest {
        public String getName() {
            String a = null;
            if (a.equals("NullPoinerException")) {
            }
            return null;
        }
    }
%>
<body>
<% out.println((new ErrTest()).getName()); %>
	<% 
        //int i = 10/0;
	%>	
</body>
</html>

