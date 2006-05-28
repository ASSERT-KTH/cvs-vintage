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

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;

import javax.help.CSH;

import net.javaprog.ui.wizard.DataModel;
import net.javaprog.ui.wizard.DefaultWizardModel;
import net.javaprog.ui.wizard.JavaHelpSupport;
import net.javaprog.ui.wizard.Step;
import net.javaprog.ui.wizard.Wizard;
import net.javaprog.ui.wizard.WizardModel;

import org.columba.core.help.HelpManager;
import org.columba.core.resourceloader.IconKeys;
import org.columba.core.resourceloader.ImageLoader;
import org.columba.mail.util.MailResourceLoader;
import org.frapuccino.swing.ActiveWindowTracker;

public class AccountWizardLauncher {
	public AccountWizardLauncher() {
	}

	public void launchWizard(boolean firstStart) {
		DataModel data = new DataModel();
		Step[] steps;

		if (firstStart) {
			steps = new Step[] { new WelcomeStep(), new IdentityStep(data),
					new IncomingServerStep(data),
					new OutgoingServerStep(data, false), new FinishStep() };
		} else {
			steps = new Step[] { new IdentityStep(data),
					new IncomingServerStep(data),
					new OutgoingServerStep(data, true) };
		}

		WizardModel model = new DefaultWizardModel(steps);
		model.addWizardModelListener(new AccountCreator(data));

		Window w = ActiveWindowTracker.findActiveWindow();

		Wizard wizard = null;

		if (w instanceof Frame)
			wizard = new Wizard((Frame) w, model, MailResourceLoader.getString(
					"dialog", "accountwizard", "title"), ImageLoader
					.getSmallIcon(IconKeys.PREFERENCES));
		else
			wizard = new Wizard((Dialog) w, model, MailResourceLoader
					.getString("dialog", "accountwizard", "title"), ImageLoader
					.getSmallIcon(IconKeys.PREFERENCES));

		wizard.setStepListRenderer(null);
		CSH.setHelpIDString(wizard, "getting_started_1");
		JavaHelpSupport.enableHelp(wizard, HelpManager.getInstance()
				.getHelpBroker());
		wizard.pack();
		wizard.setLocationRelativeTo(null);
		wizard.setVisible(true);
	}
}