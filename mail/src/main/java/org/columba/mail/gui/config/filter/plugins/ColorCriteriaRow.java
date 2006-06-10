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
package org.columba.mail.gui.config.filter.plugins;

import java.awt.Color;

import javax.swing.JComboBox;

import org.columba.api.plugin.IExtensionHandler;
import org.columba.core.filter.FilterCriteria;
import org.columba.core.gui.base.ColorComboBox;
import org.columba.core.gui.base.ColorItem;
import org.columba.mail.gui.config.filter.CriteriaList;

/**
 * A criteria row for Color filter.
 * 
 * The user can select that either the message has or has not a specific color.
 * 
 * @author redsolo
 */
public class ColorCriteriaRow extends DefaultCriteriaRow {
	private ColorComboBox colorComboBox;

	private JComboBox matchComboBox;

	/**
	 * A criteria row for the color filter.
	 * 
	 * @param pluginHandler
	 *            a plugin handler
	 * @param criteriaList
	 *            the criteria list
	 * @param c
	 *            the filter criteria to load/save data
	 */
	public ColorCriteriaRow(IExtensionHandler pluginHandler,
			CriteriaList criteriaList, FilterCriteria c) {
		super(pluginHandler, criteriaList, c);
	}

	/** {@inheritDoc} */
	public void initComponents() {
		super.initComponents();

		matchComboBox = new JComboBox();
		matchComboBox.addItem("is");
		matchComboBox.addItem("is not");

		colorComboBox = new ColorComboBox();

		addComponent(matchComboBox);
		addComponent(colorComboBox);
	}

	/** {@inheritDoc} */
	public void updateComponents(boolean b) {
		super.updateComponents(b);

		if (b) {
			matchComboBox.setSelectedItem(criteria.getCriteriaString());

			colorComboBox.setSelectedColor(criteria.getPatternString());
			colorComboBox.setCustomColor(criteria.getIntegerWithDefault("rgb",
					Color.black.getRGB()));
		} else {
			criteria
					.setCriteriaString((String) matchComboBox.getSelectedItem());

			ColorItem item = colorComboBox.getSelectedColorItem();
			criteria.setPatternString(item.getName());
			criteria.setInteger("rgb", item.getColor().getRGB());
		}
	}
}
