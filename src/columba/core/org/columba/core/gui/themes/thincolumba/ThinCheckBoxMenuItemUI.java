package org.columba.core.gui.themes.thincolumba;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicCheckBoxMenuItemUI;

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
public class ThinCheckBoxMenuItemUI extends BasicCheckBoxMenuItemUI {
	public static EmptyIcon emptyIcon = new EmptyIcon();
	
	public static ComponentUI createUI(JComponent c) {
		ComponentUI menu = new ThinCheckBoxMenuItemUI();
		
		return menu;
		
	}
	
	protected Dimension getPreferredMenuItemSize(
		JComponent c,
		Icon checkIcon,
		Icon arrowIcon,
		int defaultTextIconGap) {
		JMenuItem b = (JMenuItem) c;
		Icon icon = (Icon) b.getIcon();

		if (icon == null)
			b.setIcon(new EmptyIcon());
		Dimension d =
			super.getPreferredMenuItemSize(
				c,
				checkIcon,
				arrowIcon,
				defaultTextIconGap);

		
		
		return d;
	}

	public void paint(Graphics g, JComponent c) {
		JCheckBoxMenuItem item = (JCheckBoxMenuItem) c ;
		
		ThinUtilities.enableAntiAliasing(g);
		
		if ( item.isSelected() )
		{
			//item.setIcon( ImageLoader.getSmallImageIcon("checkedbox.png") );
			item.setIcon( ImageLoader.getSmallImageIcon("checkbox.png") );
		}
		else
		{
			item.setIcon( emptyIcon );
		}
		super.paint(g, c);
	}
}
