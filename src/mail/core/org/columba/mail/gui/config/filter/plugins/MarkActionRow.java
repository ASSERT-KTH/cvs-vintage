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

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;

import org.columba.core.gui.frame.FrameMediator;
import org.columba.mail.filter.FilterAction;
import org.columba.mail.gui.config.filter.ActionList;
import org.columba.mail.util.MailResourceLoader;


/**
 * Mark message filter action.
 *
 *
 * @author fdietz
 */
public class MarkActionRow extends DefaultActionRow {
    JComboBox variantComboBox;

    public MarkActionRow(FrameMediator mediator,ActionList list, FilterAction action) {
        super(mediator, list, action);
    }

    public void updateComponents(boolean b) {
        super.updateComponents(b);

        if (b) {
            String variant = filterAction.getMarkVariant();

            // use "mark as read" as default fallback mechanism
            if (variant == null) {
                variant = "read";
            }

            variantComboBox.setSelectedItem(variant);
        } else {
            filterAction.setMarkVariant((String) variantComboBox.getSelectedItem());
        }
    }

    public void initComponents() {
        super.initComponents();

        String[] items = {
            "read", "unread", "expunged", "not_expunged", "flagged",
            "not_flagged", "answered", "spam", "no_spam"
        };

        variantComboBox = new JComboBox(items);
        variantComboBox.setRenderer(new ComboBoxRenderer());
        variantComboBox.setSelectedItem("read");
        addComponent(variantComboBox);
    }

    class ComboBoxRenderer extends DefaultListCellRenderer {
        public ComboBoxRenderer() {
            super();
        }

        public Component getListCellRendererComponent(JList arg0, Object arg1,
            int arg2, boolean arg3, boolean arg4) {
            setText(MailResourceLoader.getString("dialog", "filter",
                    (String) arg1));

            return this;
        }
    }
}
