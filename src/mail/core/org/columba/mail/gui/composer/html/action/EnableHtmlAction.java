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

import org.columba.api.gui.frame.IFrameMediator;
import org.columba.core.gui.action.AbstractSelectableAction;
import org.columba.core.xml.XmlElement;
import org.columba.mail.config.MailConfig;
import org.columba.mail.gui.composer.ComposerController;
import org.columba.mail.util.MailResourceLoader;


/**
 * CheckBox menu item for switching between HTML and text messages.
 * <br>
 * This will change the stored option, which in turn are told to
 * notify observers => editor changes btw. HTML and text etc.
 *
 * @author fdietz, Karl Peder Olesen
 */
public class EnableHtmlAction extends AbstractSelectableAction {
    /**
     * @param frameMediator
     * @param name
     */
    public EnableHtmlAction(IFrameMediator frameMediator) {
        super(frameMediator,
            MailResourceLoader.getString("menu", "composer",
                "menu_format_enable_html"));

        // enable/disable menuitem, based on configuration text/html state
        XmlElement optionsElement = MailConfig.getInstance().get("composer_options")
                                                        .getElement("/options");
        XmlElement htmlElement = optionsElement.getElement("html");

        //	create default element if not available
        if (htmlElement == null) {
            htmlElement = optionsElement.addSubElement("html");
        }

        String enableHtml = htmlElement.getAttribute("enable", "false");
        setState(Boolean.valueOf(enableHtml).booleanValue());
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent evt) {
        XmlElement optionsElement = MailConfig.getInstance().get("composer_options")
                                                        .getElement("/options");
        XmlElement htmlElement = optionsElement.getElement("html");

        //	create default element if not available
        if (htmlElement == null) {
            htmlElement = optionsElement.addSubElement("html");
        }

        // change configuration based on menuitem selection	 
        htmlElement.addAttribute("enable", Boolean.toString(getState()));
        
        //Update the composer
        ((ComposerController)getFrameMediator()).toggleHtmlMode();
    }
}
