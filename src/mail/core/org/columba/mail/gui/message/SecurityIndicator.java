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
package org.columba.mail.gui.message;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.columba.core.gui.util.ImageLoader;


public class SecurityIndicator extends JPanel
{
    public static final int DECRYPTION_SUCCESS = 0;
    public static final int DECRYPTION_FAILURE = 1;
    public static final int VERIFICATION_SUCCESS = 2;
    public static final int VERIFICATION_FAILURE = 3;
    public static final int NOKEY = 4;
    public static final int NOOP = 5;

    public SecurityIndicator()
    {
	setLayout( new BorderLayout() );
	setValue( NOOP );
    }


    public void setValue( int value )
    {
	removeAll();

	JLabel label = new JLabel();
	//label.setMargins( new Insets(0,0,0,0) );
	label.setHorizontalAlignment( SwingConstants.RIGHT );
	label.setText(" ");
	switch ( value )
	    {
	        case  DECRYPTION_SUCCESS:
		{

		    label.setIcon( ImageLoader.getImageIcon(
							    "pgp-signature-ok.png" ) );
		    label.setToolTipText("Message was encrypted successfully");
		    break;
		}
		case  DECRYPTION_FAILURE:
		{
		    label.setIcon( ImageLoader.getImageIcon(
							    "pgp-signature-bad.png" ) );
		    label.setToolTipText("Message encryption failed");
		    break;
		}
		case  VERIFICATION_SUCCESS:
		{
		    label.setIcon( ImageLoader.getImageIcon(
							    "pgp-signature-ok.png" ) );
		    label.setToolTipText("Message was verified successfully");
		    break;
		}
		case  VERIFICATION_FAILURE:
		{
		    label.setIcon( ImageLoader.getImageIcon(
							    "pgp-signature-bad.png" ) );

		    label.setToolTipText("Message verification failed");
		    break;
		}
		case  NOKEY:
		{
		    label.setIcon( ImageLoader.getImageIcon(
							    "pgp-signature-nokey.png" ) );
		    label.setToolTipText("You don't have the right key\n\nto verify this message");
		    break;
		}
		case  NOOP:
		{
		    label.setText("");
		    break;
		}

	    }

	add( label );

	revalidate();

        repaint();
    }
}
