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

package org.columba.mail.gui.config.accountwizard;

import net.javaprog.ui.wizard.*;

import org.columba.core.gui.util.ImageLoader;

import org.columba.mail.config.MailConfig;
import org.columba.mail.util.MailResourceLoader;

public class AccountWizardLauncher {
    public AccountWizardLauncher() {}
    
    public void launchWizard() {
        DataModel data = new DataModel();
        Step[] steps = new Step[]{
            new IdentityStep(data),
            new IncomingServerStep(data),
            new OutgoingServerStep(data)
            //advanced step?
        };
        if (MailConfig.getAccountList().count() == 0) {
            Step[] temp = new Step[steps.length + 2];
            temp[0] = new WelcomeStep();
            System.arraycopy(steps, 0, temp, 1, steps.length);
            temp[temp.length - 1] = new FinishStep();
            steps = temp;
        }
        WizardModel model = new DefaultWizardModel(steps);
        model.addWizardModelListener(new AccountCreator(data));
        Wizard wizard = new Wizard(model, MailResourceLoader.getString(
                                "dialog",
                                "accountwizard",
                                "title"),
                                ImageLoader.getSmallImageIcon("stock_preferences.png"));
        wizard.setSize(500, 400);
        wizard.setLocationRelativeTo(null);
        wizard.setVisible(true);
    }
}
