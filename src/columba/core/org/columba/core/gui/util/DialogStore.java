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
package org.columba.core.gui.util;

import java.awt.Component;

import javax.swing.JDialog;
import javax.swing.JFrame;

import org.columba.core.util.Compatibility;
import org.columba.mail.gui.frame.MailFrameView;

public class DialogStore
{
    private static MailFrameView mainFrame;


    public DialogStore( MailFrameView mainFrame )
    {
        DialogStore.mainFrame = mainFrame;
        
    }

    public static JDialog getDialog()
    {
        JDialog dialog = new JDialog(mainFrame, true );
        // for jdk1.3 compatibility, this is called dynamically
		Compatibility.simpleSetterInvoke(dialog, "setLocationRelativeTo", Component.class, null );
        //dialog.setLocationRelativeTo(null);
        return dialog;
    }

    public static JDialog getDialog( String title)
    {
        JDialog dialog = new JDialog(mainFrame, title, true );
//		for jdk1.3 compatibility, this is called dynamically
			 Compatibility.simpleSetterInvoke(dialog, "setLocationRelativeTo", Component.class, null );
        //dialog.setLocationRelativeTo(null);
        return dialog;
    }

	public static JDialog getDialog( JFrame frame )
	{
		JDialog dialog = new JDialog(frame, true);
		dialog.setLocationRelativeTo(null);
		return dialog;
	}
	
	

}