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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

public class NotifyDialog
{

    
    private JDialog dialog;
    private boolean bool = false;
    private MultiLineLabel textField;



    public NotifyDialog(  )
    {
        

    }


	public void showDialog( Exception ex )
	{
		showDialog( ex.getMessage() );
	}

    public void showDialog( String message )
    {
        JLabel topLabel = new JLabel( "An Error occured:",
                                      ImageLoader.getImageIcon("stock_dialog_error_48.png"),
                                      SwingConstants.LEFT
                                      );

        JButton[] buttons = new JButton[1];

        MultiLineLabel textArea = new MultiLineLabel( message );
        textArea.setColumns(40);
        textArea.setRows(4);
        textArea.setLineWrap( true );
        textArea.setWrapStyleWord( true );
        JScrollPane scrollPane = new JScrollPane( textArea );



        buttons[0] = new JButton("Close");
        buttons[0].setActionCommand("CLOSE");
		buttons[0].setDefaultCapable(true);



        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();

        dialog = DialogStore.getDialog("Error occured...");
        dialog.getContentPane().setLayout( layout );
		dialog.getRootPane().setDefaultButton(buttons[0]);

        c.gridx=0;
        c.gridy=0;
        c.gridwidth = 1;
        c.weightx = 1;
        c.insets = new Insets(10,10,0,20);
        c.anchor = GridBagConstraints.WEST;
        layout.setConstraints( topLabel, c);


        c.gridx=0;
        c.gridy=1;
        c.gridwidth = 1;
        c.weightx = 1;
        c.insets = new Insets(10,20,10,15);
        c.anchor = GridBagConstraints.WEST;
        layout.setConstraints( scrollPane, c);



        JPanel panel = new JPanel();
        //panel.add( buttons[1] );
        panel.add( buttons[0] );

        c.gridx=0;
        c.gridy=5;
        c.weightx = 1.0;
        //c.gridwidth = GridBagConstraints.REMAINDER;
        c.gridwidth = 1;
        c.insets = new Insets(5,10,10,10);
        c.anchor = GridBagConstraints.SOUTHEAST;
        layout.setConstraints(panel, c);

        dialog.getContentPane().add ( scrollPane );
        dialog.getContentPane().add( topLabel );
        dialog.getContentPane().add( panel );
        //dialog.getContentPane().add( buttons[1] );

        dialog.pack();


        java.awt.Dimension dim = dialog.getPreferredSize();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        dialog.setLocation(screenSize.width/2 - dim.width/2, screenSize.height/2 - dim.height/2);


        buttons[0].addActionListener(
                new ActionListener()
                    {
                        public void actionPerformed(ActionEvent e)
                        {
                            String action = e.getActionCommand();

                            if  ( action.equals("CLOSE") )
                                {

                                    bool = true;

                                    dialog.dispose();
                                }


                        }
                    }
                    );





        dialog.show();

    }


    public boolean success()
    {
        return bool;

    }



}
