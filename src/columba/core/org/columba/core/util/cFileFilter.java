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
package org.columba.core.util;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class cFileFilter extends FileFilter {

	public static final int FILEPROPERTY_FILE		= 0x0001;
	public static final int FILEPROPERTY_DIRECTORY	= 0x0002;
	public static final int FILEPROPERTY_HIDDEN		= 0x0004;
		
	private int property;
	
	public cFileFilter() {
		property = 0x0000;	//Check for no property
	}
	
	/**
	 * @see FileFilter#accept(File)
	 */
	public boolean accept(File f) {
		
		boolean result = true;
		if( f == null ) return false;
		if( !f.exists() ) return true; // return true for new files
		
		if( (property & FILEPROPERTY_FILE) > 0 ) {
			result = result && f.isFile();
		}
		
		if( (property & FILEPROPERTY_DIRECTORY) > 0 ) {
			result = result && f.isDirectory();
		}

		if( (property & FILEPROPERTY_HIDDEN) > 0 ) {
			result = result && f.isHidden();
		}
		
		return result;
	}
	
	public void acceptFilesWithProperty(int newprop){
		property = newprop;
	}

	/**
	 * @see FileFilter#getDescription()
	 */
	public String getDescription() {
		return new String("Columba File Filter");
	}

}
