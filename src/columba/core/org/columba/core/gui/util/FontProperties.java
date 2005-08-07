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
package org.columba.core.gui.util;

import java.awt.Font;
import java.util.Observable;
import java.util.Observer;

import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import org.columba.core.config.Config;
import org.columba.core.xml.XmlElement;


/**
 *
 *
 * Provides font configuration and helper methods to set the fonts
 * application-wide.
 * <p>
 * text-font: this is the font used in the message-viewer and the composer
 * <p>
 * main-font: this is the application-wide font used for every gui element.
 * <p>
 * Generally Look and Feels set this. If the user wants to overwrite the Look
 * and Feel font settings he/she has to change options.xml:/options/gui/fonts
 * attribute: overwrite (true/false)
 * <p>
 * default is of course "false", to respect Look and Feel settings
 *
 * @author fdietz
 */
public class FontProperties extends Observable implements Observer {
    private static XmlElement fonts;

    /**
     *
     */
    public FontProperties() {
        XmlElement options = Config.getInstance().get("options").getElement("/options");
        XmlElement gui = options.getElement("gui");
        fonts = gui.getElement("fonts");

        if (fonts == null) {
            fonts = gui.addSubElement("fonts");
        }

        XmlElement mainFontElement = fonts.getElement("main");

        if (mainFontElement == null) {
            mainFontElement = fonts.addSubElement("main");
        }

        XmlElement textFontElement = fonts.getElement("text");

        if (textFontElement == null) {
            textFontElement = fonts.addSubElement("text");
        }

        // register as configuration change listener
        fonts.addObserver(this);
    }

    /**
     * Gets the currently selected text font used in the message-viewer and
     * composer editor.
     *
     * @return text font
     */
    public static Font getTextFont() {
        return getFont("text");
    }

    /**
     * Gets the currenlty selected widget font.
     *
     * @return widget font
     */
    public static Font getMainFont() {
        return getFont("main");
    }

    /**
     * Gets the currently configured font
     *
     * @param id
     *            can be of value "text" or "main"
     * @return currently selected font
     */
    protected static Font getFont(String id) {
        XmlElement textFontElement = fonts.getElement(id);

        if (textFontElement == null) {
            textFontElement = fonts.addSubElement(id);
        }

        boolean overwrite = Boolean.valueOf(fonts.getAttribute("overwrite",
                    "true")).booleanValue();

        Font font = null;
        String name = null;
        String size = null;

        if (!overwrite) {
            name = "Default";
            size = "12";

            font = new Font(name, Font.PLAIN, Integer.parseInt(size));
        } else {
            name = textFontElement.getAttribute("name", "Default");
            size = textFontElement.getAttribute("size", "12");

            font = new Font(name, Font.PLAIN, Integer.parseInt(size));
        }

        return font;
    }

    /**
     *
     * overwrite Look and Feel font settings
     *
     * @param item
     *            font configuration item
     */
    public static void setFont() {
        // should we really overwrite the Look and Feel font settings
        boolean overwrite = Boolean.valueOf(fonts.getAttribute("overwrite",
                    "true")).booleanValue();

        if (!overwrite) {
            return;
        }

        FontUIResource mainFont = new FontUIResource(getFont("main"));

        // patch submitted by forum user Turbo Chen
        // FIXED: user wasn't able to enter chinese text in Composer Subject textfield

        /*
        UIManager.put("Label.font", mainFont);
        UIManager.put("Textfield.font", mainFont);
        UIManager.put("TextArea.font", mainFont);
        UIManager.put("MenuItem.font", mainFont);
        UIManager.put("MenuItem.acceleratorFont", mainFont);
        UIManager.put("Menu.font", mainFont);
        UIManager.put("Menu.acceleratorFont", mainFont);
        UIManager.put("MenuBar.font", mainFont);
        UIManager.put("Tree.font", mainFont);
        UIManager.put("Table.font", mainFont);
        UIManager.put("Button.font", mainFont);
        UIManager.put("CheckBoxButton.font", mainFont);
        UIManager.put("RadioButton.font", mainFont);
        UIManager.put("ComboBox.font", mainFont);
        UIManager.put("ToggleButton.font", mainFont);
        UIManager.put("CheckBoxMenuItem.font", mainFont);
        UIManager.put("RadioButtonMenuItem.font", mainFont);
        UIManager.put("TabbedPane.font", mainFont);
        UIManager.put("List.font", mainFont);
        */
        java.util.Enumeration keys = UIManager.getDefaults().keys();

        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);

            if (value instanceof javax.swing.plaf.FontUIResource) {
                UIManager.put(key, mainFont);
            }
        }
    }

    /**
     * Gets fired if configuration changes.
     *
     * @see org.colulmba.core.gui.config.GeneralOptionsDialog
     *
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    public void update(Observable arg0, Object arg1) {
    }
}
