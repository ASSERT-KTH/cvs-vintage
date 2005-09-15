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
package org.columba.core.gui.themes.plugin;

import javax.swing.JComponent;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.ComponentUI;

import net.javaprog.ui.wizard.plaf.metal.MetalWizardContentPaneUI;
import net.javaprog.ui.wizard.plaf.windows.WindowsStepDescriptionRenderer;

public class PlasticWizardContentPaneUI extends MetalWizardContentPaneUI {
	public PlasticWizardContentPaneUI() {
		UIManager.put(getPropertyPrefix() + "stepListRenderer", null);
		UIManager.put(getPropertyPrefix() + "stepDescriptionRenderer",
				new UIDefaults.ProxyLazyValue(
						WindowsStepDescriptionRenderer.UIResource.class
								.getName()));
		UIManager.put(getPropertyPrefix() + "stepBorder",
				new BorderUIResource.EmptyBorderUIResource(12, 40, 11, 11));
	}

	public static ComponentUI createUI(JComponent c) {
		return new PlasticWizardContentPaneUI();
	}
}