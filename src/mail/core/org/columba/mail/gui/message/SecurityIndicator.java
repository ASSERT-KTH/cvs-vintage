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

package org.columba.mail.gui.message;

import java.awt.*;
import javax.swing.*;

import org.columba.mail.gui.util.*;
import org.columba.mail.message.*;
import org.columba.mail.gui.message.util.*;
import org.columba.core.gui.util.*;
import org.columba.main.*;


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
