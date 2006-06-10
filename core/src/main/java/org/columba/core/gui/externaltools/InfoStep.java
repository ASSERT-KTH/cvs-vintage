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

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;

import net.javaprog.ui.wizard.AbstractStep;

import org.columba.core.gui.base.MultiLineLabel;
import org.columba.core.resourceloader.GlobalResourceLoader;


/**
 * Shows a little info page which explains to the user
 * what happens.
 *
 * @author fdietz
 */
class InfoStep extends AbstractStep {
    private static final String RESOURCE_PATH = "org.columba.core.i18n.dialog";

    public InfoStep() {
        super(GlobalResourceLoader.getString(RESOURCE_PATH, "externaltools",
                "InfoStep.title"),
            GlobalResourceLoader.getString(RESOURCE_PATH, "externaltools",
                "InfoStep.description"));
    }

    /* (non-Javadoc)
         * @see net.javaprog.ui.wizard.AbstractStep#createComponent()
         */
    protected JComponent createComponent() {
        JPanel panel = new JPanel(new BorderLayout());

        MultiLineLabel label = new MultiLineLabel(GlobalResourceLoader.getString(
                    RESOURCE_PATH, "externaltools", "InfoStep.text"));

        panel.add(label, BorderLayout.CENTER);

        return panel;
    }

    /* (non-Javadoc)
 * @see net.javaprog.ui.wizard.Step#prepareRendering()
 */
    public void prepareRendering() {
    }
}
