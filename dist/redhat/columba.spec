%define version @version@

Summary: Columba, Java Email Client
License: MPL
Group: EMail
Name: columba
Provides: columba
Release: 0
Source: columba-%{version}.tar
URL: http://columba.sourceforge.net
Version: %{version}
BuildRoot: /tmp/columba

%description
Columba is an Email Client written in Java, featuring a user-friendly graphical interface with wizards and internalionalization support.
Its a powerful email management tool with features to enhance your productivity and communication. So, take control of your email before it takes control of you!

%prep
rm -rf $RPM_BUILD_ROOT
%setup

%build

%install
install -m 0755 -d $RPM_BUILD_ROOT/opt/columba-%{version}
install -m 0755 -d $RPM_BUILD_ROOT/opt/columba-%{version}/lib
install -m 0755 -d $RPM_BUILD_ROOT/opt/columba-%{version}/native
install -m 0755 -d $RPM_BUILD_ROOT/opt/columba-%{version}/native/linux
install -m 0755 -d $RPM_BUILD_ROOT/opt/columba-%{version}/native/linux/lib
install -m 0755 -d $RPM_BUILD_ROOT/usr/bin
install -m 0644 columba.jar $RPM_BUILD_ROOT/opt/columba-%{version}/
install -m 0644 AUTHORS $RPM_BUILD_ROOT/opt/columba-%{version}/
install -m 0644 README $RPM_BUILD_ROOT/opt/columba-%{version}/
install -m 0644 CHANGES $RPM_BUILD_ROOT/opt/columba-%{version}/
install -m 0644 LICENSE $RPM_BUILD_ROOT/opt/columba-%{version}/
install -m 0755 run.sh $RPM_BUILD_ROOT/opt/columba-%{version}/
install -m 0644 lib/* $RPM_BUILD_ROOT/opt/columba-%{version}/lib/
install -m 0644 native/linux/lib/* $RPM_BUILD_ROOT/opt/columba-%{version}/native/linux/lib/
ln -sf /opt/columba-%{version}/run.sh $RPM_BUILD_ROOT/usr/bin/columba


%clean
rm -rf $RPM_BUILD_ROOT

%files
/usr/bin/columba
/opt/columba-%{version}/*
/opt/columba-%{version}