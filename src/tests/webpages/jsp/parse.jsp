<html>
<body>
<h1>Parsing test</h1>
<%!
    String Parse(String str) {
        return "<font color=\"#bbc190\">" + str + "</font>";
    }
%>
<!-- Embedded quotes -->
<%= Parse("color") %>
</body>
</html>
