// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

package org.columba.core.util;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.*;

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
