/*
 * Created on 18.07.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.core.gui.themes.plugin;

import java.awt.Dimension;

import javax.swing.UIManager;

import com.jgoodies.plaf.FontSizeHints;
import com.jgoodies.plaf.LookUtils;
import com.jgoodies.plaf.Options;

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
		Options.setDefaultIconSize(new Dimension(18, 18));

		String lafName =
			LookUtils.isWindowsXP()
				? Options.getCrossPlatformLookAndFeelClassName()
				: Options.getSystemLookAndFeelClassName();
		;

		try {
			UIManager.setLookAndFeel(lafName);
		} catch (Exception e) {
			System.err.println("Can't set look & feel:" + e);
		};

	}

}
