/*
[Columba] win32 launcher - core code
Copyright (C) 2001-2  Luca Santarelli

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either dVersion 2
of the License, or (at your option) any later dVersion.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/

#define STRICT
#define WIN32_LEAN_AND_MEAN

//Kicks in optimizations.
#define STRONGLY_OPTIMIZE
//Uses printf() instead of MessageBox() in setDefaultClient() and checkDefaultClient()
//#define USE_STDIO

#ifdef STRONGLY_OPTIMIZE
	// /Og (global optimizations), /Os (favor small code), /Oy (no frame pointers)
	#pragma optimize("gsy",on)

	#pragma comment(linker,"/RELEASE")

	#pragma comment(linker,"/merge:.rdata=.data")
	#pragma comment(linker,"/merge:.text=.data")
	#pragma comment(linker,"/merge:.reloc=.data")

	#if _MSC_VER >= 1000
		// Only supported/needed with VC6; VC5 already does 0x200 for release builds.
		// Totally undocumented! And if you set it lower than 512 bytes, the program crashes.
		// Either leave at 0x200 or 0x1000
		#pragma comment(linker,"/FILEALIGN:0x200")
	#endif // _MSC_VER >= 1000
#endif // STRONGLY_OPTIMIZE

//Main #includes
#include <windows.h>
//This one is needed for ShellExecute()
#include <shellapi.h>
//This one is needed for strtod(), I haven0t found a win32 API replacement.
#include <stdlib.h>

#ifdef USE_STDIO
	#include <stdio.h>
#endif //USE_STDIO

//My #includes
#include "resource.h"
#include "launcher.h"

//Function signature.
BOOL checkDefaultClient();
BOOL setDefaultClient();
BOOL CALLBACK DialogProc (HWND, UINT, WPARAM, LPARAM);

//Global variables.
BOOL isDefaultClient = FALSE;
BOOL isDialogOpen = FALSE;
BOOL bLoadColumba = TRUE;

//Function definition.
//========== DialogProc ==========
//This is the dialog procedure for the input dialog.
BOOL CALLBACK DialogProc (HWND hwnd, UINT message, WPARAM wParam, LPARAM lParam) {
	switch (message) {
		case WM_INITDIALOG:
			return TRUE;
		case WM_COMMAND: //Clicks.
			switch (LOWORD(wParam)) {
				case ID_OK: //The user wants to proceed.
					bLoadColumba = TRUE;
					isDefaultClient = IsDlgButtonChecked(hwnd, IDC_CHECK); //We'll set it in WinMain
					isDialogOpen = FALSE;
					EndDialog(hwnd, 0);
					return (TRUE);
				default:
					return FALSE;
			}
		case WM_DESTROY:
			PostQuitMessage(0); //This kills the message pump, thus killing this thread.
			return FALSE;
		case WM_CLOSE:
			bLoadColumba = FALSE;
			DestroyWindow(hwnd);
			return FALSE;
	}
	return FALSE;
}

//========== WinMain ==========
//This is the entry point of the executable. The main code will be executed here.
int APIENTRY WinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, LPSTR lpCmdLine, int nCmdShow) {
	DWORD dwValue, dwCounter, dwType;
	//char* pszValue = (char*) GlobalAlloc(GMEM_FIXED | GMEM_ZEROINIT, BUFFER_SIZE * sizeof(char));
	char pszValue[BUFFER_SIZE];

	//Registry API variables.
	int iResult = 0;
	HKEY hkJavaVersions, hkJavaJRE, hkColumbaFingerprint;
	BOOL isJREOk = FALSE;
	double dVersion = 0;
	//char* pszJREHome = (char*)GlobalAlloc(GMEM_FIXED | GMEM_ZEROINIT, PATH_SIZE * sizeof(char));
	//char* pszColumbaHome = (char*)GlobalAlloc(GMEM_FIXED | GMEM_ZEROINIT, PATH_SIZE * sizeof(char));
	//char* pszColumbaParameters = (char*)GlobalAlloc(GMEM_FIXED | GMEM_ZEROINIT, PATH_SIZE * sizeof(char));
	char pszJREHome[PATH_SIZE];
	char pszColumbaHome[PATH_SIZE];
	char pszColumbaParameters[PATH_SIZE];
	//Columba variables.
	HINSTANCE hInstColumba = NULL;

	//Search for an installed JRE.
	iResult = RegOpenKeyEx(HKEY_LOCAL_MACHINE, REGKEY_JAVA_VERSIONS, 0, KEY_ENUMERATE_SUB_KEYS, &hkJavaVersions);

	if (iResult != ERROR_SUCCESS ) { //RegOpenKeyEx didn't work. It's likely that the JRE is not installed.
		MessageBox(NULL,
		"The JRE (Java Runtime Environment) is absent ot corrupted. Please, reinstall Java.",
		"Columba - Please reinstall JRE", MB_OK);
		return 2;
	}

	//Check JRE Version
	dwCounter = 0; //counter for the enumerating keys.
	while (!isJREOk) {
		dwValue = BUFFER_SIZE;
		iResult = RegEnumKeyEx(hkJavaVersions, dwCounter, pszValue, &dwValue, NULL, NULL, NULL, NULL);
		if (iResult != ERROR_SUCCESS ) { //There was an error reading from the registry.
			MessageBox(NULL,
			"The JRE (Java Runtime Environment) or your Windows registry seems to be corrupted. Try reinstalling an up to date JRE first.",
			"Columba - Registry corrupted", MB_OK);
			return 2;
		}
		//We get here if the enumerated key was good. We translate the version number to a double and check it's version.
		dVersion = strtod(pszValue, 0);
		if (dVersion >= JRE_VERSION )
			isJREOk = TRUE;
		else
			isJREOk = FALSE;
		dwCounter++;
	}

	//Get JRE Path
	iResult = RegOpenKeyEx(hkJavaVersions, pszValue, 0, KEY_QUERY_VALUE, &hkJavaJRE);

	if (iResult != ERROR_SUCCESS ) {
		MessageBox(NULL,
		"The JRE (Java Runtime Environment) or your Windows registry seems to be corrupted. Try reinstalling an up to date JRE first.",
		"Columba - Registry corrupted", MB_OK);
		return 2;
	}

	//We get here if the path reg.key can be opened.
	dwValue = PATH_SIZE;
	iResult = RegQueryValueEx(hkJavaJRE, REGVALUE_JAVA_HOME, NULL, &dwType, (unsigned char*)pszJREHome, &dwValue);

	if (iResult != ERROR_SUCCESS ) {
		MessageBox(NULL,
		"The JRE (Java Runtime Environment) or your Windows registry seems to be corrupted. Try reinstalling an up to date JRE first.",
		"Columba - Registry corrupted", MB_OK);
		return 2;
	}
	//We have the right path in pszJREHome, let's add the bin directory to it.
	lstrcat(pszJREHome,JAVA_BIN);

	RegCloseKey(hkJavaJRE);
	RegCloseKey(hkJavaVersions);

	//Check if columba is the default EMailClient
	isDefaultClient = checkDefaultClient();

	if (!isDefaultClient) { //Show the dialog asking theuser if he wants to set up columba as default email client.
		HWND hDialog;
		MSG  msg;
		int iGetMsgValue;

		hDialog = CreateDialog( hInstance, MAKEINTRESOURCE(IDD_DIALOG1), 0, DialogProc );
		ShowWindow(hDialog, SW_SHOW);

		isDialogOpen = TRUE;
		//Message pump
		while (isDialogOpen && (iGetMsgValue = GetMessage(&msg, NULL, 0, 0)) == TRUE) { //a safe Message Pump
			if (iGetMsgValue == -1) //Error in the GetMessage
				return (-1);
			if (!IsDialogMessage(hDialog, &msg)) {
				TranslateMessage(&msg);
				DispatchMessage(&msg);
			}
		}
		//We get here when the dialog has been closed.
		if (isDefaultClient) {
			if (setDefaultClient() == FALSE) { //The email client could not be set.
				//The warning message should be inside setDefaultClient().
				return 0;
			}
		}
	} //End of dialog code.
	if (!bLoadColumba) { //The user chose not to load Columba.
		return 1;
	}

	//We can now run Columba. We need to know its path and parameters.
	iResult = RegOpenKeyEx(HKEY_LOCAL_MACHINE, REGKEY_COLUMBA_FINGERPRINT, 0, KEY_QUERY_VALUE, &hkColumbaFingerprint);

	if (iResult != ERROR_SUCCESS ) {
		MessageBox(NULL,
		"Columba or your Windows registry entries seem to be corrupted. Please, try reinstalling Columba first.",
		"Columba - Registry corrupted", MB_OK);
		return 2;
	}

	dwValue = PATH_SIZE;
	iResult = RegQueryValueEx(hkColumbaFingerprint, REGVALUE_COLUMBA_HOME, NULL, &dwType, (unsigned char*)pszColumbaHome, &dwValue);

	if (iResult != ERROR_SUCCESS ) {
		MessageBox(NULL,
		"Columba or your Windows registry entries seem to be corrupted. Please, try reinstalling Columba first.",
		"Columba - Registry corrupted", MB_OK);
		return 2;
	}

	RegCloseKey(hkColumbaFingerprint);

	//We have what we need, we can start Columba.
	lstrcpy(pszColumbaParameters, COLUMBA_JAVA_PARAMETER); //Java parameters; i.e.: "-jar Columba.jar"
	//Now the (optional) command line parameters.
	lstrcat(pszColumbaParameters, " ");
	lstrcat(pszColumbaParameters, lpCmdLine);

	hInstColumba = ShellExecute(NULL, "open", pszJREHome, pszColumbaParameters, pszColumbaHome, SW_HIDE);
	if (hInstColumba <= (HINSTANCE)32) { //ShellExecute() wasn't able to execute.
		MessageBox(NULL,
		"Columba could not be started. Try reinstalling columba, please.",
		"Columba - launch error", MB_OK);
		return 2;
	}
	//Finally, return.
	return 0;
}

//========== checkDefaultClient ==========
//This function returns TRUE if Columba is the current default email client, or FALSE otherwise.
BOOL checkDefaultClient() {
	DWORD dwValue, dwType;
	char* pszValue = (char*) GlobalAlloc(GMEM_FIXED | GMEM_ZEROINIT, BUFFER_SIZE * sizeof(char));
	HKEY hkDefaultEMailer;
	int iResult;

	iResult = RegOpenKeyEx(HKEY_LOCAL_MACHINE, REGKEY_DEFAULT_MAIL, 0, KEY_QUERY_VALUE, &hkDefaultEMailer);

	if (iResult != ERROR_SUCCESS ) { //We could not open the registry. It likely means there's a corrupted Windows registry.
		//We don't need to prompt the user, we simply have found out that Columba is NOT the default email client, so we can return a FALSE.
		return FALSE;
/*
#ifdef USE_STDIO
		printf("Error : Registry corrupted! No DefaultMailClient entry\n" );
#else
		MessageBox(NULL, "Error : Registry corrupted! No DefaultMailClient entry", "Registry iResult", MB_OK);
#endif //USE_STDIO
		exit(0);
*/
	}
	//The registry key could get opened. Let's read the default email client name.
	dwValue = BUFFER_SIZE;
	iResult = RegQueryValueEx(hkDefaultEMailer, NULL, NULL, &dwType, (unsigned char*) pszValue, &dwValue);
	RegCloseKey(hkDefaultEMailer);

	return (lstrcmp(pszValue, COLUMBA) == 0);
}

//========== checkDefaultClient ==========
//This function sets Columba as default email client.
BOOL setDefaultClient() {
	HKEY hkDefaultEMailer;
	int iResult;

	iResult = RegOpenKeyEx(HKEY_LOCAL_MACHINE, REGKEY_DEFAULT_MAIL, 0, KEY_SET_VALUE, &hkDefaultEMailer );

	if (iResult != ERROR_SUCCESS ) { //We could not open the registry. It likely means there's a corrupted Windows registry or that we didn't have the right grants  to write.
#ifdef USE_STDIO
		printf("Error : Registry corrupted! No DefaultMailClient entry\n" );
#else
		MessageBox(NULL,
		"There seems to be a corruption in your Windows registry. I recommend you to backup your data and reinstall your Windows® system or run some registry-fixing utilities as soon as possible.",
		"Columba - Windows registry corrupted", MB_OK);
#endif //USE_STDIO
		//Since this is a critical error, we don't launch Columba.
		return FALSE;
	}

	iResult = RegSetValueEx(hkDefaultEMailer, NULL, (DWORD)NULL, REG_SZ, (unsigned char*) COLUMBA, sizeof(COLUMBA));
	if (iResult != ERROR_SUCCESS) { //There was an error.
		RegCloseKey(hkDefaultEMailer);
		return FALSE;
	}
	else { //Everything went fine.
		RegCloseKey(hkDefaultEMailer);
		return TRUE;
	}
}
