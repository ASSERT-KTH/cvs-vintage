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
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class FontSelectionDialog implements ActionListener, ListSelectionListener
{
    private JDialog dialog;

    JList fontList;
    JList styleList;
    JList sizeList;
    JTextField preview;
    JTextField fontName;
    JTextField styleName;
    JTextField sizeName;
    JLabel fontLabel;
    JLabel sizeLabel;
    JLabel styleLabel;
    JLabel previewLabel;

    JButton okButton;
    JButton cancelButton;

    Font font;
    int status;


    public FontSelectionDialog( Font f )
    {
        dialog = DialogStore.getDialog();

        font = f;

        dialog.setTitle( "Select a font" );
        dialog.setSize( 450, 325 );

        initData();
        initComponents();
    }

    private void initComponents()
    {
        JScrollPane scroller;
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();

        final double w1 = 3.0;
        final double w2 = 2.0;
        final double w3 = 0.7;

        dialog.getContentPane().setLayout( gridbag );

          // Spacings

        c.insets = new Insets( 1,3,1,3 );

          // First Line with Labels

        c.gridheight = 1;
        c.weighty = 1.0;
        c.weightx = w1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 1;
        gridbag.setConstraints( fontLabel, c );
        dialog.getContentPane().add( fontLabel );

        c.weightx = w2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = GridBagConstraints.RELATIVE;
        gridbag.setConstraints( styleLabel, c );
        dialog.getContentPane().add( styleLabel );

        c.weightx = w3;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints( sizeLabel, c );
        dialog.getContentPane().add( sizeLabel );

          // Second Line with Names

        c.weightx = w1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 1;
        gridbag.setConstraints( fontName, c );
        dialog.getContentPane().add( fontName );

        c.weightx = w2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = GridBagConstraints.RELATIVE;
        gridbag.setConstraints( styleName, c );
        dialog.getContentPane().add( styleName );

        c.weightx = w3;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints( sizeName, c );
        dialog.getContentPane().add( sizeName );


          // Third Line with Lists

        c.weighty = 6.0;

        scroller = new JScrollPane( fontList );
        c.weightx = w1;
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = 1;
        gridbag.setConstraints( scroller, c );
        dialog.getContentPane().add( scroller );

        scroller = new JScrollPane( styleList );
        c.weightx = w2;
        c.gridwidth = GridBagConstraints.RELATIVE;
        gridbag.setConstraints( scroller, c );
        dialog.getContentPane().add( scroller );

        scroller = new JScrollPane( sizeList );
        c.weightx = w3;
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints( scroller, c );
        dialog.getContentPane().add( scroller );

          // 4. Line with PreviewLabel

        c.weighty = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints( previewLabel, c );
        dialog.getContentPane().add( previewLabel );

          // 5. Line with Preview

        c.weightx = 1.0;
        c.weighty = 5.0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.gridheight = GridBagConstraints.RELATIVE;
        c.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints( preview, c );
        dialog.getContentPane().add( preview );

          // 6. Line with Buttons

        okButton = new JButton("OK");
        okButton.setActionCommand( "OK" );
        okButton.addActionListener( this );

        c.weightx = 1.0;
        c.weighty = 1.0;
        c.insets = new Insets( 10,5,10,5);
        c.anchor = GridBagConstraints.EAST;
        c.gridwidth = 1;
        c.gridheight = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.NONE;
        gridbag.setConstraints( okButton, c );
        dialog.getContentPane().add( okButton );


        cancelButton = new JButton("Cancel");
        cancelButton.setActionCommand( "CANCEL" );
        cancelButton.addActionListener( this );

        c.weightx = 1.0;
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.gridheight = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.NONE;
        gridbag.setConstraints( cancelButton, c );
        dialog.getContentPane().add( cancelButton );

        Dimension okSize, cancelSize;

        okSize = okButton.getPreferredSize();
        cancelSize = cancelButton.getPreferredSize();

        if( okSize.width < cancelSize.width ) {
            okSize.width =  cancelSize.width;
            okButton.setPreferredSize( okSize );
        }



    }


    void initData()
    {
        GraphicsEnvironment gEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
        //String envfonts[] = gEnv.getAvailableFontFamilyNames();
	String envfonts[] = gEnv.getAvailableFontFamilyNames( java.util.Locale.getDefault() );
        Vector vector = new Vector();
        for ( int i = 1; i < envfonts.length; i++ ) {
            vector.addElement(envfonts[i]);
        }
        fontList = new JList( vector );
        fontList.setSelectedIndex(0);

        styleList = new JList(  new Object[]{
                                "PLAIN",
                                "BOLD",
                                "ITALIC",
                                "BOLD & ITALIC"
                              });
        styleList.setSelectedIndex(0);

        sizeList = new JList(
            new Object[]{ "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18"}
            );
        sizeList.setSelectedIndex(0);


        preview = new JTextField("abcdefgh ABCDEFGH");
        preview.setHorizontalAlignment( JTextField.CENTER );

        styleName = new JTextField();
        sizeName = new JTextField();
        fontName = new JTextField();

        if( font == null) font = preview.getFont();

        fontList.setSelectedValue( font.getName(), true );
        styleList.setSelectedIndex( font.getStyle() );
        sizeList.setSelectedValue( new Integer(font.getSize()).toString(), true );

        fontName.setText( (String)fontList.getSelectedValue() );
        styleName.setText( (String)styleList.getSelectedValue() );
        sizeName.setText( (String)sizeList.getSelectedValue() );

        styleList.addListSelectionListener( this );
        sizeList.addListSelectionListener( this );
        fontList.addListSelectionListener( this );

        fontLabel = new JLabel( "Font :" );
        sizeLabel = new JLabel( "Size :" );
        styleLabel = new JLabel( "Style :" );
        previewLabel = new JLabel( "Preview :" );
    }

    public void actionPerformed( ActionEvent e )
    {
        String command = e.getActionCommand();

        if( command.equals("OK") ){
            status = 0;
            dialog.dispose();
        }
        else if( command.equals("CANCEL") ) {
            status = 1;
            dialog.dispose();
        }
    }

    public void showDialog()
    {
        dialog.setVisible(true);
    }

    public int getStatus()
    {
        return status;
    }

    public Font getSelectedFont()
    {
        return font;
    }

    public void valueChanged(ListSelectionEvent e) {
        Object list = e.getSource();
        String fontchoice;
        int stChoice, siChoice;

        if ( list == fontList ) {
            fontName.setText( (String)fontList.getSelectedValue() );
        } else if ( list == styleList ) {
            styleName.setText( (String)styleList.getSelectedValue() );
        } else if ( list == sizeList ) {
            sizeName.setText( (String)sizeList.getSelectedValue() );
        }

        fontchoice = fontName.getText();
        stChoice = styleList.getSelectedIndex();

        siChoice = new Integer( sizeName.getText() ).intValue();

        font = new Font( fontchoice, stChoice, siChoice );

        preview.setFont( font );
        preview.repaint();
    }


}
