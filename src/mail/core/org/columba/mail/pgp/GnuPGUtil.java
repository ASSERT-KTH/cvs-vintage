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

package org.columba.mail.pgp;

import java.io.*;
import java.util.*;
import javax.swing.*;

import org.columba.core.io.*;
import org.columba.mail.config.*;
import org.columba.mail.gui.util.*;


public class GnuPGUtil extends DefaultUtil
{
    static String[] cmd = { "--batch --no-tty --passphrase-fd 0 -d",
                            "--batch --no-tty --armor --encrypt",
                            "--no-secmem-warning --no-greeting --batch --yes --no-tty --armor --passphrase-fd 0 -u %user% --output - --detach-sign",
                            "--batch --no-tty --verify" };


	protected String getRawCommandString( int type ) {
		return cmd[type];
	}	

	

	/*
    public int decrypt( String path, String pgpMessage, String passphrase ) throws Exception
    {
        File tempFile = File.createTempFile("columba"+System.currentTimeMillis(),null);
        tempFile.deleteOnExit();
        DiskIO.saveStringInFile( tempFile, pgpMessage );

        setCommandString( path + " " + cmd[PGPController.DECRYPT_ACTION] + " " + tempFile.toString() );

        int exitVal = super.decrypt( path, pgpMessage, passphrase );

        if ( exitVal == 0 )
        {
            // everything worked ok
            return 0;
        }
        else if ( exitVal == 1 )
        {
            // this means: decrypted message successfully, but
            // was not able to verify the signature
            return 1;
        }
        else if ( exitVal == 2 )
        {
            // unknown error
            return 2;
        }
        else
            return 2;

    }


    public int verify( String path, String pgpMessage, String signatureString, String passphrase ) throws Exception
    {
        int exitVal = -1;

        File tempFile1 = File.createTempFile("columba"+System.currentTimeMillis(),null);
        tempFile1.deleteOnExit();
        DiskIO.saveStringInFile( tempFile1, pgpMessage );

        File tempFile2 = File.createTempFile("columba"+System.currentTimeMillis(),null);
        tempFile2.deleteOnExit();
        DiskIO.saveStringInFile( tempFile2, signatureString );

        setCommandString( path + " " + cmd[PGPController.VERIFY_ACTION] + " " + tempFile2.toString() + " " + tempFile1.toString() );

        exitVal = super.verify( path, pgpMessage, signatureString, passphrase );

        return exitVal;
    }

    public int sign( String path, String pgpMessage, String passphrase, String id ) throws Exception
    {
        int exitVal = -1;

        File tempFile = File.createTempFile("columba"+System.currentTimeMillis(),null);
        tempFile.deleteOnExit();
        DiskIO.saveStringInFile( tempFile, pgpMessage );

        outputFile = File.createTempFile( "columba"+System.currentTimeMillis(), null );
        outputFile.deleteOnExit();
        outputFile.delete();


        String str = path + " --detach-sign " + tempFile +"--armor --output " + outputFile;

        setCommandString( str );

        exitVal = super.sign( path, pgpMessage, passphrase, signValue, recipient, id );

        return exitVal;
    }

    public int encrypt( String path, String pgpMessage, String passphrase, boolean signValue,  Vector recipient, String id ) throws Exception
    {
        int exitVal = -1;

        File tempFile = File.createTempFile("columba"+System.currentTimeMillis(),null);
        tempFile.deleteOnExit();
        DiskIO.saveStringInFile( tempFile, pgpMessage );

        outputFile = File.createTempFile( "columba"+System.currentTimeMillis(), null );
        outputFile.deleteOnExit();
        outputFile.delete();

        StringBuffer rcpt = new StringBuffer();
        for ( int i=0; i<recipient.size(); i++ )
        {
            rcpt.append( (String) recipient.get(i) );
            if ( i != recipient.size()-1 ) rcpt.append(" ");
        }

        String str = path + " --recipient " + rcpt.toString() +" --output " + outputFile
                     + " " + cmd[PGPController.ENCRYPT_ACTION] + " " + tempFile.toString();

        if ( signValue == true )
        {
            str = path + " --recipient " + rcpt.toString() +" --output " +
                  outputFile + " " + cmd[PGPController.SIGN_ACTION] + " " + id
                  + " "  + cmd[PGPController.ENCRYPT_ACTION] + " " + tempFile.toString();
        }

        setCommandString( str );

        exitVal = super.encrypt( path, pgpMessage, passphrase, signValue, recipient, id );

        return exitVal;
    }
	*/
    // every line of the error stream
    // starts with "gpg"
    // remove these characters
    protected String parse( String s )
    {
        StringBuffer str = new StringBuffer( s );

        int pos = 0;
        if ( pos+3 < str.length() )
        {
            if ( ( str.charAt(pos) == 'g' ) &&
                 ( str.charAt(pos+1) == 'p' ) &&
                 ( str.charAt(pos+2) == 'g' ) &&
                 ( str.charAt(pos+3) == ':' ) )
            {
                str.delete(pos, pos+4);
            }
        }

        pos++;

        while ( pos < str.length() )
        {
            if ( str.charAt(pos) ==  '\n' )
            {
                pos++;
                if ( pos+3 < str.length() )
                {
                    if ( ( str.charAt(pos) == 'g' ) &&
                         ( str.charAt(pos+1) == 'p' ) &&
                         ( str.charAt(pos+2) == 'g' ) &&
                         ( str.charAt(pos+3) == ':' ) )
                    {
                        str.delete(pos, pos+4);
                    }
                }
            }

            pos++;
        }


        return str.toString();
    }
}
