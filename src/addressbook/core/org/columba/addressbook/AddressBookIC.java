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

package org.columba.addressbook;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;

import org.columba.addressbook.gui.AddressbookPanel;
import org.columba.core.gui.util.ImageLoader;
import org.columba.mail.gui.composer.ComposerController;

public class AddressBookIC {
		
	public static JFrame createAddressbookListFrame( ComposerController controller)
	{
		JFrame frame = new JFrame();
		frame.setIconImage( ImageLoader.getImageIcon("ColumbaIcon.png").getImage());
		frame.setContentPane( new AddressbookPanel( controller ) );
		frame.pack();
		Dimension size=frame.getSize();
		Dimension screenSize=Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation((screenSize.width-size.width)/2,(screenSize.height-size.height)/2);
		return frame;
	}

}
