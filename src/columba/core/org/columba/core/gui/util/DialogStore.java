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

package org.columba.core.gui.util;

import javax.swing.JDialog;
import javax.swing.JFrame;

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
        dialog.setLocationRelativeTo(null);
        return dialog;
    }

    public static JDialog getDialog( String title)
    {
        JDialog dialog = new JDialog(mainFrame, title, true );
        dialog.setLocationRelativeTo(null);
        return dialog;
    }

	public static JDialog getDialog( JFrame frame )
	{
		JDialog dialog = new JDialog(frame, true);
		dialog.setLocationRelativeTo(null);
		return dialog;
	}
	
	

}