Summary: Columba, Java Email Client
%define version 0.10.1
License: MPL
Group: EMail
Name: columba
#Prefix: /opt
#Prefix: /etc
#Prefix: /usr
Provides: columba
Release: 0
Source: columba-unix-%{version}-bin.tar.gz
URL: http://columba.sourceforge.net
Version: %{version}
Buildroot: /tmp/columba_rpm

%description
Columba is an Email Client written in Java, featuring a user-friendly graphical interface with wizards and internalionalization support.
Its a powerful email management tool with features to enhance your productivity and communication. So, take control of your email before it takes control of you!

%prep
%setup -q

%build

%install
rm -rf $RPM_BUILD_ROOT/opt/columba
mkdir -p $RPM_BUILD_ROOT/opt/columba
rm -rf $RPM_BUILD_ROOT/usr/bin
mkdir -p $RPM_BUILD_ROOT/usr/bin
rm -rf $RPM_BUILD_ROOT/etc
mkdir -p $RPM_BUILD_ROOT/etc
cp -R * $RPM_BUILD_ROOT/opt/columba
ln -sf /opt/columba/run.sh $RPM_BUILD_ROOT/usr/bin/columba


%clean
rm -rf $RPM_BUILD_ROOT

%files
%defattr(-,root,root)
/usr/bin/columba
/opt/columba/*
#%config(noreplace) /etc/columba
