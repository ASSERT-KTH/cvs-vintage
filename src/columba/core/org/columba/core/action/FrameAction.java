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
//All Rights Reserved.

package org.columba.core.action;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.ImageIcon;

import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.plugin.PluginInterface;

/**
 * FrameAction has an additional reference to its frame
 * controller.
 * <p>
 * This is necessary because actions have to know in which
 * frame they are, to provide visual feedback for the user
 * in the correct frame.
 * 
 * <p>
 * Note: Most constructors of this class are depreceated.
 * 
 * The preferred way should be to use methods instead to add
 * additional information to the action.
 * 
 * Example: @see org.columba.core.gui.action.CancelAction
 *
 * @author fdietz
 */
public abstract class FrameAction extends AbstractAction implements PluginInterface {

	/**
	 * special label for toolbar buttons which is smaller
	 * than the regular label
	 * 
	 * Example: Reply to Sender -> Reply
	 * 
	 */
	public static final String TOOLBAR_NAME = "ToolbarName";

	/**
	 * The toolbar uses the large icon, whereas menuitems 
	 * use the small one.
	 * 
	 */
	public static final String LARGE_ICON = "LargeIcon";

	/**
	 * JavaHelp topic ID
	 */
	public static final String TOPIC_ID = "TopicID";
	
	/**
	 * show button text in toolbar
	 */
	protected boolean showToolbarText = true;

	protected FrameMediator frameMediator;

	/**
	 * 
	 * default constructor 
	 * 
	 * @param frameMediator		frame controller 
	 * @param name					i18n name
	 * 
	 */
	public FrameAction(FrameMediator frameController, String name) {
		super(name);
		this.frameMediator = frameController;
	}

	/**
	 * Returns the frame controller
	 * 
	 * @return FrameController
	 */
	public FrameMediator getFrameMediator() {
		return frameMediator;
	}

	/**
	 * Sets the frameMediator.
	 * 
	 * @param frameMediator 
	 */
	public void setFrameMediator(FrameMediator frameController) {
		this.frameMediator = frameController;
	}

        /**
         * NOTE: This method is a temporary means to enable compilation -
         * it will soon be removed. BasicAction subclasses should directly
         * communicate with the underlying Hashtable instead of using there
         * accessor methods.
         */
        public String getName() {
                return (String)getValue(NAME);
        }
        
        /**
         * NOTE: This method is a temporary means to enable compilation -
         * it will soon be removed. BasicAction subclasses should directly
         * communicate with the underlying Hashtable instead of using there
         * accessor methods.
         */
        public void setName(String name) {
                putValue(NAME, name);
        }
        
	/**
	 * Return true if toolbar text should be visible
	 * 
	 * @return boolean	true, if toolbar text should be enabled, false otherwise
	 * 
	 */
	public boolean isShowToolBarText() {
		return showToolbarText;
	}

	/**
	 * @param showToolbarText
	 */
	public void setShowToolBarText(boolean showToolbarText) {
		this.showToolbarText = showToolbarText;
	}

	/**
	 * Return accelerator key. NOTE: This method will soon be removed.
         * BasicAction subclasses should directly communicate with the underlying
         * Hashtable instead of using there accessor methods.
	 * 
	 * @return		accelerator key of action
	 */
	public KeyStroke getAcceleratorKey() {
		return (KeyStroke) getValue(ACCELERATOR_KEY);
	}
	
	/**
	 * Set accelerator key. NOTE: This method will soon be removed.
         * BasicAction subclasses should directly communicate with the underlying
         * Hashtable instead of using there accessor methods.
	 * 
	 * @param k		new shortcut
	 */
	public void setAcceleratorKey(KeyStroke k)
	{
		putValue(ACCELERATOR_KEY, k);
	}

	/**
	 * Return long text description used in JToolTip.
	 * 
	 * @return		long description
	 */
	public String getTooltipText() {
		return (String) getValue(SHORT_DESCRIPTION);
	}
	
	/**
	 * Sets the tooltip for this action.
	 * The method removes all <code>&</code> characters from the tooltip
	 * since the <code>&</code> marks an mnemonic character.
	 * 
	 * @param s		the new tooltip, can be null.
	 */
	public void setTooltipText(String s)
	{
		String tooltip = s;
		if ( tooltip != null ) {
			tooltip = tooltip.replaceAll("&", "");
		}
		putValue(SHORT_DESCRIPTION, tooltip);
	}

	/**
	 * Return large icon, used in toolbar. NOTE: This method will soon be removed.
         * BasicAction subclasses should directly communicate with the underlying
         * Hashtable instead of using there accessor methods.
	 * 
	 * @return		large icon
	 */
	public ImageIcon getLargeIcon() {
		return (ImageIcon)getValue(LARGE_ICON);
	}
	
	/**
	 * Set large image icon, used in toolbars. NOTE: This method will soon be removed.
         * BasicAction subclasses should directly communicate with the underlying
         * Hashtable instead of using there accessor methods.
	 * 
	 * @param i		image icon
	 */
	public void setLargeIcon(ImageIcon i)
	{
		putValue(LARGE_ICON, i);
	}

	/**
	 * Return toolbar button name, which is usually shorter
	 * than the regular action label. NOTE: This method will soon be removed.
         * BasicAction subclasses should directly communicate with the underlying
         * Hashtable instead of using there accessor methods.
	 * 
	 * @return		toolbar name
	 */
	public String getToolBarName() {
		return (String)getValue(TOOLBAR_NAME);
	}
	
	/**
	 * Set toolbar name, which is usually shorter than the regular
	 * action label. NOTE: This method will soon be removed.
         * BasicAction subclasses should directly communicate with the underlying
         * Hashtable instead of using there accessor methods.
	 * 
	 * @param s		new toolbar name
	 */
	public void setToolBarName(String s) 
	{
		putValue(TOOLBAR_NAME, s);
	}

	/**
	 * Return small icon, used by menuitems. NOTE: This method will soon be removed.
         * BasicAction subclasses should directly communicate with the underlying
         * Hashtable instead of using there accessor methods.
	 * 
	 * @return		small icon
	 */
	public ImageIcon getSmallIcon() {
		return (ImageIcon) getValue(SMALL_ICON);
	}
	
	/**
	 * Set small icon, used by the menuitems. NOTE: This method will soon be removed.
         * BasicAction subclasses should directly communicate with the underlying
         * Hashtable instead of using there accessor methods.
	 * 
	 * @param i		new small icon
	 */
	public void setSmallIcon(ImageIcon i)
	{
		putValue(SMALL_ICON, i);
	}
	
	/**
	 * Set new mnemonic for this action. NOTE: This method will soon be removed.
         * BasicAction subclasses should directly communicate with the underlying
         * Hashtable instead of using there accessor methods.
	 * 
	 * @param mnemonic	new mnemonic
	 */
	public void setMnemonic(int mnemonic)
	{
		putValue(MNEMONIC_KEY, new Integer(mnemonic));
	}
	
	/**
	 * Return mnemonic. Used in the menuitem to underline a 
	 * specific character as a shortcut. NOTE: This method will soon be removed.
         * BasicAction subclasses should directly communicate with the underlying
         * Hashtable instead of using there accessor methods.
	 * 
	 * @return	mnemonic
	 */
	public int getMnemonic()
	{
		return ((Integer)getValue(MNEMONIC_KEY)).intValue();
	}

	/**
	 * Return JavaHelp topic ID. NOTE: This method will soon be removed.
         * BasicAction subclasses should directly communicate with the underlying
         * Hashtable instead of using there accessor methods.
	 * 
	 * @return		topic ID
	 */
	public String getTopicID() {
		return (String)getValue(TOPIC_ID);
	}

	/**
	 * Set JavaHelp topic ID. NOTE: This method will soon be removed.
         * BasicAction subclasses should directly communicate with the underlying
         * Hashtable instead of using there accessor methods.
	 * 
	 * @param string	new topic ID
	 */
	public void setTopicID(String string) {
		putValue(TOPIC_ID, string);
	}
}
