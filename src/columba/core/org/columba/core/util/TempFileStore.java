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

import org.columba.core.config.ConfigPath;
import org.columba.core.io.DiskIO;

public class TempFileStore
{
    private static File tempDir;
    
    static {
	File configDir = ConfigPath.getConfigDirectory();

	tempDir = new File( configDir, "tmp" );
	DiskIO.emptyDirectory( tempDir );
	DiskIO.ensureDirectory( tempDir );
    }

    private TempFileStore() {}

    protected static String replaceWhiteSpaces( String s )
    {
	return s.replace( ' ', '_' );
    }

    public static File createTempFile()
    {
	return createTempFileWithSuffix( "tmp" );
    }


    public static File createTempFile( String s )
    {
	return new File( tempDir, replaceWhiteSpaces(s) );
    }


    public static File createTempFileWithSuffix( String suffix )
    {
	return new File( tempDir, "columba"+System.currentTimeMillis()+"."+suffix );
    }
}
