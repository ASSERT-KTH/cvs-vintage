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
package org.columba.mail.gui.composer.html.action;

import java.awt.event.ActionEvent;
import java.util.Observable;
import java.util.Observer;

import org.columba.core.action.AbstractColumbaAction;
import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.xml.XmlElement;
import org.columba.mail.gui.composer.ComposerController;
import org.columba.mail.gui.composer.html.HtmlEditorController;
import org.columba.mail.main.MailInterface;
import org.columba.mail.util.MailResourceLoader;


/**
 * Inserts the html element &lt;br&gt (br tag), i.e. a line break.
 *
 * @author Karl Peder Olesen (karlpeder), 20030923
 */
public class InsertBreakAction extends AbstractColumbaAction implements Observer {
    /**
     * @param frameMediator
     */
    public InsertBreakAction(FrameMediator frameMediator) {
        super(frameMediator,
            MailResourceLoader.getString("menu", "composer", "menu_format_break"));
        putValue(SHORT_DESCRIPTION,
            MailResourceLoader.getString("menu", "composer",
                "menu_format_break_tooltip").replaceAll("&", ""));

        // register for changes to editor type (text / html)
        XmlElement optionsElement = MailInterface.config.get("composer_options")
                                                        .getElement("/options");
        XmlElement htmlElement = optionsElement.getElement("html");

        if (htmlElement == null) {
            htmlElement = optionsElement.addSubElement("html");
        }

        String enableHtml = htmlElement.getAttribute("enable", "false");
        htmlElement.addObserver(this);

        // set initial enabled state
        setEnabled(Boolean.valueOf(enableHtml).booleanValue());
    }

    /**
     * Method is called when the html option has changed.
     * <br>
     * Used to enable / disable the action and associated menu
     *
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    public void update(Observable arg0, Object arg1) {
        if (arg0 instanceof XmlElement) {
            // possibly change btw. html and text
            XmlElement e = (XmlElement) arg0;

            if (e.getName().equals("html")) {
                // This action should only be enabled in html mode
                setEnabled(Boolean.valueOf(e.getAttribute("enable", "false"))
                                  .booleanValue());
            }
        }
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent evt) {
        // this action is disabled when the text/plain editor is used
        // -> so, its safe to just cast to HtmlEditorController here
        HtmlEditorController editorController = (HtmlEditorController) ((ComposerController) frameMediator).getEditorController();

        editorController.insertBreak();
    }
}
