#define STRICT
#define WIN32_LEAN_AND_MEAN
#define _MERGE_RDATA_
#include <jni.h>
#include <windows.h>
#include "AggressiveOptimize.h"

#include <stdio.h>
JNIEXPORT void JNICALL Java_org_columba_core_util_WindowMaximizer_maximizeWindow
	(JNIEnv * env, jobject obj, jstring jsClassName) {
	HWND hWnd = NULL;

	//The first thing we need to do is translating the jstring into a 0 terminated C string.
	const char * jscszClassName = (*env)->GetStringUTFChars(env, jsClassName, 0);
	hWnd = FindWindow((char*)jscszClassName, NULL);
	(*env)->ReleaseStringUTFChars(env, jsClassName, jscszClassName);

	if (hWnd) {//We found it.
		ShowWindow(hWnd, SW_MAXIMIZE);
	}
#ifdef _DEBUG
	else {
		DWORD dwError = GetLastError();
		char szError[1024];
		wsprintf(szError, "Errore in FindWindow. GetLastError() returned: %8x (%d).\n", dwError, dwError);
		printf(szError);
	}
#endif
}

JNIEXPORT jboolean JNICALL Java_org_columba_core_util_WindowMaximizer_isWindowMaximized
	(JNIEnv * env, jobject obj, jstring jsClassName) {
	HWND hWnd = NULL;

	//The first thing we need to do is translating the jstring into a 0 terminated C string.
	const char * jscszClassName = (*env)->GetStringUTFChars(env, jsClassName, 0);
	hWnd = FindWindow((char*)jscszClassName, NULL);
	(*env)->ReleaseStringUTFChars(env, jsClassName, jscszClassName);

	if (hWnd) {//We found it.
		WINDOWPLACEMENT wp = { 0 };
		BOOL bResult = FALSE;

		wp.length = sizeof(WINDOWPLACEMENT);
		bResult = GetWindowPlacement(hWnd, &wp);
		if (bResult)
			return (jboolean)(wp.showCmd == SW_MAXIMIZE || wp.showCmd == SW_SHOWMAXIMIZED);
		else {
			return (jboolean)FALSE;
		}
	}
#ifdef _DEBUG
	else {
		DWORD dwError = GetLastError();
		char szError[1024];
		wsprintf(szError, "Errore in FindWindow. GetLastError() returned: %8x (%d).\n", dwError, dwError);
		printf(szError);
		return (jboolean)FALSE;
	}
#endif
	return (jboolean)FALSE;
}
