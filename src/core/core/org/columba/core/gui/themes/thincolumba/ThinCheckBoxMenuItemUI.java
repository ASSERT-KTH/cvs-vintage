package org.columba.core.gui.themes.thincolumba;

import java.awt.Graphics;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicCheckBoxMenuItemUI;

import org.columba.core.gui.util.ImageLoader;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ThinCheckBoxMenuItemUI extends BasicCheckBoxMenuItemUI {
	
	
	public static ComponentUI createUI(JComponent c) {
		return new ThinCheckBoxMenuItemUI();
	}

	public void paint(Graphics g, JComponent c) {
		JCheckBoxMenuItem item = (JCheckBoxMenuItem) c ;
		
		ThinUtilities.enableAntiAliasing(g);
		
		if ( item.isSelected() )
		{
			item.setIcon( ImageLoader.getSmallImageIcon("checkedbox.png") );
		}
		else
		{
			item.setIcon( ImageLoader.getSmallImageIcon("checkbox.png") );
		}
		super.paint(g, c);
	}
}
