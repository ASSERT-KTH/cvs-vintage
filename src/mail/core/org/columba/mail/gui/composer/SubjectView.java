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
package org.columba.mail.gui.composer;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import org.columba.core.gui.util.CTextField;
import org.columba.mail.util.MailResourceLoader;


/**
 * @author frd
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class SubjectView extends CTextField {
    SubjectController controller;

    public SubjectView(SubjectController controller) {
        super(MailResourceLoader.getString("dialog", "composer",
                "composer_no_subject")); //$NON-NLS-1$
        this.controller = controller;
        addFocusListener(new FocusEventHandler());
    }

    public void installListener(SubjectController controller) {
        getDocument().addDocumentListener(controller);
    }

    private class FocusEventHandler extends FocusAdapter {
        /**
 * Used to clear the subject field if the user clicks in it for
 * the first time and the text contained in it is the default emtpy
 * subject test.
 *
 * @param evt The focus event fired when the focus was gained by this
 * component.
 */
        public void focusGained(FocusEvent evt) {
            if (SubjectView.this.getText().equals(MailResourceLoader.getString(
                            "dialog", "composer", "composer_no_subject"))) {
                SubjectView.this.setText("");
            }
        }
    }
}
