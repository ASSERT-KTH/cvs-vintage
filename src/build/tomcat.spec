Summary: Apache's servlet engine
Name: tomcat
Version: 3.1
%define packname jakarta-tomcat
Release: 3
Vendor: Apache Software Foundation
Group: System Environment/Daemons
Copyright: Apache - free
Icon: tomcat.gif
Url: http://jakarta.apache.org
Provides: tomcat 
#BuildArchitectures: noarch
Requires: ant
BuildRequires: ant apache-devel
Source: http://jakarta.apache.org/builds/tomcat/release/v%{version}/src/%{packname}.tar.gz
Source1: tomcat.init
Source2: tomcat.logrotate
Patch: Ajp12ConnectionHandler.diff
BuildRoot: /var/tmp/%{name}-root

%description
Develop Web applications in Java.

%package doc
Group: Applications/Internet
Requires: webserver
Summary: Online manual for tomcat

%description doc
Documentation for tomcat.

%package jserv
Group: Applications/Internet
Requires: apache
Summary: add mod_jserv support to apache

%description jserv
mod_jserv support for apache

%prep
rm -rf $RPM_BUILD_ROOT
rm -rf $RPM_BUILD_DIR/%{packname}

%setup -n %{packname}
%patch

%build
ant -Dant.home /opt/ant -Dtomcat.home $RPM_BUILD_DIR/%{packname} -Dtomcat.build $RPM_BUILD_DIR/%{packname}/build
cd src/native/apache/jserv
/usr/sbin/apxs -c -o mod_jserv.so *.c


%install
cd $RPM_BUILD_DIR/%{packname}
mkdir -p $RPM_BUILD_ROOT/home/httpd/html/manual/%{name}
ant -Dant.home /opt/ant -Dtomcat.build $RPM_BUILD_DIR/%{packname}/build -Dtomcat.home $RPM_BUILD_ROOT/opt/%{name} dist 
mkdir -p $RPM_BUILD_ROOT/usr/lib/apache
install src/native/apache/jserv/mod_jserv.so $RPM_BUILD_ROOT/usr/lib/apache/mod_jserv_tomcat.so

cd $RPM_BUILD_ROOT/home/httpd/html/manual/%{name}
jar xvf $RPM_BUILD_ROOT/opt/%{name}/webapps/ROOT.war

# sysv init and logging
mkdir -p $RPM_BUILD_ROOT/etc/rc.d/init.d
install $RPM_SOURCE_DIR/tomcat.logrotate $RPM_BUILD_ROOT/opt/tomcat/conf
install $RPM_SOURCE_DIR/tomcat.init $RPM_BUILD_ROOT/etc/rc.d/init.d/tomcat


%clean
rm -rf $RPM_BUILD_ROOT
rm -rf $RPM_BUILD_DIR/%{packname}

%post
rm -f /usr/bin/tomcat
ln -s /opt/tomcat/bin/tomcat.sh /usr/bin/tomcat
/sbin/chkconfig --add tomcat

echo ""
echo ""
echo "Don't forget to set JAVA_HOME in /etc/rc.d/init.d/tomcat"
echo "to your JDK/JRE directory since we didn't have these info"
echo "at boot time."
echo "As supplied we assume you're using IBM JDK 1.1.8"
echo ""

%post jserv
cp -f /opt/tomcat/conf/tomcat.logrotate /etc/logrotate.d/tomcat

if [ -f /etc/httpd/conf/httpd.conf ] ; then
    if ! grep -q '.*LoadModule *jserv_module *lib/apache/mod_jserv_tomcat.so' /etc/httpd/conf/httpd.conf ; then
        sed "s|^\LoadModule *rewrite_module *lib/apache/mod_rewrite.so\$|LoadModule jserv_module       lib/apache/mod_jserv_tomcat.so\\
LoadModule rewrite_module     lib/apache/mod_rewrite.so|" < /etc/httpd/conf/httpd.conf > /etc/httpd/conf/httpd.conf-
        mv -f /etc/httpd/conf/httpd.conf- /etc/httpd/conf/httpd.conf
    fi

    if ! grep -q '.*AddModule *mod_jserv.c' /etc/httpd/conf/httpd.conf ; then
      sed "s|^\AddModule *mod_rewrite.c\$|AddModule mod_jserv.c\\
AddModule mod_rewrite.c|" < /etc/httpd/conf/httpd.conf > /etc/httpd/conf/httpd.conf-
        mv -f /etc/httpd/conf/httpd.conf- /etc/httpd/conf/httpd.conf
    fi

    if ! grep -q '.*Include /opt/tomcat/conf/tomcat.conf' /etc/httpd/conf/httpd.conf ; then
        cat >>/etc/httpd/conf/httpd.conf<<EOT
<IfModule mod_jserv.c>
ApJServLogFile /var/log/httpd/mod_jserv_tomcat.log
Include /opt/tomcat/conf/tomcat.conf
</IfModule>
EOT
    fi
fi

%preun
if [ $1 = 0 ]; then
    if [ -f /usr/bin/tomcat ]; then
        rm -f /usr/bin/tomcat
    fi

    if [ -f /var/lock/subsys/tomcat ]; then
        /etc/rc.d/init.d/tomcat stop
    fi
    if [ -f /etc/rc.d/init.d/tomcat ]; then
        /sbin/chkconfig --del tomcat
    fi

fi

%preun jserv
if [ $1 = 0 ]; then
# remove existing map if any
	sed -e '/^Include \/opt\/tomcat\/conf\/tomcat.conf/d' \
        -e '/^ApJServLogFile \/var\/log\/httpd\/mod_jserv_tomcat.log/d' \
        -e '/^LoadModule jserv_module lib\/apache\/mod_jserv_tomcat.so/d' \
        -e '/^AddModule mod_jserv.c/d' \
      < /etc/httpd/conf/httpd.conf \
      > /etc/httpd/conf/httpd.conf-
  	mv /etc/httpd/conf/httpd.conf- \
     	/etc/httpd/conf/httpd.conf

	rm -f /etc/logrotate.d/tomcat
fi

%files
%defattr(644 root root 755)
%attr(755,root,root)  %dir 				 /opt/tomcat
%attr(755,root,root)  %dir 				 /opt/tomcat/bin
%attr(755,root,root)  %dir 				 /opt/tomcat/conf
%attr(755,root,root)  %dir 				 /opt/tomcat/lib
%attr(755,root,root)  %dir 				 /opt/tomcat/webapps
%attr( - ,root,root)       				 /opt/tomcat/bin/*
%attr( - ,root,root)  %config(noreplace) /opt/tomcat/conf/*
%attr(755,root,root)  %config 			 /etc/rc.d/init.d/*
%attr( - ,root,root)       				 /opt/tomcat/lib/*
%attr( - ,root,root)       				 /opt/tomcat/webapps/*
%attr( - ,root,root)  %doc 				 build/doc/* LICENSE README RELE* TODO

%files doc
%defattr(644 root root 755)
%attr( - ,root,root)                /home/httpd/html/manual/%{name}

%files jserv
%attr(755,root,root)  %dir               /usr/lib/apache
%attr( - ,root,root)                     /usr/lib/apache/*


%changelog
* Mon May 22 2000 Henri Gomez <hgomez@slib.fr>
- v3.1-3
- apply SSL patch (Ajp12ConnectionHandler.java)

* Thu May 04 2000 Henri Gomez <hgomez@slib.fr>
- v3.1-2
- Added JAVA_HOME to tomcat.init, necessary at init
  time to determine where is your JAVA_HOME
 
* Tue May 02 2000 Henri Gomez <hgomez@slib.fr>
- v3.1 final release
- Compiled on Redhat 6.1 with IBM JDK 1.1.8 (20000328)
  for apache 1.3.12 + mod_ssl 2.6.4 (EAPI support).

* Thu Apr 13 2000 Henri Gomez <hgomez@slib.fr>
- v3.1_rc1
- RPM didn't replace configuration files of tomcat in /opt/tomcat/conf 
  This choice is to avoid trashing your own settings. 
  Warning, since v3.1_rc1, you need to to set home vars in ContextManager 
  in file /opt/tomcat/conf/server.xml like this :
  <ContextManager debug="0" workDir="work" home="/opt/tomcat">
- New in v3.1_rc1 is that tomcat generate dynamically a conf file,
  tomcat-apache.conf, in /opt/tomcat/conf
- compiled on Redhat 6.1 with IBM JDK 1.1.8 (20000328)
  for apache 1.3.12 + mod_ssl 2.6.2 (EAPI support).

* Thu Mar 09 2000 Henri Gomez <gomez@slib.fr>
- v3.1b1-2
- added missing tomcat.init (oups)
- tomcat home is in /opt/tomcat but there is a link
  /usr/bin/tomcat to allow any user to start it since
  now it didn't use privilegied port (8007 & 8080).
  This may be an issue if you only want root start it.
- added logrotate support for apache/jserv side. 
  Will add tomcat log support when we could signal
  tomcat to rotate logs.
- compiled on Redhat 6.1 with IBM JDK 1.1.8 (19991220) 
  for apache 1.3.12 + mod_ssl 2.6.2 (EAPI support).

* Thu Mar 09 2000 Henri Gomez <gomez@slib.fr>
- v3.1b1-1
- compile and install mod_jserv for apache
  renamed to mod_jserv_tomcat.so to avoid conflict
  with mod_jserv.so from jserv 1.1 (java.apache.org)

* Wed Mar 08 2000 Henri Gomez <gomez@slib.fr>
- v3.1b1

* Tue Feb 29 2000 Henri Gomez <gomez@slib.fr>
- v3.1_m2rc2

* Fri Feb 25 2000 Henri Gomez <gomez@slib.fr>
- v3.1_m2rc1

* Fri Jan 28 2000 Henri Gomez <gomez@slib.fr>
- v3.1_m1

* Tue Jan 18 2000 Henri Gomez <gomez@slib.fr>
- first RPM of tomcat 3.1 m1_rc1

* Tue Jan  4 2000 Henri Gomez <gomez@slib.fr>
- moved from /opt/jakarta/jakarta-tomcat to /opt/tomcat

* Tue Jan  4 2000 Henri Gomez <gomez@slib.fr>
- CVS 4 Jan 2000

* Thu Dec 30 1999 Henri Gomez <gomez@slib.fr>
- Initial release for jakarta-tomcat cvs


