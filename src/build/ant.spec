Summary: ant
Name: ant
Version: 0.3.1
%define packversion 3.1
%define packname jakarta-ant
Release: 1
Group: Development/Tools
Copyright: Apache - free
Provides: ant 
Url: http://jakarta.apache.org
BuildArchitectures: noarch
Source: http://jakarta.apache.org/builds/tomcat/release/v%{packversion}/src/%{packname}.tar.gz
BuildRoot: /var/tmp/%{name}-root

%description
Platform-independent build tool for java.
Ant is a Java based build system
Ant is used by apache jakarta&xml projects.

%package doc
Group: Applications/Internet
Requires: webserver
Summary: Online manual for ant

%description doc
Documentation for ant, Platform-independent build tool for java.
Used by Apache Group for jakarta and xml projects.

%prep
rm -rf $RPM_BUILD_ROOT

%setup -n %{packname}

%build
export CLASSPATH=
sh bootstrap.sh

%install
export CLASSPATH=
mkdir -p $RPM_BUILD_ROOT/opt/%{name}
sh build.sh -Dant.dist.dir $RPM_BUILD_ROOT/opt/%{name} dist
#mkdir -p $RPM_BUILD_ROOT/opt/%{name}/bin
#mkdir -p $RPM_BUILD_ROOT/opt/%{name}/lib
#sh build.sh -Dant.dist.dir $RPM_BUILD_ROOT/opt/%{name} dist
mkdir -p $RPM_BUILD_ROOT/home/httpd/html/manual/%{name}
cp -prf $RPM_BUILD_ROOT/opt/%{name}/docs/* $RPM_BUILD_ROOT/home/httpd/html/manual/%{name}

%clean
rm -rf $RPM_BUILD_ROOT
rm -rf ${RPM_BUILD_DIR}/%{packname}

%post
rm -f /usr/bin/ant
ln -s /opt/%{name}/bin/ant /usr/bin/ant

%preun
if [ $1 = 0 ]; then
	if [ -f /usr/bin/ant ]; then
		rm -f /usr/bin/ant
	fi
fi
  
%files
%doc LICENSE README TODO
%dir /opt/%{name}
/opt/%{name}/bin
/opt/%{name}/lib

%files doc
%defattr(644 root root 755)
%attr( - ,root,root)                /home/httpd/html/manual/%{name}

%changelog
* Tue May 02 2000 Henri Gomez <hgomez@slib.fr>
- v0.3.1
- From jakarta/tomcat 3.1 final release. Need now to
  have a consistent version number ;-)
- Fixed classpath problem at compile time by cleaning CLASSPATH before
  build/install stages.
- Compiled on Redhat 6.1 with IBM JDK 1.1.8 (20000328)
 
* Thu Apr 13 2000 Henri Gomez <hgomez@slib.fr>
- v0.3.1_rc1
- Version renamed to 0.3.1_rc1 to follow Sam Ruby (rubys@us.ibm.com)
  recommandation since the next major release will be 1.0
 
* Wed Mar 08 2000 Henri Gomez <gomez@slib.fr>
- v3.1b1
- removed moo from ant RPM. Will be now in watchdog RPM.

* Tue Feb 29 2000 Henri Gomez <gomez@slib.fr>
- v3.1_m2rc2

* Fri Feb 25 2000 Henri Gomez <gomez@slib.fr>
- v3.1_m2rc1
- moo is no more in the tar packages, will be released
  in another RPM
- added doc package

* Fri Jan 28 2000 Henri Gomez <gomez@slib.fr>
- v3.1_m1

* Tue Jan 18 2000 Henri Gomez <gomez@slib.fr>
- first RPM of v3.1_m1_rc1 

* Tue Jan  4 2000 Henri Gomez <gomez@slib.fr>
- moved from /opt/jakarta/jakarta-tools to /opt/ant

* Tue Jan  4 2000 Henri Gomez <gomez@slib.fr>
- CVS 4 Jan 2000 
- added servlet.jar from tomcat in SRPM
   to allow first build of moo.

* Thu Dec 30 1999 Henri Gomez <gomez@slib.fr>
- Initial release for jakarta-tools cvs


