// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

package org.columba.core.gui.util;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.metal.MetalLookAndFeel;

import org.columba.core.config.Config;
import org.columba.core.config.ThemeItem;
import org.columba.core.gui.themes.thincolumba.ThinColumbaTheme;
import org.columba.main.MainInterface;

public class ThemeSwitcher {
	public ThemeSwitcher() {
	}

	public static void setTheme() {
		ThemeItem item = Config.getOptionsConfig().getThemeItem();
		try {
			switch (item.getTheme()) {
				case 0 :
					{
						break;
					}
				case 1 :
					{

						UIManager.setLookAndFeel(
							UIManager.getCrossPlatformLookAndFeelClassName());

						break;
					}
				case 2 :
					{
						
						
						MainInterface.lookAndFeel = new MetalLookAndFeel();
						MetalLookAndFeel.setCurrentTheme(new ThinColumbaTheme(Config.getOptionsConfig().getThemeItem())
							);
						UIManager.setLookAndFeel(MainInterface.lookAndFeel);
						
						


						break;
					}
				/*
				case 3 :
					{
						mainInterface.columbaTheme =
							new ContrastColumbaTheme(mainInterface.themeItem);
						mainInterface.lookAndFeel = new MetalLookAndFeel();
						mainInterface.lookAndFeel.setCurrentTheme(
							mainInterface.columbaTheme);
						UIManager.setLookAndFeel(mainInterface.lookAndFeel);

						break;
					}
				*/
				case 3 :
					{
					
						
						try {
							UIManager.setLookAndFeel(
								 "com.sun.java.swing.plaf.windows.WindowsLookAndFeel" );
						} catch (Exception e) {
							//e.printStackTrace();
							UIManager.setLookAndFeel(
							UIManager.getCrossPlatformLookAndFeelClassName());
						}
						
						break;
					}
				case 4 :
					{
					
						
						try {
							UIManager.setLookAndFeel(
								  "javax.swing.plaf.mac.MacLookAndFeel"  );
						} catch (Exception e) {
							//e.printStackTrace();
							UIManager.setLookAndFeel(
							UIManager.getCrossPlatformLookAndFeelClassName());
						}
						
						break;
					}
				case 5 :
					{
					
						
						try {
							UIManager.setLookAndFeel(
								   "com.sun.java.swing.plaf.motif.MotifLookAndFeel"   );
						} catch (Exception e) {
							//e.printStackTrace();
							UIManager.setLookAndFeel(
							UIManager.getCrossPlatformLookAndFeelClassName());
						}
						break;
					}
			}
		} catch (Exception ex) {
			System.out.println(
				"failure while trying to load theme: " + ex.getMessage());
		}

		setFonts(item);

	}

	public static void updateFrame(JFrame frame) {
		SwingUtilities.updateComponentTreeUI(frame);
	}

	public static void setFonts(ThemeItem item) {
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