;Columba Setup file
;InnoSetup Version > 4.2.7

#define VERSION="1.0 RC1"

;Uncomment this line to bundle a JRE
;#define BUNDLE_JRE
#define DOWNLOADED_JRE_PATH="C:\Documents and Settings\user\My Documents\My Downloads\"
#define JRE_FILE="j2re-1_4_2_05-windows-i586-p.exe"

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
OutputBaseFilename=ColumbaSetup
Compression=bzip
; we are in dest/win32/
SourceDir=..\..\
OutputDir=.

[Tasks]
Name: desktopicon; Description: Create a &desktop icon; GroupDescription: Additional icons:; MinVersion: 4,4

[Files]
Source: lib\jargs.jar; DestDir: {app}\lib\
Source: lib\lucene-1.3-final.jar; DestDir: {app}\lib\
Source: lib\jwizz-0.1.2.jar; DestDir: {app}\lib\
Source: lib\plastic-1.2.0.jar; DestDir: {app}\lib\
Source: AUTHORS; DestDir: {app}
Source: CHANGES; DestDir: {app}
Source: native\win32\launcher\columba.exe; DestDir: {app}
Source: native\win32\launcher\columba.lap; DestDir: {app}
Source: columba.jar; DestDir: {app}
Source: LICENSE; DestDir: {app}
Source: README; DestDir: {app}
Source: run.bat; DestDir: {app}
Source: lib\ristretto-1.0_RC2.jar; DestDir: {app}\lib\
Source: lib\jhall.jar; DestDir: {app}\lib\
Source: lib\usermanual.jar; DestDir: {app}\lib\
Source: lib\forms-1.0.3.jar; DestDir: {app}\lib\
#ifdef BUNDLE_JRE
Source: {#DOWNLOADED_JRE_PATH}{#JRE_FILE}; DestDir: {tmp}; Flags: deleteafterinstall dontcopy
#endif
Source: lib\macchiato-1.0pre1.jar; DestDir: {app}\lib\
Source: lib\winpack.jar; DestDir: {app}\lib\
Source: lib\jniwrap-2.4.jar; DestDir: {app}\lib\
Source: lib\frappucino-1.0pre1.jar; DestDir: {app}\lib\
Source: lib\jscf-0.1.jar; DestDir: {app}\lib\

[Icons]
Name: {group}\Columba; Filename: {app}\columba.exe; IconIndex: 0; WorkingDir: {app}
Name: {userdesktop}\Columba; Filename: {app}\columba.exe; MinVersion: 4,4; Tasks: desktopicon; WorkingDir: {app}; IconIndex: 0
Name: {group}\AUTHORS; Filename: notepad.exe; Parameters: AUTHORS; WorkingDir: {app}; IconIndex: 0
Name: {group}\CHANGES; Filename: notepad.exe; Parameters: CHANGES; WorkingDir: {app}; IconIndex: 0
Name: {group}\LICENSE; Filename: notepad.exe; Parameters: LICENSE; WorkingDir: {app}; IconIndex: 0
Name: {group}\README; Filename: notepad.exe; Parameters: README; WorkingDir: {app}; IconIndex: 0

[Run]
Filename: {app}\columba.exe; Description: Launch Columba; Flags: nowait postinstall skipifsilent; WorkingDir: {app}

[_ISTool]
EnableISX=true
UseAbsolutePaths=true

[Dirs]

[Registry]
Root: HKLM; SubKey: SOFTWARE\Clients\Mail\Columba; ValueType: string; ValueName: ; ValueData: Columba
Root: HKLM; SubKey: SOFTWARE\Clients\Mail\Columba\FingerPrint; ValueType: string; ValueName: ColumbaHome; ValueData: {app}
Root: HKLM; SubKey: SOFTWARE\Clients\Mail\Columba\FingerPrint; ValueType: string; ValueName: MakeDefault; ValueData: YES
Root: HKLM; SubKey: SOFTWARE\Clients\Mail\Columba\Protocols\mailto; ValueType: string; ValueName: URL Protocol; ValueData: 
Root: HKLM; SubKey: SOFTWARE\Clients\Mail\Columba\Protocols\mailto; ValueType: string; ValueName: ; ValueData: URL:MailTo-Protokoll
Root: HKLM; SubKey: SOFTWARE\Clients\Mail\Columba\Protocols\mailto\shell\open\command; ValueType: string; ValueData: {app}\columba --mailurl %1


[Code]
var
  Label1: TLabel;
  jreVersion: TLabel;
  installButton: TButton;
  Label2: TLabel;
  Label3: TLabel;

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
end;


procedure InstallJRE(Sender: TObject);
var
AppPath, Parameters, WorkingDirectory: String;WaitTerminated,
WaitIdle: Boolean;ShowCmd, ResultCode: Integer;
LogFile : String;

begin
	if ExtractTemporaryFile('{#JRE_FILE}') then
	begin
		LogFile := ExpandConstant('{tmp}\setup.log');
		AppPath := ExpandConstant( '{tmp}\{#JRE_FILE}');
		WorkingDirectory := ExpandConstant( '{tmp}');
		WaitTerminated := True;
		WaitIdle := True;
		ShowCmd := 0;
		//Parameters := '/v"/qn ADDLOCAL=ALL REBOOT=Suppress"';
		InstExec( AppPath, Parameters, WorkingDirectory,
		WaitTerminated,WaitIdle, ShowCmd, ResultCode);

		BringToFrontAndRestore();
		jreVersion.Caption := getJavaVersion();
	end;

	WizardForm.NextButton.Enabled := CheckJRE();
	jreVersion.Caption := getJavaVersion();
end;

{ ScriptDlgPages }

function ScriptDlgPages(CurPage: Integer; BackClicked: Boolean): Boolean;
var
  Next: Boolean;

begin
  #ifdef BUNDLE_JRE
  { place JRE Install Page between 'Welcome' and 'License' }
  if (not CheckJRE()) and ((not BackClicked and (CurPage = wpWelcome)) or (BackClicked and (CurPage = wpLicense))) then
  begin
    ScriptDlgPageOpen();
    ScriptDlgPageClearCustom();

    ScriptDlgPageSetCaption('Java Not Found');
    ScriptDlgPageSetSubCaption1('A JRE Version 1.4 or above must be installed!');

	{ Label1 }
	Label1 := TLabel.Create(WizardForm.ScriptDlgPanel);
	with Label1 do
	begin
	  Parent := WizardForm.ScriptDlgPanel;
	  Left := 24;
	  Top := 16;
	  Width := 107;
	  Height := 13;
	  Caption := 'Installed JRE Version :';
	end;

	{ jreVersion }
	jreVersion := TLabel.Create(WizardForm.ScriptDlgPanel);
	with jreVersion do
	begin
	  Parent := WizardForm.ScriptDlgPanel;
	  Left := 150;
	  Top := 16;
	  Width := 30;
	  Height := 21;
	  Caption := getJavaVersion();
	end;

	{ Label3 }
	Label3 := TLabel.Create(WizardForm.ScriptDlgPanel);
	with Label3 do
	begin
	  Parent := WizardForm.ScriptDlgPanel;
	  Left := 24;
	  Top := 58;
	  Width := 120;
	  Height := 13;
	  Caption := 'To continure Installation:';
	end;

	{ installButton }
	installButton := TButton.Create(WizardForm.ScriptDlgPanel);
	with installButton do
	begin
	  Parent := WizardForm.ScriptDlgPanel;
	  Caption := 'Install JRE';
	  Left := 150;
	  Top := 50;
	  Width := 75;
	  Height := 23;
	  TabOrder := 0;
	  OnClick := @InstallJRE;
	end;

	{ Label2 }
	Label2 := TLabel.Create(WizardForm.ScriptDlgPanel);
	with Label2 do
	begin
	  Parent := WizardForm.ScriptDlgPanel;
	  Left := 24;
	  Top := 90;
	  Width := 200;
	  Height := 13;
	  Caption := 'Note: Do *not* restart after installation!';
	end;


	WizardForm.NextButton.Enabled := CheckJRE();

    Next := ScriptDlgPageProcessCustom(installButton);

    { check main-page navigation }
    if not BackClicked then
      Result := Next
    else
      Result := not Next;
    ScriptDlgPageClose(not Result);
  end
  { return default }
  else
  #endif
    Result := True;
end;

{ NextButtonClick }

function NextButtonClick(CurPage: Integer): Boolean;
begin
  Result := ScriptDlgPages(CurPage, False);
end;

{ BackButtonClick }

function BackButtonClick(CurPage: Integer): Boolean;
begin
  Result := ScriptDlgPages(CurPage, True);
end;
