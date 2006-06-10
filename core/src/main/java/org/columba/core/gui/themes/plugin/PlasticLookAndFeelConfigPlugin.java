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
package org.columba.core.gui.themes.plugin;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;

import org.columba.core.config.Config;
import org.columba.core.gui.plugin.AbstractConfigPlugin;
import org.columba.core.xml.XmlElement;

import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.PlasticTheme;


/**
 * Asks the user which theme he wants to use.
 *
 * @author fdietz
 */
public class PlasticLookAndFeelConfigPlugin extends AbstractConfigPlugin {
    JList list;
    XmlElement themeElement;

    /**
     *
     */
    public PlasticLookAndFeelConfigPlugin() {
        super();

        XmlElement options = Config.getInstance().get("options").getElement("/options");
        XmlElement gui = options.getElement("gui");
        themeElement = gui.getElement("theme");
    }

    /* (non-Javadoc)
     * @see org.columba.core.gui.plugin.AbstractConfigPlugin#createPanel()
     */
    public JPanel createPanel() {
        list = new JList(computeThemes());
        list.setCellRenderer(createThemeRenderer());

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JScrollPane pane = new JScrollPane(list);
        panel.add(pane, BorderLayout.NORTH);

        return panel;
    }

    protected PlasticTheme[] computeThemes() {
        List themes = PlasticLookAndFeel.getInstalledThemes();

        return (PlasticTheme[]) themes.toArray(new PlasticTheme[themes.size()]);
    }

    protected PlasticTheme getTheme(String name) {
        PlasticTheme[] themes = computeThemes();

        for (int i = 0; i < themes.length; i++) {
            String str = themes[i].getName();

            if (name.equals(str)) {
                return themes[i];
            }
        }

        return null;
    }

    /* (non-Javadoc)
     * @see org.columba.core.gui.plugin.AbstractConfigPlugin#updateComponents(boolean)
     */
    public void updateComponents(boolean b) {
        String theme = themeElement.getAttribute("theme");

        if (b) {
            if (theme != null) {
                PlasticTheme t = getTheme(theme);

                if (t != null) {
                    list.setSelectedValue(t, true);
                }
            }
        } else {
            PlasticTheme t = (PlasticTheme) list.getSelectedValue();

            if (t != null) {
                themeElement.addAttribute("theme", t.getName());
            }
        }
    }

    
	private ListCellRenderer createThemeRenderer() {
        return new DefaultListCellRenderer() {
                public Component getListCellRendererComponent(JList list,
                    Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                    JLabel label = (JLabel) super.getListCellRendererComponent(list,
                            value, index, isSelected, cellHasFocus);
                    PlasticTheme theme = (PlasticTheme) value;
                    label.setText(theme.getName());

                    return label;
                }
            };
    }
}
