<html>
<head>
    <title>Class test</title>
</head>
<%! public class JSPTest {
        public String getName() {
            return "JSPTest";
        }

        class Inner extends JSPTest {
            public String getName() {
                return "Inner";
            }
        }
    }
%>
<body>
Base name: <% out.println((new JSPTest()).getName()); out.println(", Inner name: " + ((new JSPTest()).new Inner()).getName()); %>
</body>
</html>
