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
//All Rights Reserved.undation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
package org.columba.mail.gui.composer.text;

import java.awt.Dimension;
import java.awt.Font;
import java.nio.charset.Charset;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JTextPane;

import org.columba.core.charset.CharsetEvent;
import org.columba.core.charset.CharsetListener;
import org.columba.core.gui.util.FontProperties;
import org.columba.core.main.MainInterface;
import org.columba.core.xml.XmlElement;
import org.columba.mail.gui.composer.util.UndoDocument;


/**
 * @author frd
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class TextEditorView extends JTextPane implements Observer,
    CharsetListener {
    private TextEditorController controller;
    private UndoDocument message;

    public TextEditorView(TextEditorController controller, UndoDocument m) {
        super();

        this.controller = controller;
        controller.getController().addCharsetListener(this);

        message = m;

        setStyledDocument(message);
        setEditable(true);

        Font font = FontProperties.getTextFont();
        setFont(font);

        XmlElement options = MainInterface.config.get("options").getElement("/options");
        XmlElement gui = options.getElement("gui");
        XmlElement fonts = gui.getElement("fonts");

        if (fonts == null) {
            fonts = gui.addSubElement("fonts");
        }

        // register interest on configuratin changes
        fonts.addObserver(this);

        setPreferredSize(new Dimension(300, 200));
    }

    public void installListener(TextEditorController controller) {
        message.addDocumentListener(controller);
    }

    /**
     *
     * @see org.columba.mail.gui.config.general.MailOptionsDialog
     *
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    public void update(Observable arg0, Object arg1) {
        Font font = FontProperties.getTextFont();
        setFont(font);
    }

    public void charsetChanged(CharsetEvent e) {
        Charset charset = e.getCharset();

        if (charset == null) {
            charset = Charset.forName(System.getProperty("file.encoding"));
        }

        setContentType("text/plain; charset=\"" + charset.name() + "\"");
    }
}
