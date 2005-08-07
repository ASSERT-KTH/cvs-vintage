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

import org.columba.api.gui.frame.IFrameMediator;
import org.columba.core.filter.FilterAction;
import org.columba.core.gui.base.ColorComboBox;
import org.columba.core.gui.base.ColorFactory;
import org.columba.core.gui.base.ColorItem;
import org.columba.mail.gui.config.filter.ActionList;


/**
 * A configuration panel for the <code>ColorMessageFilterAction</code>
 * This displays a <code>JComboBox</code> filled with different colors.
 *
 * @author redsolo
 */
public class ColorActionConfig extends DefaultActionRow {
    private ColorComboBox colorsComboBox;

    /**
 * @param list the action list (?)
 * @param action the action to configure.
 */
    public ColorActionConfig(IFrameMediator mediator,ActionList list, FilterAction action) {
        super(mediator, list, action);
    }

    /** {@inheritDoc} */
    public void initComponents() {
        super.initComponents();
        colorsComboBox = new ColorComboBox();

        // Add the custom color item.
        int rgb = getFilterAction().getIntegerWithDefault("rgb", Color.black.getRGB());
        colorsComboBox.setCustomColor(ColorFactory.getColor(rgb));

        addComponent(colorsComboBox);
    }

    /** {@inheritDoc} */
    public void updateComponents(boolean b) {
        super.updateComponents(b);

        if (b) {
            String string = getFilterAction().get("color");
            colorsComboBox.setSelectedColor(string);
        } else {
            ColorItem object = (ColorItem) colorsComboBox.getSelectedColorItem();

            if (object != null) {
                getFilterAction().setString("color", object.getName());
                getFilterAction().setInteger("rgb", object.getColor().getRGB());
            }
        }
    }
}
