/*
 * Created on 06.08.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.core.gui.plugin;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class PluginNode extends DefaultMutableTreeNode {

	String id;
	String version;
	String tooltip;
	
	boolean category;
	
	boolean enabled;

	public PluginNode()
	{
	}
	
	/**
	 * @param arg0
	 */
	public PluginNode(Object arg0) {
		super(arg0);
		
		category = false;
		
	}


	/**
	 * @return
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * @param string
	 */
	public void setId(String string) {
		id = string;
	}

	/**
	 * @param b
	 */
	public void setEnabled(boolean b) {
		enabled = b;
	}

	/**
	 * @return
	 */
	public String getTooltip() {
		return tooltip;
	}

	/**
	 * @param string
	 */
	public void setTooltip(String string) {
		tooltip = string;
	}

	/**
	 * @return
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param string
	 */
	public void setVersion(String string) {
		version = string;
	}

	/**
	 * @return
	 */
	public boolean isCategory() {
		return category;
	}

	/**
	 * @param b
	 */
	public void setCategory(boolean b) {
		category = b;
	}

}
