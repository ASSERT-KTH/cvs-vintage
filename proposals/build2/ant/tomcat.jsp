<h2>Ant results</h2> 
<%@ page buffer="none" %>
<%@ taglib uri="http://jakarta.apache.org/taglibs/ant-1.0" prefix="ant" %>

<ant:ant >
  <ant:target param="target" />
  <ant:property name="ant.file" 
		location="/WEB-INF/scripts/tomcat.xml" />
  <ant:property name="package.name" param="package.name" />
  <ant:property name="arch.name" param="arch.name" />
  <ant:property name="tag" param="tag" />
</ant:ant>
