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
<!--
out.println((new JSPTest()).getName()); 
out.println(", Inner name: " + ((new JSPTest()).new Inner()).getName()); 
-->
<%  JSPTest jspTest = new JSPTest(); 
    JSPTest.Inner inn = jspTest.new Inner();
    out.println(" Base name: " + jspTest.getName()); 
    out.println(" Inner name: " + inn.getName()); 
%>
</body>
</html>
