/*
 * Created on 29.03.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.core.action;

import javax.swing.JCheckBoxMenuItem;

/**
 * @author frd
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public interface CheckBoxActionInterface {
	public JCheckBoxMenuItem getCheckBoxMenuItem();
	public void setCheckBoxMenuItem(JCheckBoxMenuItem checkBoxMenuItem);
	public boolean getState();
	public void setState(boolean value);
	public boolean getInitState();
}
