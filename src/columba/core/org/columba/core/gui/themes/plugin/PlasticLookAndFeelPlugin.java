/*
 * Created on 18.07.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.core.gui.themes.plugin;

import java.awt.Dimension;

import javax.swing.UIManager;

import com.jgoodies.clearlook.ClearLookManager;
import com.jgoodies.clearlook.ClearLookMode;
import com.jgoodies.plaf.FontSizeHints;
import com.jgoodies.plaf.LookUtils;
import com.jgoodies.plaf.Options;
import com.jgoodies.plaf.plastic.PlasticLookAndFeel;
import com.jgoodies.plaf.plastic.PlasticTheme;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class PlasticLookAndFeelPlugin extends AbstractThemePlugin {

	/**
	 * 
	 */
	public PlasticLookAndFeelPlugin() {
		super();

	}

	/* (non-Javadoc)
	 * @see org.columba.core.gui.themes.plugin.AbstractThemePlugin#setLookAndFeel()
	 */
	public void setLookAndFeel() throws Exception {
		UIManager.put(Options.USE_SYSTEM_FONTS_APP_KEY, Boolean.TRUE);
		Options.setGlobalFontSizeHints(FontSizeHints.MIXED);
		Options.setDefaultIconSize(new Dimension(16, 16));

		ClearLookManager.setMode(ClearLookMode.ON);
		ClearLookManager.setPolicy("com.jgoodies.clearlook.DefaultClearLookPolicy");
		// use this when using the cross-platform version of 
		// jGoodies, which contains also a windows-xp like theme
		/*
		String lafName =
			LookUtils.isWindowsXP()
				? Options.getCrossPlatformLookAndFeelClassName()
				: Options.getSystemLookAndFeelClassName();
		;
		*/

		try {
			//UIManager.setLookAndFeel(lafName);
			PlasticTheme theme = PlasticLookAndFeel.createMyDefaultTheme();
			LookUtils.setLookAndTheme(new PlasticLookAndFeel(), theme);
		} catch (Exception e) {
			System.err.println("Can't set look & feel:" + e);
		};

	}

}
