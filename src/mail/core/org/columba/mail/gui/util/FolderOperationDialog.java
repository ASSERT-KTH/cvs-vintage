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
package org.columba.mail.gui.util;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.columba.core.gui.util.DialogStore;


public class FolderOperationDialog
{

    private JDialog dialog;
    private boolean bool = false;
    private JTextField textField;




    public void showDialog( Exception ex )
    {
        ex.printStackTrace();
        JButton[] buttons = new JButton[2];
        JLabel label = new JLabel( "   An Exception occured:" );
        JTextArea textArea = new JTextArea( ex.getMessage() , 10,30 );
        textArea.setLineWrap( true );
        JScrollPane scrollPane = new JScrollPane( textArea );
        JLabel label2 = new JLabel( "Do you want to cancel the whole Operation?" );

        buttons[0] = new JButton("No");
        buttons[0].setActionCommand("NO");
	buttons[0].setDefaultCapable(true);
        buttons[1] = new JButton("Yes");
        buttons[1].setActionCommand("YES");



        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();

        dialog = DialogStore.getDialog();
        dialog.getContentPane().setLayout( layout );
	dialog.getRootPane().setDefaultButton(buttons[1]);


        c.gridx=0;
        c.gridy=0;
        c.gridwidth = 1;
        c.weightx = 1;
        c.insets = new Insets(10,10,0,10);
        c.anchor = GridBagConstraints.WEST;
        layout.setConstraints(label, c);

        c.gridx=0;
        c.gridy=1;
        c.gridwidth = 1;
        c.weightx = 1;
        c.insets = new Insets(5,10,10,10);
        c.anchor = GridBagConstraints.CENTER;
        layout.setConstraints( scrollPane, c);

        c.gridx=0;
        c.gridy=2;
        c.gridwidth = 1;
        c.weightx = 1;
        c.insets = new Insets(10,10,10,10);
        c.anchor = GridBagConstraints.CENTER;
        layout.setConstraints(label2, c);

        c.gridx=0;
        c.gridy=4;
        c.weightx = 1.0;
        c.gridwidth = GridBagConstraints.RELATIVE;
        c.insets = new Insets(10,30,10,10);
        c.anchor = GridBagConstraints.SOUTHWEST;
        layout.setConstraints(buttons[0], c);

        c.gridx=0;
        c.gridy=4;
        c.weightx = 1.0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.insets = new Insets(10,10,10,30);
        c.anchor = GridBagConstraints.SOUTHEAST;
        layout.setConstraints(buttons[1], c);

        dialog.getContentPane().add ( label );
        dialog.getContentPane().add ( scrollPane );
        dialog.getContentPane().add ( label2 );
        dialog.getContentPane().add( buttons[0] );
        dialog.getContentPane().add( buttons[1] );

        dialog.pack();

		/*
        java.awt.Dimension dim = dialog.getPreferredSize();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        dialog.setLocation(screenSize.width/2 - dim.width/2, screenSize.height/2 - dim.height/2);
		*/

        for ( int i=0; i<2; i++ )
        {
            buttons[i].addActionListener(
                new ActionListener()
                    {
                        public void actionPerformed(ActionEvent e)
                        {
                            String action = e.getActionCommand();

                            if  ( action.equals("YES") )
                                {

                                    bool = true;

                                    dialog.dispose();
                                }
                            else if ( action.equals("NO") )
                                {
                                    bool = false;

                                    dialog.dispose();
                                }

                        }
                    }
                );
        }



        dialog.show();

    }


    public boolean success()
    {
        return bool;

    }



}
