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

import java.awt.*;
import java.awt.event.*;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.*;
import javax.swing.border.*;

import org.columba.mail.message.*;
import org.columba.main.*;
import org.columba.mail.gui.util.*;

public class AboutDialog implements ActionListener
{
    private JDialog dialog;
    private boolean bool = false;

    public AboutDialog( )
    {
	showDialog();
    }

    public void showDialog()
    {
	//LOCALIZE
        dialog = DialogStore.getDialog("About Columba v"+org.columba.main.MainInterface.version);
	JPanel contentPane = (JPanel)dialog.getContentPane();
	contentPane.setLayout(new BorderLayout(0,0));
	contentPane.add(new JLabel(ImageLoader.getImageIcon("splash.gif")),BorderLayout.NORTH);
        JPanel contactPanel = new JPanel(new GridBagLayout());
	//LOCALIZE
	contactPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(10,12,0,11),
				BorderFactory.createTitledBorder("Contact")));

        GridBagConstraints c = new GridBagConstraints();
	//LOCALIZE
        JLabel authorLabel = new JLabel("Authors:");
	//Font font = MainInterface.columbaTheme.getControlTextFont();
	Font font = UIManager.getFont("Label.font");
	if ( font != null )
	{
            font = font.deriveFont( Font.BOLD );
            authorLabel.setFont( font );
	}
	c.gridx = 0;
	c.gridy = 0;
	c.anchor = GridBagConstraints.WEST;
	contactPanel.add( authorLabel, c );

	c.gridx = 1;
	c.gridy = 0;
	c.anchor = GridBagConstraints.WEST;
	Component box = Box.createRigidArea( new Dimension( 10,10 ) );
	contactPanel.add( box, c );

        AddressLabel a1 = new AddressLabel( "Frederik Dietz <fdietz@users.sourceforge.net>" );
        c.gridx = 2;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = GridBagConstraints.REMAINDER;
        contactPanel.add( a1, c );

        AddressLabel a2 = new AddressLabel( "Timo Stich <tstich@users.sourceforge.net>" );
        c.gridx = 2;
        c.gridy = 1;
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = GridBagConstraints.REMAINDER;
        contactPanel.add( a2, c );

	//LOCALIZE
        JLabel websiteLabel = new JLabel("Website:");
        if(font!=null)websiteLabel.setFont( font );
        c.gridx = 0;
        c.gridy = 2;
        c.anchor = GridBagConstraints.WEST;
        contactPanel.add( websiteLabel, c );

        URLLabel websiteUrl = null; 
	try{
		websiteUrl = new URLLabel(new URL("http://columba.sourceforge.net"));
	}catch(MalformedURLException mue){} //does not occur
        c.gridx = 2;
        c.gridy = 2;
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = GridBagConstraints.REMAINDER;
        contactPanel.add( websiteUrl, c );

	contentPane.add(contactPanel,BorderLayout.CENTER);
	JPanel buttonPanel = new JPanel(new BorderLayout(0,0));
	buttonPanel.setBorder(BorderFactory.createEmptyBorder(17,12,11,11));
	//LOCALIZE
	JButton closeButton = new JButton("Close");
	closeButton.setActionCommand("CLOSE");
	closeButton.addActionListener(this);
	buttonPanel.add(closeButton,BorderLayout.EAST);
	contentPane.add(buttonPanel,BorderLayout.SOUTH);
	dialog.getRootPane().setDefaultButton( closeButton );
	dialog.getRootPane().registerKeyboardAction(this,"CLOSE",KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0),JComponent.WHEN_IN_FOCUSED_WINDOW);
        dialog.pack();
	dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    public void actionPerformed(ActionEvent e)
    {
	if(e.getActionCommand().equals("CLOSE")){
	    bool=true;
	    dialog.dispose();
	}
    }

    public boolean success()
    {
        return bool;

    }
}
