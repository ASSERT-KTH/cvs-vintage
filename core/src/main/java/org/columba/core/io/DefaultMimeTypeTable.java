//The contents of this file are subject to the Mozilla Public License Version 1.1
//(the "License"); you may not use this file except in compliance with the 
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License 
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.
package org.columba.core.io;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DefaultMimeTypeTable {

	private static Map mimeTable = loadTable();

	private static Map<String, String> loadTable() {
		Map<String, String> result = new Hashtable<String, String>();

		try {
			String table = DiskIO
					.readStringFromResource("org/columba/core/config/mime_table");
			Pattern listPattern = Pattern.compile("(\\w+) (\\w+/\\w+)");
			Matcher matcher = listPattern.matcher(table);
			while (matcher.find()) {
				result.put(matcher.group(1), matcher.group(2));
			}
		} catch (IOException e) {
			// do nothing here
		}

		return result;
	}

	public static String lookup(File file) {
		int dotPos = file.getName().lastIndexOf('.');
		String _return = lookup(file.getName().substring(dotPos + 1));
		if (dotPos == -1 || dotPos == file.getName().length() - 1) {
			_return = "application/octet-stream";
		}
		return _return;

	}

	public static String lookup(String string) {
		String lookupResult = (String) mimeTable.get(string);

		if (lookupResult == null) {
			lookupResult = "application/octet-stream";
		}

		return lookupResult;
	}

}
