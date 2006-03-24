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
package org.columba.core.gui.externaltools;

import java.io.File;

import net.javaprog.ui.wizard.DataModel;
import net.javaprog.ui.wizard.WizardModelEvent;
import net.javaprog.ui.wizard.WizardModelListener;

import org.columba.core.config.Config;
import org.columba.core.xml.XmlElement;


/**
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 *
 * @author fdietz
 */
class ExternalToolsWizardModelListener implements WizardModelListener {
    protected DataModel data;
    protected boolean finished = false;

    public ExternalToolsWizardModelListener(DataModel data) {
        this.data = data;
    }

    public void wizardFinished(WizardModelEvent e) {
       
        // get location of executable
        File sourceFile = (File) data.getData("Location.source");

        // get plugin ID
        String id = (String) data.getData("id");

        // get configuration
        XmlElement root = Config.getInstance().get("external_tools").getElement("tools");

        for (int i = 0; i < root.count(); i++) {
            XmlElement child = root.getElement(i);

            if (child.getAttribute("name").equals(id)) {
                // set configuration of this plugin
                child.addAttribute("first_time", "false");
                child.addAttribute("location", sourceFile.getPath());

                // exit for-loop
                break;
            }
        }

        finished = true;
    }

    public void stepShown(WizardModelEvent e) {
    }

    public void wizardCanceled(WizardModelEvent e) {
    }

    public void wizardModelChanged(WizardModelEvent e) {
    }

    public boolean isFinished() {
        return finished;
    }
}
