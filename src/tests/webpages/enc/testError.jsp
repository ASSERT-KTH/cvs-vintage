<%@ page import="java.util.*" contentType="text/html; charset=KOI8-r"%>

<html>
<head><title>Тест инклуда</title></head>

<%= new Date() %><br>

Это в руте текст<br>
---------------------<br>
<%@ include file="incTest.inc"%>
<br>---------------------
снова в руте


</body>
</html>





