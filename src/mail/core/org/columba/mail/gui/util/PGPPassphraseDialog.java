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

package org.columba.mail.gui.util;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import org.columba.core.gui.util.*;

public class PGPPassphraseDialog implements ActionListener
{
    private char[] password;

    //private JFrame frame;
    private JDialog dialog;
    private boolean bool = false;
    private JPasswordField passwordField;
    private JTextField loginTextField;
    private JCheckBox checkbox;
    private JLabel checkLabel;

    private String user;
    private boolean save;

    public PGPPassphraseDialog( String id, boolean save)
    {
        this.user = id;
        this.save = save;
	init();
    }

    protected void init()
    {
        dialog = DialogStore.getDialog();
        dialog.getContentPane().setLayout( new BoxLayout( dialog.getContentPane(), BoxLayout.Y_AXIS ) );

        JButton[] buttons = new JButton[2];

        //JLabel hostLabel = new JLabel("Server: "+host);
	//LOCALIZE
        JLabel idLabel = new JLabel("User Id: "+ user );
        //JLabel loginLabel = new JLabel("Login:");
	//LOCALIZE
        JLabel passwordLabel = new JLabel("Password:");

        buttons[0] = new JButton("Cancel");
        buttons[0].addActionListener( this );
        buttons[0].setActionCommand( "CANCEL" );

        buttons[1] = new JButton("Ok");
        buttons[1].addActionListener( this );
        buttons[1].setActionCommand( "OK" );
       	buttons[1].setDefaultCapable(true);
        passwordField = new JPasswordField(15);
        passwordField.setMaximumSize( new Dimension( 150,25) );
        //loginTextField = new JTextField(user, 15);
        //loginTextField.setMaximumSize( new Dimension( 150,25) );
	//LOCALIZE
	checkbox = new JCheckBox( "Remember Password for the remainder of this session" );
	checkbox.setSelected( save );
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout( new BoxLayout( centerPanel, BoxLayout.Y_AXIS ) );
        centerPanel.setBorder( BorderFactory.createEmptyBorder(10,10,10,10) );
        //TitledBorder etched = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), " Login ");
	//centerPanel.setBorder( etched );

        centerPanel.add( Box.createRigidArea( new Dimension(0,5) ) );

        JPanel panel = new JPanel();
        panel.setLayout( new BoxLayout( panel, BoxLayout.X_AXIS ) );
        panel.add( idLabel );
        centerPanel.add( panel );

        centerPanel.add( Box.createRigidArea( new Dimension(0,5) ) );

        /*
        panel = new JPanel();
        panel.setLayout( new BoxLayout( panel, BoxLayout.X_AXIS ) );
        panel.add( Box.createRigidArea( new Dimension(5,0) ) );
        panel.add( loginLabel );
        panel.add( Box.createHorizontalGlue() );
        panel.add( loginTextField );
        panel.add( Box.createRigidArea( new Dimension(5,0) ) );
        centerPanel.add( panel );

        centerPanel.add( Box.createRigidArea( new Dimension(0,5) ) );
        */

        panel = new JPanel();
        panel.setLayout( new BoxLayout( panel, BoxLayout.X_AXIS ) );
        panel.add( Box.createRigidArea( new Dimension(5,0) ) );
        panel.add( passwordLabel );
        panel.add( Box.createHorizontalGlue() );
        panel.add( passwordField );
        panel.add( Box.createRigidArea( new Dimension(5,0) ) );
        centerPanel.add( panel );

        centerPanel.add( Box.createRigidArea( new Dimension(0,5) ) );

        panel = new JPanel();
        panel.setLayout( new BoxLayout( panel, BoxLayout.X_AXIS ) );
        panel.add( Box.createRigidArea( new Dimension(5,0) ) );
        panel.add( checkbox );

        panel.add( Box.createRigidArea( new Dimension(5,0) ) );
          /*
            panel.add( checkLabel );
          */
        centerPanel.add( panel );

        centerPanel.add( Box.createRigidArea( new Dimension(0,5) ) );

        dialog.getContentPane().add( centerPanel );

        dialog.getContentPane().add( Box.createRigidArea( new Dimension(0,10) ) );

        panel = new JPanel();
        panel.setLayout( new BoxLayout( panel, BoxLayout.X_AXIS ) );
        panel.add( Box.createRigidArea( new Dimension(50,0) ) );
        panel.add( buttons[0] );
        panel.add( Box.createHorizontalGlue() );
        panel.add( buttons[1] );
        panel.add( Box.createRigidArea( new Dimension(50,0) ) );
        dialog.getContentPane().add( panel );

        dialog.getContentPane().add( Box.createRigidArea( new Dimension(0,20) ) );

	dialog.getRootPane().setDefaultButton(buttons[1]);
	dialog.getRootPane().registerKeyboardAction(this,"CANCEL",KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0),JComponent.WHEN_IN_FOCUSED_WINDOW);
	dialog.pack();
	dialog.setLocationRelativeTo(null);
        dialog.show();
    }

    public char[] getPassword()
    {
        return password;
    }

    public boolean success()
    {
        return bool;
    }

    public boolean getSave()
    {
        return save;
    }

    public void actionPerformed(ActionEvent e)
    {
	String action  = e.getActionCommand();
	if  ( action.equals("OK") )
        {
            password = passwordField.getPassword();

            //user = loginTextField.getText();

            save = checkbox.isSelected();
            bool = true;
            dialog.dispose();
        }
        else if ( action.equals("CANCEL") )
        {
            bool = false;
            dialog.dispose();
        }
    }
}
