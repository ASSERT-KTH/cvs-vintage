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

package org.columba.mail.gui.mimetype;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.columba.mail.message.MimeHeader;

public class WindowsViewer extends DefaultViewer
{

    public Process openWith( MimeHeader header, File tempFile )
    {
        openDocument( tempFile.getPath() );
        return null;
    }

    public Process open( MimeHeader header, File tempFile )
    {
        openDocument( tempFile.getPath() );
        return null;
    }

    public Process openURL( URL url )
    {
        String osName = System.getProperty("os.name" );
	if( osName.equals( "Windows 2000" ) ) {
		Process proc = null;
		try {
                	String[] cmd = new String[]{"cmd.exe","/C","start",url.toString()};
			Runtime rt = Runtime.getRuntime();
			System.out.println("Executing " + cmd[0] + " " + cmd[1] + " " + cmd[2] + " " +cmd[3]);
			proc = rt.exec(cmd);
			// any error message?
			StreamGobbler errorGobbler = new
				StreamGobbler(proc.getErrorStream(), "ERROR");
			errorGobbler.start();
			// any output?
			StreamGobbler outputGobbler = new
				StreamGobbler(proc.getInputStream(), "OUTPUT");
			outputGobbler.start();

			// any error?
			int exitVal = proc.waitFor();
			System.out.println("ExitValue: " + exitVal);
		} catch (Throwable t) {
			t.printStackTrace();
		}

	}else{
	    openDocument( url.getPath() );
	}
        return null;
    }

    public Process openWithURL( URL url )
    {
        openDocument( url.getPath() );
        return null;
    }

    protected void openDocument( String filename )
    {
        try
        {
            String osName = System.getProperty("os.name" );
	    Process proc = null;
            if ( osName.equals( "Windows NT" )  )
            {
                String[] cmd = new String[]{"cmd.exe","/C",filename};
                Runtime rt = Runtime.getRuntime();
                System.out.println("Executing " + cmd[0] + " " + cmd[1] + " " + cmd[2]);
                proc = rt.exec(cmd);
            }
            else if ( ( osName.equals( "Windows 95" ) ) || ( osName.equals( "Windows 98" ) ) )
            {
                String[] cmd = new String[]{"start",filename};
                Runtime rt = Runtime.getRuntime();
                System.out.println("Executing " + cmd[0] + " " + cmd[1]);
                proc = rt.exec(cmd);
            }
            else if ( osName.equals( "Windows 2000" )  )
            {
                String[] cmd = new String[3];
                cmd[0] = "cmd.exe" ;
                cmd[1] = "/C" ;
                cmd[2] = filename.charAt(0) + "\"" + filename.substring( 1 ) + "\"";

                Runtime rt = Runtime.getRuntime();
                System.out.println("Executing " + cmd[0] + " " + cmd[1] + " " + cmd[2]);
                proc = rt.exec(cmd);
            }

            // any error message?
            StreamGobbler errorGobbler = new
                StreamGobbler(proc.getErrorStream(), "ERROR");
            errorGobbler.start();
            // any output?
            StreamGobbler outputGobbler = new
                StreamGobbler(proc.getInputStream(), "OUTPUT");
            outputGobbler.start();

            // any error?
            int exitVal = proc.waitFor();
            System.out.println("ExitValue: " + exitVal);
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
    }

    class StreamGobbler extends Thread
    {
        InputStream is;
        String type;

        StreamGobbler(InputStream is, String type)
        {
            this.is = is;
            this.type = type;
        }

        public void run()
        {
            try
            {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line=null;
                while ( (line = br.readLine()) != null)
                      System.out.println(type + ">" + line);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
}
