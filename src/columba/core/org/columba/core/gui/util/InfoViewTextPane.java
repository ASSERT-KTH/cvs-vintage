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

import java.awt.Font;
import java.io.IOException;
import java.net.URL;

import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

/**
 * Customized HTML JTextPane.
 *
 * @author fdietz
 */
public class InfoViewTextPane extends JTextPane {
    
    /**
     *
     */
    public InfoViewTextPane() {
        super();
        
        HTMLEditorKit editorKit = new HTMLEditorKit();
        StyleSheet styles = new StyleSheet();
        
        Font font = UIManager.getFont("Label.font");
        String name = font.getName();
        int size = font.getSize();
        String css = "<style type=\"text/css\"><!--p {font-family:\"" + name
            + "\"; font-size:\"" + size + "pt\"}--></style>";
        styles.addRule(css);
        editorKit.setStyleSheet(styles);
        
        setEditorKit(editorKit);
    }
    
    /**
     * @param url
     */
    public InfoViewTextPane(URL url) throws IOException {
        this();
        setPage(url);
    }
}
