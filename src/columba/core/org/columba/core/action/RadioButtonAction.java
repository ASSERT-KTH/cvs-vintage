/*
 * Created on 07.09.2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.columba.core.action;

import javax.swing.JRadioButtonMenuItem;

import org.columba.core.gui.frame.AbstractFrameController;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class RadioButtonAction extends FrameAction {

	JRadioButtonMenuItem menuItem;
	
	public RadioButtonAction(
		AbstractFrameController frameController,
		String name) {
		super(frameController, name);
	}

	/**
	 * @return
	 */
	public JRadioButtonMenuItem getMenuItem() {
		return menuItem;
	}

	/**
	 * @param menuItem
	 */
	public void setMenuItem(JRadioButtonMenuItem menuItem) {
		this.menuItem = menuItem;
	}

}
