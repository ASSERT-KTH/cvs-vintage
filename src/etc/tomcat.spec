Summary: Apache's servlet engine
Name: tomcat
Version: 3.0
Release: 0
Vendor: Apache Software Foundation
Group: System Environment/Daemons
Copyright: Apache - free
## Icon: tomcat.gif
Url: http://jakarta.apache.org
# BuildRoot: /home/costin/
Provides: tomcat 

Prefix: /opt

Source: http://jakarta.apache.org/builds/tmp/tomcat/jakarta-tomcat.src.zip
Source: http://jakarta.apache.org/builds/tmp/tomcat/jakarta-tools.src.zip

%description
Develop Web applications in Java.


%prep
cd /usr/src/redhat/BUILD
rm -rf jakarta-tomcat
rm -rf build
unzip -x ${RPM_SOURCE_DIR}/jakarta-tomcat.src.zip
unzip -x ${RPM_SOURCE_DIR}/jakarta-tools.src.zip
cd jakarta-tomcat

%build
cd /usr/src/redhat/BUILD/jakarta-tomcat
ant 

%install
cd /usr/src/redhat/BUILD/jakarta-tomcat
ant -Dtomcat.home /opt/tomcat dist 

%clean
rm -rf jakarta-tomcat 
rm -rf build

%post

%preun
  
%files
%dir /opt/tomcat
%dir /opt/tomcat/lib
%dir /opt/tomcat/etc
%dir /opt/tomcat/src
%dir /opt/tomcat/src/javax
%dir /opt/tomcat/src/javax/servlet
%dir /opt/tomcat/src/javax/servlet/http
%dir /opt/tomcat/src/javax/servlet/jsp
%dir /opt/tomcat/src/javax/servlet/jsp/tagext
%dir /opt/tomcat/examples
%dir /opt/tomcat/examples/WEB-INF
%dir /opt/tomcat/examples/WEB-INF/classes
%dir /opt/tomcat/examples/WEB-INF/classes/examples
%dir /opt/tomcat/examples/WEB-INF/classes/checkbox
%dir /opt/tomcat/examples/WEB-INF/classes/error
%dir /opt/tomcat/examples/WEB-INF/classes/num
%dir /opt/tomcat/examples/WEB-INF/classes/cal
%dir /opt/tomcat/examples/WEB-INF/classes/colors
%dir /opt/tomcat/examples/WEB-INF/classes/sessions
%dir /opt/tomcat/examples/WEB-INF/classes/dates
%dir /opt/tomcat/examples/WEB-INF/jsp
%dir /opt/tomcat/examples/WEB-INF/jsp/applet
%dir /opt/tomcat/examples/jsp
%dir /opt/tomcat/examples/jsp/plugin
%dir /opt/tomcat/examples/jsp/plugin/applet
%dir /opt/tomcat/examples/jsp/cal
%dir /opt/tomcat/examples/jsp/jsptoserv
%dir /opt/tomcat/examples/jsp/dates
%dir /opt/tomcat/examples/jsp/colors
%dir /opt/tomcat/examples/jsp/error
%dir /opt/tomcat/examples/jsp/sessions
%dir /opt/tomcat/examples/jsp/forward
%dir /opt/tomcat/examples/jsp/checkbox
%dir /opt/tomcat/examples/jsp/simpletag
%dir /opt/tomcat/examples/jsp/include
%dir /opt/tomcat/examples/jsp/num
%dir /opt/tomcat/examples/jsp/snp
%dir /opt/tomcat/examples/servlets
%dir /opt/tomcat/examples/images
%dir /opt/tomcat/webpages
%dir /opt/tomcat/webpages/docs
%dir /opt/tomcat/webpages/docs/api
%dir /opt/tomcat/webpages/docs/api/javax
%dir /opt/tomcat/webpages/docs/api/javax/servlet
%dir /opt/tomcat/webpages/docs/api/javax/servlet/jsp
%dir /opt/tomcat/webpages/docs/api/javax/servlet/jsp/tagext
%dir /opt/tomcat/webpages/docs/api/javax/servlet/jsp/tagext/class-use
%dir /opt/tomcat/webpages/docs/api/javax/servlet/jsp/class-use
%dir /opt/tomcat/webpages/docs/api/javax/servlet/class-use
%dir /opt/tomcat/webpages/docs/api/javax/servlet/http
%dir /opt/tomcat/webpages/docs/api/javax/servlet/http/class-use
%dir /opt/tomcat/webpages/WEB-INF
%dir /opt/tomcat/webpages/WEB-INF/classes
/opt/tomcat/lib/servlet.jar
/opt/tomcat/lib/jasper.jar
/opt/tomcat/etc/server.xml
/opt/tomcat/etc/server.dtd
/opt/tomcat/etc/web.xml
/opt/tomcat/etc/web.dtd
/opt/tomcat/etc/tomcat.conf
/opt/tomcat/etc/SimpleStartup.java
/opt/tomcat/src/javax/servlet/http/HttpServletResponse.java
/opt/tomcat/src/javax/servlet/http/LocalStrings.properties
/opt/tomcat/src/javax/servlet/http/Cookie.java
/opt/tomcat/src/javax/servlet/http/HttpSessionBindingListener.java
/opt/tomcat/src/javax/servlet/http/HttpServlet.java
/opt/tomcat/src/javax/servlet/http/HttpSession.java
/opt/tomcat/src/javax/servlet/http/HttpSessionContext.java
/opt/tomcat/src/javax/servlet/http/HttpUtils.java
/opt/tomcat/src/javax/servlet/http/HttpSessionBindingEvent.java
/opt/tomcat/src/javax/servlet/http/HttpServletRequest.java
/opt/tomcat/src/javax/servlet/jsp/JspEngineInfo.java
/opt/tomcat/src/javax/servlet/jsp/PageContext.java
/opt/tomcat/src/javax/servlet/jsp/tagext/BodyTagSupport.java
/opt/tomcat/src/javax/servlet/jsp/tagext/TagSupport.java
/opt/tomcat/src/javax/servlet/jsp/tagext/TagAttributeInfo.java
/opt/tomcat/src/javax/servlet/jsp/tagext/BodyTag.java
/opt/tomcat/src/javax/servlet/jsp/tagext/TagLibraryInfo.java
/opt/tomcat/src/javax/servlet/jsp/tagext/Tag.java
/opt/tomcat/src/javax/servlet/jsp/tagext/BodyContent.java
/opt/tomcat/src/javax/servlet/jsp/tagext/TagInfo.java
/opt/tomcat/src/javax/servlet/jsp/tagext/VariableInfo.java
/opt/tomcat/src/javax/servlet/jsp/tagext/TagData.java
/opt/tomcat/src/javax/servlet/jsp/tagext/TagExtraInfo.java
/opt/tomcat/src/javax/servlet/jsp/JspPage.java
/opt/tomcat/src/javax/servlet/jsp/JspTagException.java
/opt/tomcat/src/javax/servlet/jsp/JspFactory.java
/opt/tomcat/src/javax/servlet/jsp/JspWriter.java
/opt/tomcat/src/javax/servlet/jsp/JspException.java
/opt/tomcat/src/javax/servlet/jsp/HttpJspPage.java
/opt/tomcat/src/javax/servlet/ServletContext.java
/opt/tomcat/src/javax/servlet/ServletOutputStream.java
/opt/tomcat/src/javax/servlet/Servlet.java
/opt/tomcat/src/javax/servlet/ServletConfig.java
/opt/tomcat/src/javax/servlet/UnavailableException.java
/opt/tomcat/src/javax/servlet/ServletRequest.java
/opt/tomcat/src/javax/servlet/GenericServlet.java
/opt/tomcat/src/javax/servlet/RequestDispatcher.java
/opt/tomcat/src/javax/servlet/SingleThreadModel.java
/opt/tomcat/src/javax/servlet/LocalStrings.properties
/opt/tomcat/src/javax/servlet/ServletInputStream.java
/opt/tomcat/src/javax/servlet/ServletResponse.java
/opt/tomcat/src/javax/servlet/ServletException.java
/opt/tomcat/examples/WEB-INF/classes/examples/FooTagExtraInfo.class
/opt/tomcat/examples/WEB-INF/classes/examples/ExampleTagBase.class
/opt/tomcat/examples/WEB-INF/classes/examples/FooTag.class
/opt/tomcat/examples/WEB-INF/classes/examples/LogTag.class
/opt/tomcat/examples/WEB-INF/classes/examples/FooTag.java
/opt/tomcat/examples/WEB-INF/classes/examples/FooTagExtraInfo.java
/opt/tomcat/examples/WEB-INF/classes/examples/ShowSource.class
/opt/tomcat/examples/WEB-INF/classes/examples/ExampleTagBase.java
/opt/tomcat/examples/WEB-INF/classes/examples/ShowSource.java
/opt/tomcat/examples/WEB-INF/classes/examples/LogTag.java
/opt/tomcat/examples/WEB-INF/classes/RequestHeaderExample.java
/opt/tomcat/examples/WEB-INF/classes/servletToJsp.class
/opt/tomcat/examples/WEB-INF/classes/checkbox/CheckTest.class
/opt/tomcat/examples/WEB-INF/classes/checkbox/CheckTest.java
/opt/tomcat/examples/WEB-INF/classes/error/Smart.java
/opt/tomcat/examples/WEB-INF/classes/error/Smart.class
/opt/tomcat/examples/WEB-INF/classes/LocalStrings.properties
/opt/tomcat/examples/WEB-INF/classes/num/NumberGuessBean.java
/opt/tomcat/examples/WEB-INF/classes/num/NumberGuessBean.class
/opt/tomcat/examples/WEB-INF/classes/RequestParamExample.java
/opt/tomcat/examples/WEB-INF/classes/cal/Entry.java
/opt/tomcat/examples/WEB-INF/classes/cal/TableBean.java
/opt/tomcat/examples/WEB-INF/classes/cal/TableBean.class
/opt/tomcat/examples/WEB-INF/classes/cal/JspCalendar.java
/opt/tomcat/examples/WEB-INF/classes/cal/Entries.java
/opt/tomcat/examples/WEB-INF/classes/cal/JspCalendar.class
/opt/tomcat/examples/WEB-INF/classes/cal/Entries.class
/opt/tomcat/examples/WEB-INF/classes/cal/Entry.class
/opt/tomcat/examples/WEB-INF/classes/colors/ColorGameBean.java
/opt/tomcat/examples/WEB-INF/classes/colors/ColorGameBean.class
/opt/tomcat/examples/WEB-INF/classes/CookieExample.java
/opt/tomcat/examples/WEB-INF/classes/SessionExample.class
/opt/tomcat/examples/WEB-INF/classes/SnoopServlet.java
/opt/tomcat/examples/WEB-INF/classes/RequestParamExample.class
/opt/tomcat/examples/WEB-INF/classes/CookieExample.class
/opt/tomcat/examples/WEB-INF/classes/RequestInfoExample.class
/opt/tomcat/examples/WEB-INF/classes/sessions/DummyCart.java
/opt/tomcat/examples/WEB-INF/classes/sessions/DummyCart.class
/opt/tomcat/examples/WEB-INF/classes/SessionExample.java
/opt/tomcat/examples/WEB-INF/classes/RequestInfoExample.java
/opt/tomcat/examples/WEB-INF/classes/servletToJsp.java
/opt/tomcat/examples/WEB-INF/classes/SnoopServlet.class
/opt/tomcat/examples/WEB-INF/classes/RequestHeaderExample.class
/opt/tomcat/examples/WEB-INF/classes/dates/JspCalendar.class
/opt/tomcat/examples/WEB-INF/classes/dates/JspCalendar.java
/opt/tomcat/examples/WEB-INF/classes/HelloWorldExample.java
/opt/tomcat/examples/WEB-INF/classes/HelloWorldExample.class
/opt/tomcat/examples/WEB-INF/jsp/applet/Clock2.java
/opt/tomcat/examples/WEB-INF/jsp/example-taglib.tld
/opt/tomcat/examples/WEB-INF/web.xml
/opt/tomcat/examples/jsp/plugin/applet/Clock2.class
/opt/tomcat/examples/jsp/plugin/applet/Clock2.java
/opt/tomcat/examples/jsp/plugin/plugin.txt
/opt/tomcat/examples/jsp/plugin/plugin.html
/opt/tomcat/examples/jsp/plugin/plugin.jsp
/opt/tomcat/examples/jsp/cal/cal2.txt
/opt/tomcat/examples/jsp/cal/TableBean.txt
/opt/tomcat/examples/jsp/cal/calendar.html
/opt/tomcat/examples/jsp/cal/cal2.jsp
/opt/tomcat/examples/jsp/cal/Entries.txt
/opt/tomcat/examples/jsp/cal/JspCalendar.txt
/opt/tomcat/examples/jsp/cal/Entry.txt
/opt/tomcat/examples/jsp/cal/cal1.txt
/opt/tomcat/examples/jsp/cal/cal1.jsp
/opt/tomcat/examples/jsp/cal/login.html
/opt/tomcat/examples/jsp/jsptoserv/jsptoservlet.jsp
/opt/tomcat/examples/jsp/jsptoserv/jts.txt
/opt/tomcat/examples/jsp/jsptoserv/stj.txt
/opt/tomcat/examples/jsp/jsptoserv/jts.html
/opt/tomcat/examples/jsp/jsptoserv/hello.jsp
/opt/tomcat/examples/jsp/dates/date.html
/opt/tomcat/examples/jsp/dates/date.txt
/opt/tomcat/examples/jsp/dates/date.jsp
/opt/tomcat/examples/jsp/colors/ColorGameBean.html
/opt/tomcat/examples/jsp/colors/colors.html
/opt/tomcat/examples/jsp/colors/colors.txt
/opt/tomcat/examples/jsp/colors/colrs.jsp
/opt/tomcat/examples/jsp/colors/clr.html
/opt/tomcat/examples/jsp/error/er.html
/opt/tomcat/examples/jsp/error/err.txt
/opt/tomcat/examples/jsp/error/err.jsp
/opt/tomcat/examples/jsp/error/error.html
/opt/tomcat/examples/jsp/error/errorpge.jsp
/opt/tomcat/examples/jsp/sessions/DummyCart.html
/opt/tomcat/examples/jsp/sessions/carts.txt
/opt/tomcat/examples/jsp/sessions/carts.html
/opt/tomcat/examples/jsp/sessions/crt.html
/opt/tomcat/examples/jsp/sessions/carts.jsp
/opt/tomcat/examples/jsp/forward/one.jsp
/opt/tomcat/examples/jsp/forward/fwd.html
/opt/tomcat/examples/jsp/forward/forward.txt
/opt/tomcat/examples/jsp/forward/forward.jsp
/opt/tomcat/examples/jsp/forward/two.html
/opt/tomcat/examples/jsp/checkbox/checkresult.txt
/opt/tomcat/examples/jsp/checkbox/CheckTest.html
/opt/tomcat/examples/jsp/checkbox/checkresult.jsp
/opt/tomcat/examples/jsp/checkbox/check.html
/opt/tomcat/examples/jsp/checkbox/cresult.html
/opt/tomcat/examples/jsp/simpletag/foo.html
/opt/tomcat/examples/jsp/simpletag/foo.txt
/opt/tomcat/examples/jsp/simpletag/foo.jsp
/opt/tomcat/examples/jsp/include/foo.html
/opt/tomcat/examples/jsp/include/include.txt
/opt/tomcat/examples/jsp/include/include.jsp
/opt/tomcat/examples/jsp/include/foo.jsp
/opt/tomcat/examples/jsp/include/inc.html
/opt/tomcat/examples/jsp/num/numguess.html
/opt/tomcat/examples/jsp/num/numguess.txt
/opt/tomcat/examples/jsp/num/numguess.jsp
/opt/tomcat/examples/jsp/snp/snoop.txt
/opt/tomcat/examples/jsp/snp/snoop.jsp
/opt/tomcat/examples/jsp/snp/snoop.html
/opt/tomcat/examples/jsp/index.html
/opt/tomcat/examples/jsp/source.jsp
/opt/tomcat/examples/servlets/sessions.html
/opt/tomcat/examples/servlets/helloworld.html
/opt/tomcat/examples/servlets/reqinfo.html
/opt/tomcat/examples/servlets/cookies.html
/opt/tomcat/examples/servlets/reqparams.html
/opt/tomcat/examples/servlets/index.html
/opt/tomcat/examples/servlets/reqheaders.html
/opt/tomcat/examples/images/code.gif
/opt/tomcat/examples/images/return.gif
/opt/tomcat/examples/images/execute.gif
/opt/tomcat/webpages/docs/api/javax/servlet/jsp/package-summary.html
/opt/tomcat/webpages/docs/api/javax/servlet/jsp/tagext/VariableInfo.html
/opt/tomcat/webpages/docs/api/javax/servlet/jsp/tagext/class-use/Tag.html
/opt/tomcat/webpages/docs/api/javax/servlet/jsp/tagext/class-use/VariableInfo.html
/opt/tomcat/webpages/docs/api/javax/servlet/jsp/tagext/class-use/TagAttributeInfo.html
/opt/tomcat/webpages/docs/api/javax/servlet/jsp/tagext/class-use/TagSupport.html
/opt/tomcat/webpages/docs/api/javax/servlet/jsp/tagext/class-use/TagData.html
/opt/tomcat/webpages/docs/api/javax/servlet/jsp/tagext/class-use/TagExtraInfo.html
/opt/tomcat/webpages/docs/api/javax/servlet/jsp/tagext/class-use/TagLibraryInfo.html
/opt/tomcat/webpages/docs/api/javax/servlet/jsp/tagext/class-use/BodyContent.html
/opt/tomcat/webpages/docs/api/javax/servlet/jsp/tagext/class-use/TagInfo.html
/opt/tomcat/webpages/docs/api/javax/servlet/jsp/tagext/class-use/BodyJspWriter.html
/opt/tomcat/webpages/docs/api/javax/servlet/jsp/tagext/class-use/BodyTagSupport.html
/opt/tomcat/webpages/docs/api/javax/servlet/jsp/tagext/class-use/BodyTag.html
/opt/tomcat/webpages/docs/api/javax/servlet/jsp/tagext/TagLibraryInfo.html
/opt/tomcat/webpages/docs/api/javax/servlet/jsp/tagext/BodyJspWriter.html
/opt/tomcat/webpages/docs/api/javax/servlet/jsp/tagext/TagAttributeInfo.html
/opt/tomcat/webpages/docs/api/javax/servlet/jsp/tagext/TagExtraInfo.html
/opt/tomcat/webpages/docs/api/javax/servlet/jsp/tagext/BodyTagSupport.html
/opt/tomcat/webpages/docs/api/javax/servlet/jsp/tagext/Tag.html
/opt/tomcat/webpages/docs/api/javax/servlet/jsp/tagext/TagInfo.html
/opt/tomcat/webpages/docs/api/javax/servlet/jsp/tagext/TagSupport.html
/opt/tomcat/webpages/docs/api/javax/servlet/jsp/tagext/package-frame.html
/opt/tomcat/webpages/docs/api/javax/servlet/jsp/tagext/package-use.html
/opt/tomcat/webpages/docs/api/javax/servlet/jsp/tagext/package-summary.html
/opt/tomcat/webpages/docs/api/javax/servlet/jsp/tagext/package-tree.html
/opt/tomcat/webpages/docs/api/javax/servlet/jsp/tagext/BodyContent.html
/opt/tomcat/webpages/docs/api/javax/servlet/jsp/tagext/BodyTag.html
/opt/tomcat/webpages/docs/api/javax/servlet/jsp/tagext/TagData.html
/opt/tomcat/webpages/docs/api/javax/servlet/jsp/JspEngineInfo.html
/opt/tomcat/webpages/docs/api/javax/servlet/jsp/class-use/JspFactory.html
/opt/tomcat/webpages/docs/api/javax/servlet/jsp/class-use/JspWriter.html
/opt/tomcat/webpages/docs/api/javax/servlet/jsp/class-use/JspEngineInfo.html
/opt/tomcat/webpages/docs/api/javax/servlet/jsp/class-use/JspPage.html
/opt/tomcat/webpages/docs/api/javax/servlet/jsp/class-use/HttpJspPage.html
/opt/tomcat/webpages/docs/api/javax/servlet/jsp/class-use/JspError.html
/opt/tomcat/webpages/docs/api/javax/servlet/jsp/class-use/PageContext.html
/opt/tomcat/webpages/docs/api/javax/servlet/jsp/class-use/JspException.html
/opt/tomcat/webpages/docs/api/javax/servlet/jsp/JspFactory.html
/opt/tomcat/webpages/docs/api/javax/servlet/jsp/JspError.html
/opt/tomcat/webpages/docs/api/javax/servlet/jsp/JspWriter.html
/opt/tomcat/webpages/docs/api/javax/servlet/jsp/JspPage.html
/opt/tomcat/webpages/docs/api/javax/servlet/jsp/package-frame.html
/opt/tomcat/webpages/docs/api/javax/servlet/jsp/JspException.html
/opt/tomcat/webpages/docs/api/javax/servlet/jsp/HttpJspPage.html
/opt/tomcat/webpages/docs/api/javax/servlet/jsp/package-tree.html
/opt/tomcat/webpages/docs/api/javax/servlet/jsp/package-use.html
/opt/tomcat/webpages/docs/api/javax/servlet/jsp/PageContext.html
/opt/tomcat/webpages/docs/api/javax/servlet/class-use/ServletConfig.html
/opt/tomcat/webpages/docs/api/javax/servlet/class-use/RequestDispatcher.html
/opt/tomcat/webpages/docs/api/javax/servlet/class-use/ServletRequest.html
/opt/tomcat/webpages/docs/api/javax/servlet/class-use/ServletOutputStream.html
/opt/tomcat/webpages/docs/api/javax/servlet/class-use/Servlet.html
/opt/tomcat/webpages/docs/api/javax/servlet/class-use/ServletResponse.html
/opt/tomcat/webpages/docs/api/javax/servlet/class-use/SingleThreadModel.html
/opt/tomcat/webpages/docs/api/javax/servlet/class-use/ServletException.html
/opt/tomcat/webpages/docs/api/javax/servlet/class-use/ServletInputStream.html
/opt/tomcat/webpages/docs/api/javax/servlet/class-use/ServletContext.html
/opt/tomcat/webpages/docs/api/javax/servlet/class-use/GenericServlet.html
/opt/tomcat/webpages/docs/api/javax/servlet/class-use/UnavailableException.html
/opt/tomcat/webpages/docs/api/javax/servlet/http/class-use/HttpServletRequest.html
/opt/tomcat/webpages/docs/api/javax/servlet/http/class-use/HttpUtils.html
/opt/tomcat/webpages/docs/api/javax/servlet/http/class-use/HttpServletResponse.html
/opt/tomcat/webpages/docs/api/javax/servlet/http/class-use/Cookie.html
/opt/tomcat/webpages/docs/api/javax/servlet/http/class-use/HttpServlet.html
/opt/tomcat/webpages/docs/api/javax/servlet/http/class-use/HttpSessionContext.html
/opt/tomcat/webpages/docs/api/javax/servlet/http/class-use/HttpSession.html
/opt/tomcat/webpages/docs/api/javax/servlet/http/class-use/HttpSessionBindingListener.html
/opt/tomcat/webpages/docs/api/javax/servlet/http/class-use/HttpSessionBindingEvent.html
/opt/tomcat/webpages/docs/api/javax/servlet/http/HttpSessionBindingListener.html
/opt/tomcat/webpages/docs/api/javax/servlet/http/HttpServletRequest.html
/opt/tomcat/webpages/docs/api/javax/servlet/http/HttpUtils.html
/opt/tomcat/webpages/docs/api/javax/servlet/http/HttpServlet.html
/opt/tomcat/webpages/docs/api/javax/servlet/http/HttpServletResponse.html
/opt/tomcat/webpages/docs/api/javax/servlet/http/package-summary.html
/opt/tomcat/webpages/docs/api/javax/servlet/http/HttpSessionContext.html
/opt/tomcat/webpages/docs/api/javax/servlet/http/HttpSession.html
/opt/tomcat/webpages/docs/api/javax/servlet/http/package-frame.html
/opt/tomcat/webpages/docs/api/javax/servlet/http/HttpSessionBindingEvent.html
/opt/tomcat/webpages/docs/api/javax/servlet/http/package-use.html
/opt/tomcat/webpages/docs/api/javax/servlet/http/package-tree.html
/opt/tomcat/webpages/docs/api/javax/servlet/http/Cookie.html
/opt/tomcat/webpages/docs/api/javax/servlet/package-tree.html
/opt/tomcat/webpages/docs/api/javax/servlet/RequestDispatcher.html
/opt/tomcat/webpages/docs/api/javax/servlet/package-frame.html
/opt/tomcat/webpages/docs/api/javax/servlet/ServletContext.html
/opt/tomcat/webpages/docs/api/javax/servlet/GenericServlet.html
/opt/tomcat/webpages/docs/api/javax/servlet/ServletOutputStream.html
/opt/tomcat/webpages/docs/api/javax/servlet/ServletException.html
/opt/tomcat/webpages/docs/api/javax/servlet/ServletInputStream.html
/opt/tomcat/webpages/docs/api/javax/servlet/SingleThreadModel.html
/opt/tomcat/webpages/docs/api/javax/servlet/ServletRequest.html
/opt/tomcat/webpages/docs/api/javax/servlet/package-summary.html
/opt/tomcat/webpages/docs/api/javax/servlet/Servlet.html
/opt/tomcat/webpages/docs/api/javax/servlet/package-use.html
/opt/tomcat/webpages/docs/api/javax/servlet/ServletConfig.html
/opt/tomcat/webpages/docs/api/javax/servlet/UnavailableException.html
/opt/tomcat/webpages/docs/api/javax/servlet/ServletResponse.html
/opt/tomcat/webpages/docs/api/serialized-form.html
/opt/tomcat/webpages/docs/api/package-list
/opt/tomcat/webpages/docs/api/packages.html
/opt/tomcat/webpages/docs/api/index.html
/opt/tomcat/webpages/docs/api/help-doc.html
/opt/tomcat/webpages/docs/api/stylesheet.css
/opt/tomcat/webpages/docs/api/deprecated-list.html
/opt/tomcat/webpages/docs/api/overview-summary.html
/opt/tomcat/webpages/docs/api/overview-frame.html
/opt/tomcat/webpages/docs/api/index-all.html
/opt/tomcat/webpages/docs/api/overview-tree.html
/opt/tomcat/webpages/docs/api/allclasses-frame.html
/opt/tomcat/webpages/WEB-INF/classes/SnoopServlet.class
/opt/tomcat/webpages/WEB-INF/classes/SnoopServlet.java
/opt/tomcat/webpages/WEB-INF/web.xml
/opt/tomcat/webpages/tomcat.gif
/opt/tomcat/webpages/index.html
/opt/tomcat/tomcatEnv.bat
/opt/tomcat/shutdown.bat
/opt/tomcat/ant
/opt/tomcat/startup.sh
/opt/tomcat/startup.bat
/opt/tomcat/env.tomcat
/opt/tomcat/tomcat.sh
/opt/tomcat/tomcat.bat
/opt/tomcat/shutdown.sh
/opt/tomcat/server.xml
/opt/tomcat/README
/opt/tomcat/FAQ
/opt/tomcat/webserver.jar

%changelog
