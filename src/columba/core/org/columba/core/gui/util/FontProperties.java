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
package org.columba.core.gui.util;

import java.awt.Font;

import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import org.columba.core.config.Config;
import org.columba.core.config.GuiItem;
import org.columba.core.xml.XmlElement;

/**
 * @author fdietz
 *
 * Class contains font configuration and helper methods to 
 * set the fonts application-wide
 * 
 * text-font:
 * this is the font used in the message-viewer
 * and the composer
 * 
 * main-font:
 * this is the application-wide font used for every
 * gui element.
 * generall Look and Feels set this. If the user wants
 * to overwrite the Look and Feel font settings he/she 
 * has to change options.xml:/options/gui/mainfont
 * attribute: overwrite (true/false)
 * 
 * default is of course "false", to respect Look and Feel
 * settings
 * 
 */
public class FontProperties {

	protected static GuiItem item;

	/**
	 * 
	 */
	public FontProperties() {

		item = Config.getOptionsConfig().getGuiItem();
	}

	/**
	 * 
	 * overwrite Look and Feel font settings
	 * 
	 * @param item	font configuration item
	 */
	public static void setFont() {
		// should we really overwrite the Look and Feel font settings
		if ( !isOverwriteThemeSettings() ) return;
		
		FontUIResource mainFont = new FontUIResource(getMainFont());

		UIManager.put("Label.font", mainFont);
		UIManager.put("Textfield.font", mainFont);
		UIManager.put("TextArea.font", mainFont);
		UIManager.put("MenuItem.font", mainFont);
		UIManager.put("Menu.font", mainFont);
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

		
	}

	public static Font getTextFont() {
		return item.getTextFont();
	}

	public static Font getMainFont() {
		return item.getMainFont();
	}
	
	/**
	 * 
	 * 
	 * @return	true if user wants to overwrite Look and Feel font settings,
	 * 		    else otherwise
	 */
	public static boolean isOverwriteThemeSettings()
	{
		XmlElement mainfont = item.getElement("mainfont");
		String overwrite = mainfont.getAttribute("overwrite");
		
		// create attribute if not available
		if ( overwrite == null )
		{
			 mainfont.addAttribute("overwrite","false");
			 overwrite = "false"; 
		} 
		
		if ( overwrite.equals("true")) return true;
		
		return false;
	}

}
