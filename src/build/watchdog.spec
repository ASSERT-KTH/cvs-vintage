Summary: Apache's Servlet/JSP validator
Name: watchdog
Version: 3.1
%define packname jakarta-watchdog
%define moopackname jakarta-tools
%define jname       jakarta
Release: 1
Vendor: Apache Software Foundation
Group: System Environment/Daemons
Copyright: Apache - free
Url: http://jakarta.apache.org
Provides: watchdog 
BuildArchitectures: noarch
BuildRequires: ant
BuildRequires: tomcat
Requires: ant
Requires: tomcat
Provides:  watchdog moo
Source:  http://jakarta.apache.org/builds/tomcat/release/v%{version}/src/%{packname}.zip
Source1: http://jakarta.apache.org/builds/tomcat/release/v%{version}/src/%{moopackname}.tar.gz
Patch: %{name}-%{version}.patch
BuildRoot: /var/tmp/%{name}-root


%description
Validate Apache's Servlet/JSP.

%prep
rm -rf $RPM_BUILD_ROOT
rm -rf %{jname}
mkdir -p %{jname}

#%setup -T -D -a 0 -n %{jname}
cd jakarta
unzip %{SOURCE0}
%setup -T -D -a 1 -n %{jname}
%patch

%build
cd ${RPM_BUILD_DIR}/%{jname}/%{packname}
ant -Dwatchdog.build $RPM_BUILD_DIR/%{jname}/%{packname}/build -Dwatchdog.dist $RPM_BUILD_ROOT/opt/%{name} clean
ant -Dwatchdog.build $RPM_BUILD_DIR/%{jname}/%{packname}/build -Dwatchdog.dist $RPM_BUILD_ROOT/opt/%{name} 

%install
cd $RPM_BUILD_DIR/%{jname}/%{packname}
mkdir -p $RPM_BUILD_ROOT/opt/%{name}
ant -Dwatchdog.build $RPM_BUILD_DIR/%{jname}/%{packname}/build -Dwatchdog.dist $RPM_BUILD_ROOT/opt/%{name} dist 
#chmod a+x $RPM_BUILD_ROOT/opt/%{name}/runtest.sh
#chmod a+x $RPM_BUILD_ROOT/opt/%{name}/runclient.sh


%clean
rm -rf $RPM_BUILD_ROOT
rm -rf ${RPM_BUILD_DIR}/%{name}_%{version}

%post
rm -f /usr/bin/%{name}test
rm -f /usr/bin/%{name}-runtest
rm -f /usr/bin/%{name}-runclient
ln -s /opt/%{name}/runtest.sh /usr/bin/%{name}-runtest
ln -s /opt/%{name}/runclient.sh /usr/bin/%{name}-runclient

%preun
if [ $1 = 0 ]; then
    if [ -f /usr/bin/%{name}-runtest ]; then
        rm -f /usr/bin/%{name}-runtest
    fi

    if [ -f /usr/bin/%{name}-runclient ]; then
        rm -f /usr/bin/%{name}-runclient
    fi
fi
 
%files
%dir /opt/%{name}
/opt/%{name}/*

%changelog
* Tue May 02 2000 Henri Gomez <hgomez@slib.fr>
- v3.1 final release
- Modified spec file since watchdog is available now on zip format.
- Compiled on Redhat 6.1 with IBM JDK 1.1.8 (20000328)

* Thu Apr 13 2000 Henri Gomez <hgomez@slib.fr>
- v3.1_rc1

* Wed Mar 08 2000 Henri Gomez <gomez@slib.fr>
- v3.1b1

* Fri Jan 28 2000 Henri Gomez <gomez@slib.fr>
- v3.1_m1

* Tue Jan 18 2000 Henri Gomez <gomez@slib.fr>
- first RPM of tomcat 3.1 m1_rc1

* Tue Jan  4 2000 Henri Gomez <gomez@slib.fr>
- moved from /opt/jakarta/jakarta-watchdog to /opt/watchdog

* Tue Jan  4 2000 Henri Gomez <gomez@slib.fr>
- CVS 4 Jan 2000

* Thu Dec 30 1999 Henri Gomez <gomez@slib.fr>
- Initial release for jakarta-watchdog cvs


