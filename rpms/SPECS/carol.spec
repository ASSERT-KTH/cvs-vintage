%define name		carol
%define version		2.0.4
%define release		1jpp
%define	section		free


Name:		%{name}
Summary:	CAROL: Common Architecture for RMI ObjectWeb Layer
Url:		http://carol.objectweb.org/
Version:	%{version}
Release:	%{release}
Epoch:		0
License:	LGPL
Group:		Development/Libraries/Java
Vendor:		JPackage Project
Distribution:	JPackage
BuildArch:	noarch
Source0:	carol-%{version}-src.tgz

BuildRequires:  jpackage-utils >= 0:1.5
BuildRequires:	ant >= 0:1.6
BuildRequires:  %{_bindir}/perl
BuildRequires:	objectweb-anttask
BuildRequires:  jakarta-commons-logging >= 1.0.4
# Commons-collections is required by irmi
BuildRequires:  commons-collections >= 3.1
# FIXME Using private version of JacORB
# JONAS_JACORB_1_0_3
#BuildRequires:  jacorb >= 2.2.1
# FIXME needs 2.2.7
BuildRequires:  jgroups >= 2.2.6
BuildRequires:  jonathan-core >= 4.1
BuildRequires:  jonathan-jeremie >= 4.2.1
BuildRequires:  junit >= 3.8.1
# FIXME needs 1.9
#BuildRequires:  monolog >= 1.9
BuildRequires:  mx4j >= 3.0.1
BuildRequires:  oldkilim >= 1.1.3
BuildRequires:  nanoxml-lite >= 2.2.1
BuildRequires:  velocity >= 1.4
Requires:  jakarta-commons-logging >= 1.0.4
# Commons-collections is required by irmi
Requires:  commons-collections >= 3.1
# FIXME Using private version of JacORB
# JONAS_JACORB_1_0_3
#Requires:  jacorb >= 2.2.1
# FIXME needs 2.2.7
Requires:  jgroups >= 2.2.6
Requires:  jonathan-core >= 4.1
Requires:  jonathan-jeremie >= 4.2.1
Requires:  junit >= 3.8.1
# FIXME needs 1.9
#Requires:  monolog >= 1.9
Requires:  mx4j >= 3.0.1
Requires:  oldkilim >= 1.1.3
Requires:  nanoxml-lite >= 2.2.1
Requires:  velocity >= 1.4
BuildRoot:	%{_tmppath}/%{name}-%{version}-buildroot

%description
CAROL is a library allowing to use different RMI implementations. 
Thanks to CAROL, a Java server application can be independent of 
RMI implementations and accessible simultaneously by RMI clients 
using different RMI implementations. CAROL allows to design, 
implement, compile, package, deploy, and execute distributed 
applications compliant with the RMI model.


%package javadoc
Summary:	Javadoc for %{name}
Group:		Development/Documentation

%description javadoc
Javadoc for %{name}.

%prep
%setup -q -n %{name}
chmod -R go=u-w *

# FIXME Using private version of JacORB
# JONAS_JACORB_1_0_3
# FIXME Get ow_util_log_api from Monolog 1.9
# FIXME Build irmi from sources
find . -name "*.jar" \
       -not -name "jacorb.jar" \
       -not -name "ow_util_log_api.jar" \
       -not -name "irmi.jar" \
       -exec rm -f {} \;

%build
pushd externals

ln -sf $(build-classpath commons-logging-api) .
ln -sf $(build-classpath commons-collections) .
ln -sf $(build-classpath mx4j/mx4j) .
ln -sf $(build-classpath jgroups) .
# FIXME Using private version of JacORB
# JONAS_JACORB_1_0_3
#ln -sf $(build-classpath jacorb/jacorb) .
ln -sf $(build-classpath junit) .
ln -sf $(build-classpath velocity) .

cd jeremie
ln -sf $(build-classpath jonathan-core) .
ln -sf $(build-classpath jonathan-jeremie) .
ln -sf $(build-classpath nanoxml-lite) .
ln -sf $(build-classpath oldkilim) .
# FIXME needs 1.9
#ln -sf $(build-classpath monolog/ow_util_log_api) .
popd

export OPT_JAR_LIST="objectweb-anttask"
ant dist jdoc

%install
# jars
install -d -m 755 $RPM_BUILD_ROOT%{_javadir}/%{name}

install -m 644 output/dist/lib/ow_%{name}.jar \
        $RPM_BUILD_ROOT%{_javadir}/%{name}/ow_%{name}-%{version}.jar
install -m 644 output/dist/lib/ow_%{name}-all.jar \
        $RPM_BUILD_ROOT%{_javadir}/%{name}/ow_%{name}-all-%{version}.jar
install -m 644 output/dist/lib/ow_%{name}_cmic.jar \
        $RPM_BUILD_ROOT%{_javadir}/%{name}/ow_%{name}-%{version}_cmic.jar
install -m 644 output/dist/lib/ow_%{name}_cmi.jar \
        $RPM_BUILD_ROOT%{_javadir}/%{name}/ow_%{name}-%{version}_cmi.jar
install -m 644 output/dist/lib/ow_%{name}_iiop_delegate.jar \
        $RPM_BUILD_ROOT%{_javadir}/%{name}/ow_%{name}-%{version}_iiop_delegate.jar
(cd $RPM_BUILD_ROOT%{_javadir}/%{name} && for jar in *-%{version}*; do ln -sf ${jar} `echo $jar| sed  "s|-%{version}||g"`; done)

# javadoc
install -d -m 755 $RPM_BUILD_ROOT%{_javadocdir}/%{name}-%{version}
cp -pr output/dist/jdoc/* $RPM_BUILD_ROOT%{_javadocdir}/%{name}-%{version}
ln -s %{name}-%{version} $RPM_BUILD_ROOT%{_javadocdir}/%{name} # ghost symlink

%clean
rm -rf $RPM_BUILD_ROOT

%files
%defattr(-,root,root)
%dir %{_javadir}/%{name}
%{_javadir}/%{name}/ow_carol-%{version}.jar
%{_javadir}/%{name}/ow_carol.jar
%{_javadir}/%{name}/ow_carol-%{version}-all.jar
%{_javadir}/%{name}/ow_carol-all.jar
%{_javadir}/%{name}/ow_carol-%{version}_cmic.jar
%{_javadir}/%{name}/ow_carol_cmic.jar
%{_javadir}/%{name}/ow_carol-%{version}_cmi.jar
%{_javadir}/%{name}/ow_carol_cmi.jar
%{_javadir}/%{name}/ow_carol-%{version}_iiop_delegate.jar
%{_javadir}/%{name}/ow_carol_iiop_delegate.jar


%files javadoc
%defattr(-,root,root)
%{_javadocdir}/%{name}-%{version}
%ghost %doc %{_javadocdir}/%{name}

%changelog
* Tue May 17 2005 Fernando Nasser <fnasser at redhat.com> 0:2.0.4-1jpp
- Upgrade to 2.0.4
- List files by name and make sure ow_carol_iiop_delegate is copied
- Use mx4j package

* Tue May 17 2005 Fernando Nasser <fnasser at redhat.com> 0:1.8.9.4-2jpp
- Add ow_carol_iiop_delegate.jar file

* Fri May 06 2005 Fernando Nasser <fnasser at redhat.com> 0:1.8.9.4-1jpp
- Upgrade to 1.8.9.4

* Mon Mar 07 2005 Fernando Nasser <fnasser at redhat.com> 0:1.8.9.3-1jpp
- Upgrade to 1.8.9.3

* Mon Dec 20 2004 Fernando Nasser <fnasser at redhat.com> 0:1.8.4-1jpp
- Upgrade to 1.8.4

* Mon Oct 04 2004 Ralph Apel <r.apel at r-apel.de> 0:1.5.1-1jpp
- First release

