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
//$Log: WizardPanelSequence.java,v $
//Revision 1.1  2003/02/03 14:58:41  fdietz
//[intern]wizard fixes
//
package org.columba.core.gui.util.wizard;

import java.util.Vector;

/**
 * @author frd
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class WizardPanelSequence {

	protected Vector list;
	protected int selected;

	/**
	 * Constructor for WizardPanelSequence.
	 */
	public WizardPanelSequence() {
		super();

		list = new Vector();
		selected = 0;
	}

	public DefaultWizardPanel getNextPanel() {
		return (DefaultWizardPanel) list.get(++selected);
	}

	public DefaultWizardPanel getPreviousPanel() {
		return (DefaultWizardPanel) list.get(--selected);
	}

	public DefaultWizardPanel getFirstPanel() {
		return (DefaultWizardPanel) list.get(0);
	}

	public DefaultWizardPanel getLastPanel() {
		return (DefaultWizardPanel) list.get(list.size() - 1);
	}

	public void addPanel(DefaultWizardPanel panel) {
		list.add(panel);
	}

	public int count() {
		return list.size();
	}

	public boolean isLast(DefaultWizardPanel p) {
		DefaultWizardPanel panel = getLastPanel();

		if (panel.equals(p))
			return true;

		return false;
	}

	public boolean isFirst(DefaultWizardPanel p) {
		DefaultWizardPanel panel = getFirstPanel();

		if (panel.equals(p))
			return true;

		return false;
	}

	public boolean hasSuccessor(DefaultWizardPanel p) {
		for (int i = 0; i < count(); i++) {
			DefaultWizardPanel panel = (DefaultWizardPanel) list.get(i);

			if (panel.equals(p)) {
				if (i < count() - 1)
					return true;

			}
		}

		return false;
	}

	public boolean hasPredeccessor(DefaultWizardPanel p) {
		for (int i = 0; i < count(); i++) {
			DefaultWizardPanel panel = (DefaultWizardPanel) list.get(i);

			if (panel.equals(p)) {
				if (i > 0)
					return true;

			}
		}

		return false;
	}
}
