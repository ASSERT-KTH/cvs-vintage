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
import java.util.Vector;

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
