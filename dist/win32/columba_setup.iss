;Columba Setup file
;InnoSetup Version > 4.2.7

;This is normally defined by the ant winsetup task
;Uncomment if you want to compile the task from
;ISTools
;#define VERSION="1.0 RC1"
;#define BUNDLE_JRE
;#define JRE_FILE="j2re-1_4_2_05-windows-i586-p.exe"

;The JRE which gets bundled is expected to be in
;the extras directory
;#define JRE_SRC_PATH="extras\"

[Setup]
AppName=Columba
AppVerName=Columba {#VERSION}
AppPublisherURL=http://columba.sourceforge.net/
AppSupportURL=http://columba.sourceforge.net/
AppUpdatesURL=http://columba.sourceforge.net/
DefaultDirName={pf}\Columba
DefaultGroupName=Columba
LicenseFile=LICENSE
AlwaysShowComponentsList=false
InfoAfterFile=CHANGES
#ifdef BUNDLE_JRE
OutputBaseFilename=ColumbaSetupwithJRE
#else
OutputBaseFilename=ColumbaSetup
#endif
Compression=bzip
; we are in dest/win32/
SourceDir=..\..\
OutputDir=release\

[Tasks]
Name: desktopicon; Description: Create a &desktop icon; GroupDescription: Additional icons:; MinVersion: 4,4

[Files]
Source: AUTHORS; DestDir: {app}
Source: CHANGES; DestDir: {app}
Source: native\win32\columba.exe; DestDir: {app}
Source: native\win32\columbaw.exe; DestDir: {app}
Source: native\win32\columba.lap; DestDir: {app}; AfterInstall: updateLAPfile
Source: {app}\columba.lap; DestDir: {app}; DestName: columbaw.lap; Flags: external
Source: columba.jar; DestDir: {app}
Source: LICENSE; DestDir: {app}
Source: README; DestDir: {app}
Source: run.bat; DestDir: {app}
Source: {#RISTRETTO}; DestDir: {app}
Source: {#JHALL}; DestDir: {app}
Source: {#USERMANUAL}; DestDir: {app}
Source: {#FORMS}; DestDir: {app}
Source: {#MACCHIATO}; DestDir: {app}
Source: {#FRAPUCCINO}; DestDir: {app}
Source: {#JSCF}; DestDir: {app}
Source: {#JARGS}; DestDir: {app}
Source: {#LUCENE}; DestDir: {app}
Source: {#JWIZZ}; DestDir: {app}
Source: {#PLASTIC}; DestDir: {app}
Source: {#JE}; DestDir: {app}
Source: {#JDOM}; DestDir: {app}
Source: {#JPIM}; DestDir: {app}
Source: native\win32\{#JNIWRAP}; DestDir: {app}\native\win32\
Source: native\win32\{#WINPACK}; DestDir: {app}\native\win32\
Source: native\win32\{#JDIC}; DestDir: {app}\native\win32\
Source: native\win32\lib\jniwrap.dll; DestDir: {app}\native\win32\lib\
Source: native\win32\lib\tray.dll; DestDir: {app}\native\win32\lib\
Source: native\win32\lib\jdic.dll; DestDir: {app}\native\win32\lib\
#ifdef BUNDLE_JRE
Source: {#JRE_SRC_PATH}\{#JRE_FILE}; DestDir: {tmp}; Flags: deleteafterinstall dontcopy
#endif

[UninstallDelete]
Name: columbaw.lap; Type: files

[Icons]
Name: {group}\Columba; Filename: {app}\columbaw.exe; IconIndex: 0; WorkingDir: {app}
Name: {userdesktop}\Columba; Filename: {app}\columbaw.exe; MinVersion: 4,4; Tasks: desktopicon; WorkingDir: {app}; IconIndex: 0
Name: {group}\AUTHORS; Filename: notepad.exe; Parameters: AUTHORS; WorkingDir: {app}; IconIndex: 0
Name: {group}\CHANGES; Filename: notepad.exe; Parameters: CHANGES; WorkingDir: {app}; IconIndex: 0
Name: {group}\LICENSE; Filename: notepad.exe; Parameters: LICENSE; WorkingDir: {app}; IconIndex: 0
Name: {group}\README; Filename: notepad.exe; Parameters: README; WorkingDir: {app}; IconIndex: 0

[Run]
Filename: {app}\columbaw.exe; Description: Launch Columba; Flags: nowait postinstall skipifsilent; WorkingDir: {app}

[_ISTool]
EnableISX=true
UseAbsolutePaths=true

[Dirs]

[Registry]
Root: HKLM; SubKey: SOFTWARE\Clients\Mail\Columba; ValueType: string; ValueName: ; ValueData: Columba
Root: HKLM; SubKey: SOFTWARE\Clients\Mail\Columba\Protocols\mailto; ValueType: string; ValueName: URL Protocol; ValueData: 
Root: HKLM; SubKey: SOFTWARE\Clients\Mail\Columba\Protocols\mailto; ValueType: string; ValueData: {app}\columba --mailurl %1
Root: HKLM; SubKey: SOFTWARE\Clients\Mail\Columba\Protocols\mailto; ValueType: binary; ValueName: EditFlags; ValueData: 20 00 00 00
Root: HKLM; SubKey: SOFTWARE\Clients\Mail\Columba\Protocols\mailto\DefaultIcon; ValueType: string; ValueData: {app}\columba.exe,3
Root: HKLM; SubKey: SOFTWARE\Clients\Mail\Columba\Protocols\mailto\shell\open\command; ValueType: string; ValueData: {app}\columba.exe --mailurl %1
Root: HKLM; SubKey: SOFTWARE\Clients\Mail\Columba\shell\open\command; ValueType: string; ValueData: {app}\columba.exe


[Code]
var
  Page: TWizardPage;
  jreVersion: TLabel;
  InstallJREPage: TInputQueryWizardPage;

//* Getting Java version from registry *//
function getJavaVersion(): String;
var
     javaVersion: String;
begin
     javaVersion := 'None';
     RegQueryStringValue(HKLM, 'SOFTWARE\JavaSoft\Java Runtime Environment', 'CurrentVersion', javaVersion);
     GetVersionNumbersString(javaVersion, javaVersion);
     Result := javaVersion;
end;

//* Check if a JRE was installed *//
function checkJRE(): boolean;
begin
	Result := True;

	//First we need to check if a JRE is installed
     if compareStr( getJavaVersion(), 'None') = 0 then begin
          //* No Java detected *//
          Result := False;
          end
     else begin
          //* Java version lower than 1.4 detected *//
          if (getJavaVersion()) < '1.4' then begin
          Result := False;
          end
     end;
     
     Result := False;
end;

procedure updateLAPfile();
var
lapString : String;

begin
	LoadStringFromFile(ExpandConstant('{app}\columba.lap'), lapString);

	StringChange(lapString, '{app}', ExpandConstant('{app}'));

	SaveStringToFile(ExpandConstant('{app}\columba.lap'), lapString, false);
end;


#ifdef BUNDLE_JRE
procedure InstallJRE(Sender: TObject);
var
AppPath, Parameters, WorkingDirectory: String;ResultCode: Integer;
LogFile : String;

begin
	ExtractTemporaryFile('{#JRE_FILE}')
	LogFile := ExpandConstant('{tmp}\setup.log');
	AppPath := ExpandConstant( '{tmp}\{#JRE_FILE}');
	WorkingDirectory := ExpandConstant( '{tmp}');
	//Parameters := '/v"/qn ADDLOCAL=ALL REBOOT=Suppress"';
	Exec( AppPath, Parameters, WorkingDirectory, SW_SHOW, ewWaitUntilTerminated,  ResultCode);

	BringToFrontAndRestore();
	jreVersion.Caption := getJavaVersion();
end;

procedure CreateJREInstallPage;
var
  InstallButton, OKButton, CancelButton: TButton;
  Label1, Label2, Label3: TLabel;
  Next: Boolean;

begin
	Page := CreateCustomPage(wpWelcome, 'Java Not Found', 'A JRE Version 1.4 or above must be installed!');

	{ Label1 }
	Label1 := TLabel.Create(Page);
	with Label1 do
	begin
	  Parent := Page.Surface;
	  Left := 24;
	  Top := 16;
	  Width := 107;
	  Height := 13;
	  Caption := 'Installed JRE Version :';
	end;

	{ jreVersion }
	jreVersion := TLabel.Create(Page);
	with jreVersion do
	begin
	  Parent := Page.Surface;
	  Left := 150;
	  Top := 16;
	  Width := 30;
	  Height := 21;
	  Caption := getJavaVersion();
	end;

	{ Label3 }
	Label3 := TLabel.Create(Page);
	with Label3 do
	begin
	  Parent := Page.Surface;
	  Left := 24;
	  Top := 58;
	  Width := 120;
	  Height := 13;
	  Caption := 'To continure Installation:';
	end;

	{ installButton }
	installButton := TButton.Create(Page);
	with installButton do
	begin
	  Parent := Page.Surface;
	  Caption := 'Install JRE';
	  Left := 150;
	  Top := 50;
	  Width := 75;
	  Height := 23;
	  TabOrder := 0;
	  OnClick := @InstallJRE;
	end;

	{ Label2 }
	Label2 := TLabel.Create(Page);
	with Label2 do
	begin
	  Parent := Page.Surface;
	  Left := 24;
	  Top := 90;
	  Width := 200;
	  Height := 13;
	  Caption := 'Note: Do *not* restart after installation!';
	end;

end;

function NextButtonClick(CurPageID: Integer): Boolean; 
begin
	Result := True;
	if CurPageId = page.ID then
	begin
		if not checkJRE() then
		begin
			MsgBox('You need to install a JRE first!', mbError, MB_OK);
			Result := False;
		end;
	end;
end;

#endif

procedure InitializeWizard();
var 
	ErrorCode: Integer;
begin
	if not checkJRE() then	
	#ifdef BUNDLE_JRE
	begin
		createJREInstallPage();
	end;		
	#else
	begin
		MsgBox('You need to install a JRE first!', mbError, MB_OK);
		ShellExec('open','http://www.java.com/en/download/windows_automatic.jsp','','',SW_SHOW,ewNoWait,ErrorCode);
		Abort();
	end;
	#endif
end;
