// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.core.gui.util;

import java.awt.FontMetrics;

import java.util.StringTokenizer;

import javax.swing.JTextArea;
import javax.swing.LookAndFeel;


public class MultiLineLabel extends JTextArea {
    private final static int DEFAULT_WIDTH = 300;

    public MultiLineLabel(String s) {
        this(s, DEFAULT_WIDTH);
    }

    public MultiLineLabel(String s, int pixelWidth) {
        setOpaque(false);
        setText(s, pixelWidth);
    }

    public MultiLineLabel(String s, int pixelWidth, int rows, int cols) {
        super(rows, cols);
        setOpaque(false);
        setText(s, pixelWidth);
    }

    public void setText(String s, int pixels) {
        super.setText(createSizedString(s, pixels));
    }

    public void setText(String s) {
        setText(s, DEFAULT_WIDTH);
    }

    private String createSizedString(final String message, final int pixels) {
        if (message == null) {
            return "";
        }

        if (message.length() == 0) {
            return "";
        }

        FontMetrics fm = getFontMetrics(getFont());

        String word;
        StringBuffer sb = new StringBuffer();
        StringBuffer cursb;
        StringTokenizer st;

        /*
         * StringTokenizer will split a\n\nb in only 2 parts. String replace
         * all provides a workaround by generating a\n \nb,
         */
        StringTokenizer stn = new StringTokenizer(message.replaceAll("\n\n",
                    "\n \n"), "\n");

        while (stn.hasMoreTokens()) {
            st = new StringTokenizer(stn.nextToken());
            cursb = new StringBuffer();

            while (st.hasMoreTokens()) {
                word = st.nextToken();

                if (fm.stringWidth(cursb.toString() + word) > pixels) {
                    sb.append(cursb.toString());
                    sb.append("\n");
                    cursb = new StringBuffer();
                }

                cursb.append(word);
                cursb.append(" ");
            }

            sb.append(cursb.toString() + "\n");
        }

        return sb.toString();
    }

    /*
     * public MultiLineLabel( String s ) { super( s );
     *
     * //setBorder( UIManager.getBorder( "Label.border" ) ); //setBorder(
     * BorderFactory.createEmptyBorder( 5,5,5,5 ) );
     *
     * setEditable( false ); setBackground( UIManager.getColor(
     * "Label.background" ) ); setFont( UIManager.getFont( "Label.font") );
     * setWrapStyleWord( true );
     *  }
     *
     *
     *
     * public MultiLineLabel( String[] s ) { StringBuffer buf = new
     * StringBuffer();
     *
     * for ( int i=0; i <s.length; i++ ) { buf.append( s[i] ); }
     *
     * setText( buf.toString() ); setBorder( BorderFactory.createEmptyBorder(
     * 10,10,10,10 ) ); setEditable( false ); setBackground(
     * UIManager.getColor( "Label.background" ) ); setFont( UIManager.getFont(
     * "Label.font") ); setWrapStyleWord( true ); }
     */
    public void updateUI() {
        super.updateUI();

        //setLineWrap(true);
        setWrapStyleWord(true);
        setHighlighter(null);
        setEditable(false);
        LookAndFeel.installBorder(this, "Label.border");
        LookAndFeel.installColorsAndFont(this, "Label.background",
            "Label.foreground", "Label.font");
    }

    public boolean isFocusTraversable() {
        return false;
    }

    public boolean isRequestFocusEnabled() {
        return false;
    }

    public boolean isFocusable() {
        return false;
    }
}
