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

import java.io.*;
import java.net.URL;
import javax.swing.*;

import org.columba.main.MainInterface;
import org.columba.mail.parser.*;
import org.columba.mail.message.*;
import org.columba.mail.gui.message.*;

public class MimeTypeViewer
{
    private String osName = System.getProperty("os.name");

    public MimeTypeViewer(){}

    protected boolean isWindowsPlatform()
    {
	return osName.startsWith("Windows");
    }

    public Process openWith( MimeHeader header, File tempFile )
    {
	DefaultViewer viewer;
        if ( isWindowsPlatform() ) {
            viewer = new WindowsViewer();
        }else{
            viewer = new ColumbaViewer();
        }
	return viewer.openWith( header, tempFile );
    }

    public Process open( MimeHeader header, File tempFile )
    {
        DefaultViewer viewer;
	if ( isWindowsPlatform() ) {
            viewer = new WindowsViewer();
        }else{
            viewer = new ColumbaViewer();
        }
	return viewer.open( header, tempFile );
    }

    public Process openURL( URL url )
    {
        DefaultViewer viewer;
	if ( isWindowsPlatform() ) {
            viewer = new WindowsViewer();
        }else{
            viewer = new ColumbaViewer();
        }
        return viewer.openURL( url );
    }

    public Process openWithURL( URL url )
    {
        DefaultViewer viewer;
	if ( isWindowsPlatform() ) {
            viewer = new WindowsViewer();
        }else{
            viewer = new ColumbaViewer();
        }
	return viewer.openWithURL( url );
    }

	/*
    public void openWithBrowserURL( URL url )
    {
        boolean b = MainInterface.frameController.messageController.getView().enableViewer( MessageView.HTML );
        //MainInterface.frameController.messageController.getView().update(b);

        HtmlViewer viewer = (HtmlViewer) MainInterface.frameController.messageController.getView().getViewer( MessageView.HTML );

        if ( viewer != null )
        {
            try
            {
                viewer.setPage( url );
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
                if ( ex instanceof java.net.UnknownHostException )
                {
                    JOptionPane.showMessageDialog( MainInterface.frameController.getView(),
                                                           "Unknown host exception: "+ex.getMessage() );
                }
                else
                {
                    JOptionPane.showMessageDialog( MainInterface.frameController.getView(),
                                                           "Browser exception: "+ex.getMessage() );
                }
            }
        }
    }
    */
}
