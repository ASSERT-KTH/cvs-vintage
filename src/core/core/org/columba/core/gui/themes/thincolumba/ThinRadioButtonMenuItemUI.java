package org.columba.core.gui.themes.thincolumba;

import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicRadioButtonMenuItemUI;

import org.columba.core.gui.util.EmptyIcon;
import org.columba.core.gui.util.ImageLoader;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ThinRadioButtonMenuItemUI extends BasicRadioButtonMenuItemUI {

	public static ComponentUI createUI(JComponent c) {
		return new ThinRadioButtonMenuItemUI();
	}

	public void paint(Graphics g, JComponent c) {
		JRadioButtonMenuItem item = (JRadioButtonMenuItem) c;
		
		ThinUtilities.enableAntiAliasing(g);
		
		
		if ( item.isSelected() )
		{
			item.setIcon( ImageLoader.getSmallImageIcon("menucheckedbox.png") );
		}
		else
		{
			item.setIcon( new EmptyIcon() );
		}
		super.paint(g, c);
	}
}
