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

import org.columba.core.config.ConfigPath;
import org.columba.core.io.DiskIO;

public class TempFileStore
{
    private static File tempDir;

    public TempFileStore(  )
    {
	File configDir = ConfigPath.getConfigDirectory();

	tempDir = new File( configDir, "tmp" );
	DiskIO.emptyDirectory( tempDir );
	DiskIO.ensureDirectory( tempDir );
    }

    // this is called on startup, to get rid of old tempfiles
    protected static void cleanupTempFolder()
    {
	DiskIO.deleteDirectory( tempDir );
    }

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
