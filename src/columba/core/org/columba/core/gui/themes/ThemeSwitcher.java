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
//All Rights Reserved.Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

package org.columba.core.gui.themes;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import org.columba.core.config.Config;
import org.columba.core.config.GuiItem;
import org.columba.core.gui.themes.plugin.AbstractThemePlugin;
import org.columba.core.main.MainInterface;
import org.columba.core.plugin.ThemePluginHandler;
import org.columba.core.xml.XmlElement;

public class ThemeSwitcher {
	public static void setTheme() {
		// get configuration
		XmlElement themeConfig =
			Config.get("options").getElement("/options/gui/theme");

		try {
			// get plugin-handler
			ThemePluginHandler handler =
				(ThemePluginHandler) MainInterface.pluginManager.getHandler(
					"org.columba.core.theme");
			// if no theme available -> set ThinColumba as default
			String pluginName = themeConfig.getAttribute("name", "ThinColumba");
			if (pluginName == null) {
				themeConfig.addAttribute("name", "ThinColumba");
				pluginName = "ThinColumba";
			}

			AbstractThemePlugin theme = null;

			// instanciate theme
			theme = (AbstractThemePlugin) handler.getPlugin(pluginName, null);

			// apply theme
			theme.setLookAndFeel();

		} catch (Exception ex) {
			ex.printStackTrace();
			
			try {

				// fall-back
				UIManager.setLookAndFeel(
					UIManager.getCrossPlatformLookAndFeelClassName());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		setFonts(Config.getOptionsConfig().getGuiItem());
	}

	public static void updateFrame(JFrame frame) {
		final JFrame f = frame;

		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				SwingUtilities.updateComponentTreeUI(f);
			}
		});

	}

	public static void setFonts(GuiItem item) {
		FontUIResource mainFont = new FontUIResource(item.getMainFont());
		FontUIResource textFont = new FontUIResource(item.getTextFont());

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

		// set messageviewer and composer textcomponent fonts
		//MainInterface.messageViewer.setFont();
	}
}
