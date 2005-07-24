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
import java.net.MalformedURLException;
import java.net.URL;
import com.apple.eio.FileManager;

public class MacDesktop implements DesktopInterface {

	public String getMimeType(File file) {
		return "application/octet-stream";
	}

	public String getMimeType(String ext) {
		return "application/octet-stream";
	}

	public boolean supportsOpen() {
		return true;
	}

	public boolean open(File file) {
		try {
			FileManager.openURL(file.toURL().toString());
		} catch (MalformedURLException e) {
			return false;
		}
		return true;
	}

	public boolean openAndWait(File file) {
		return open(file);
	}

	public boolean supportsBrowse() {
		return true;
	}

	public void browse(URL url) {
		FileManager.openURL(url.toString());
	}

}
