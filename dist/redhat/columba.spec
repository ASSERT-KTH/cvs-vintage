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
rm -rf $RPM_BUILD_ROOT/opt/columba-%{version}
mkdir -p $RPM_BUILD_ROOT/opt/columba-%{version}
rm -rf $RPM_BUILD_ROOT/usr/bin
mkdir -p $RPM_BUILD_ROOT/usr/bin
rm -rf $RPM_BUILD_ROOT/etc
mkdir -p $RPM_BUILD_ROOT/etc
cp -R * $RPM_BUILD_ROOT/opt/columba-%{version}
ln -sf /opt/columba-%{version}/run.sh $RPM_BUILD_ROOT/usr/bin/columba


%clean
rm -rf $RPM_BUILD_ROOT

%files
%defattr(-,root,root)
/usr/bin/columba
/opt/columba-%{version}/*
