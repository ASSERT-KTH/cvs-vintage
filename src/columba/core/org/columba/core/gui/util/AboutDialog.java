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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.*;

import org.columba.core.util.GlobalResourceLoader;

import org.columba.mail.gui.util.AddressLabel;
import org.columba.mail.gui.util.URLLabel;

public class AboutDialog implements ActionListener {
    
    public static final String CMD_CLOSE = "CLOSE";
    private static final String RESOURCE_BUNDLE_PATH = "org.columba.core.i18n.dialog";
    
    private JDialog dialog;
    private boolean bool = false;

    public AboutDialog( )
    {
	showDialog();
    }

    public void showDialog()
    {
	//LOCALIZE
        dialog = DialogStore.getDialog(GlobalResourceLoader.getString(RESOURCE_BUNDLE_PATH, "about", "title") + org.columba.core.main.MainInterface.version);
	JPanel contentPane = (JPanel)dialog.getContentPane();
	contentPane.setLayout(new BorderLayout(0,0));
	contentPane.add(new JLabel(ImageLoader.getImageIcon("splash.gif")),BorderLayout.NORTH);
        JPanel contactPanel = new JPanel(new GridBagLayout());
	contactPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(10,12,0,11),
				BorderFactory.createTitledBorder(GlobalResourceLoader.getString(RESOURCE_BUNDLE_PATH, "about", "contact"))));

        GridBagConstraints c = new GridBagConstraints();
        JLabel authorLabel = new JLabel(GlobalResourceLoader.getString(RESOURCE_BUNDLE_PATH, "about", "authors"));
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

        JLabel websiteLabel = new JLabel(GlobalResourceLoader.getString(RESOURCE_BUNDLE_PATH, "about", "website"));
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
	JButton closeButton = new JButton(GlobalResourceLoader.getString("global", "global", "close"));
	closeButton.setActionCommand(CMD_CLOSE);
	closeButton.addActionListener(this);
	buttonPanel.add(closeButton,BorderLayout.EAST);
	contentPane.add(buttonPanel,BorderLayout.SOUTH);
	dialog.getRootPane().setDefaultButton(closeButton);
	dialog.getRootPane().registerKeyboardAction(this,CMD_CLOSE,KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0),JComponent.WHEN_IN_FOCUSED_WINDOW);
        dialog.pack();
	dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    public void actionPerformed(ActionEvent e)
    {
	if(CMD_CLOSE.equals(e.getActionCommand())){
	    bool=true;
	    dialog.dispose();
	}
    }

    public boolean success()
    {
        return bool;
    }
}
