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

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 * @version 	1.0
 * @author
 */
public class cFileChooser extends JFileChooser {
	
	private FileFilter selectFilter;
	
	public cFileChooser() {
		super();
	}

	public cFileChooser(File currentDir) {
		super(currentDir);
	}
	
	public void setSelectFilter(FileFilter selectFilter){
		this.selectFilter = selectFilter;
	}
	
	public void setSelectedFile(File f) {
		if( selectFilter != null ) {
			if( selectFilter.accept( f ) ) {
				super.setSelectedFile( f );	
			} 	
		}				
	}

	public File getSelectedFile() {
		File currentDir = super.getCurrentDirectory();
		File selectedFile = super.getSelectedFile();
		if( selectedFile == null ) return null;
		
		return new File( currentDir, selectedFile.getName() );
	}
	
	public void forceSelectedFile( File f ) {
		super.setSelectedFile( f );
	}

}
