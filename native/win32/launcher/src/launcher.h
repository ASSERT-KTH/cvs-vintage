/*
[Columba] win32 launcher - definitions
Copyright (C) 2001-2  Luca Santarelli

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/

#define BUFFER_SIZE							255
#define PATH_SIZE							1024
#define COLUMBA								"Columba"
#define JAVA_BIN							"\\bin\\java"
#define REGKEY_JAVA_VERSIONS				"SOFTWARE\\JavaSoft\\Java Runtime Environment"
#define REGKEY_DEFAULT_MAIL					"SOFTWARE\\Clients\\Mail"
#define REGKEY_COLUMBA_FINGERPRINT			"SOFTWARE\\Clients\\Mail\\Columba\\FingerPrint"
#define REGVALUE_JAVA_HOME					"JavaHome"
#define REGVALUE_COLUMBA_HOME				"ColumbaHome"
#define REGVALUE_MAKE_DEFAULT				"MakeDefault"

#define _RUN_PACKED_
//Packed = jar file.

#define JRE_VERSION						1.3

#ifdef _RUN_PACKED_
	#define COLUMBA_JAVA_PARAMETER		"-jar Columba.jar"
#else
	#define COLUMBA_JAVA_PARAMETER		"org.columba.main.Main"
#endif //_RUN_PACKED_

