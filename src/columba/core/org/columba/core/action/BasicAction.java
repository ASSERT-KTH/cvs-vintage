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
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

/**
 * Every action should subclass BasicAction.
 * 
 * It provides much more attributes than JAbstractAction,
 * including:
 * <ul>
 * <li>
 * i18n tooltip
 * </li>
 * <li>
 * i18n short description (tooltip!)
 * </li>
 * <li>
 * toolbar name (this is shorter to save visual space in the toolbar)
 * </li>
 * <li>
 * an additional large icon used in the toolbar
 * </li>
 * <li>
 * JavaHelp topic ID
 * </li>
 * </ul>
 * <p>
 * <b>Note:</b> Most constructors of this class are depreceated.
 * <p>
 * The preferred way should be to use methods instead to add
 * additional information to the action.
 * <p>
 * In property files this value ends with "tooltip".
 * <p>
 * <b>Note:</b> There exist two descriptions: a LONG_DESCRIPTION and
 * a SHORT_DESCRIPTION.
 * <p>
 * We only use the SMALL_DESCRIPTION as the tooltip text and the 
 * display text of the statusbar, when hovering over the menuitem
 * associated with this action. This also means much less work 
 * for translator who didn't know how to handle this description
 * anyway.
 * <p>
 * There is no LONG_DESCRIPTION anymore!! 
 * <p>
 * 
 * @see org.columba.core.gui.action.CancelAction
 *
 * @author fdietz
 */
public abstract class BasicAction extends AbstractAction {

	/**
	 * special label for toolbar buttons which is smaller
	 * than the regular label
	 * 
	 * Example: Reply to Sender -> Reply
	 * 
	 */
	public String TOOLBAR_NAME;

	/**
	 * The toolbar uses the large icon, whereas menuitems 
	 * use the small one.
	 * 
	 */
	public ImageIcon LARGE_ICON;

	/**
	 * show button text in toolbar
	 */
	boolean showToolbarText = true;


	/**
	 * JavaHelp topic ID
	 */
	public String topicID=null;
	
	/**
	 * default constructor
	 */
	public BasicAction() {

	}

	/**
	 * default constructor
	 * 
	 * @param name		i18n name
	 */
	public BasicAction(String name) {
		super(name);
		
		// in case their's no action command specified
		// we just pre-fill the name
                setActionCommand(name);
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
         * NOTE: This method is a temporary means to enable compilation -
         * it will soon be removed. BasicAction subclasses should directly
         * communicate with the underlying Hashtable instead of using there
         * accessor methods.
         */
        public String getActionCommand() {
                return (String)getValue(ACTION_COMMAND_KEY);
        }
        
        /**
         * NOTE: This method is a temporary means to enable compilation -
         * it will soon be removed. BasicAction subclasses should directly
         * communicate with the underlying Hashtable instead of using there
         * accessor methods.
         */
        public void setActionCommand(String actionCommand) {
                putValue(ACTION_COMMAND_KEY, actionCommand);
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
	 * Enable/Disable text in toolbar button.
	 * 
	 * @param b	true/false
	 */
	public void enableToolBarText(boolean b) {
		showToolbarText = b;
	}

	/**
	 * Return accelerator key.
	 * 
	 * @return		accelerator key of action
	 */
	public KeyStroke getAcceleratorKey() {
		return (KeyStroke) getValue(ACCELERATOR_KEY);
	}
	
	/**
	 * Set accelerator key.
	 * 
	 * @param k		new shortcut
	 */
	public void setAcceleratorKey( KeyStroke k)
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
	public void setTooltipText( String s)
	{
		String tooltip = s;
		if ( tooltip != null ) {
			tooltip = tooltip.replaceAll("&", "");
		}
		putValue(SHORT_DESCRIPTION, tooltip);
	}

	/**
	 * Return large icon, used in toolbar
	 * 
	 * @return		large icon
	 */
	public ImageIcon getLargeIcon() {
		return LARGE_ICON;
	}
	
	/**
	 * Set large image icon, used in toolbars
	 * 
	 * @param i		image icon
	 */
	public void setLargeIcon( ImageIcon i )
	{
		LARGE_ICON = i;
	}

	/**
	 * Return toolbar button name, which is usually shorter
	 * than the regular action label.
	 * 
	 * @return		toolbar name
	 */
	public String getToolBarName() {
		return TOOLBAR_NAME;
	}
	
	/**
	 * Set toolbar name, which is usually shorter than the regular
	 * action label.
	 * 
	 * @param s		new toolbar name
	 */
	public void setToolBarText( String s) 
	{
		TOOLBAR_NAME = s;
	}

	/**
	 * Return small icon, used by menuitems.
	 * 
	 * @return		small icon
	 */
	public ImageIcon getSmallIcon() {
		return (ImageIcon) getValue(SMALL_ICON);
	}
	
	/**
	 * Set small icon, used by the menuitems
	 * 
	 * @param i		new small icon
	 */
	public void setSmallIcon( ImageIcon i)
	{
		putValue(SMALL_ICON, i);
	}
	
	/**
	 * Set new mnemonic for this action.
	 * 
	 * @param mnemonic	new mnemonic
	 */
	public void setMnemonic( int mnemonic)
	{
		putValue(MNEMONIC_KEY, new Integer(mnemonic));
	}
	
	/**
	 * Return mnemonic. Used in the menuitem to underline a 
	 * specific character as a shortcut.
	 * 
	 * @return	mnemonic
	 */
	public int getMnemonic()
	{
		return ((Integer)getValue(MNEMONIC_KEY)).intValue();
	}

	/******************** deprecated constructors ********************/

	/**
	 * @deprecated 	use default constructor instead
	 * 
	 * @param name
	 * @param longDescription
	 * @param tooltip
	 * @param actionCommand
	 * @param small_icon
	 * @param big_icon
	 * @param mnemonic
	 * @param keyStroke
	 */
	public BasicAction(
		String name,
		String longDescription,
		String tooltip,
		String actionCommand,
		ImageIcon small_icon,
		ImageIcon big_icon,
		int mnemonic,
		KeyStroke keyStroke) {
		super();
		putValue(NAME, name);
		putValue(LONG_DESCRIPTION, longDescription);
		putValue(SMALL_ICON, small_icon);
		LARGE_ICON = big_icon;

		//putValue(Action.SHORT_DESCRIPTION, longDescription);
		setTooltipText(tooltip);
		putValue(ACTION_COMMAND_KEY, actionCommand);
		putValue(ACCELERATOR_KEY, keyStroke);
		putValue(MNEMONIC_KEY, new Integer(mnemonic));

		TOOLBAR_NAME = name;
	}

	/**
	 * @deprecated use default constructor instead
	 * 
	 * 
	 * @param name
	 * @param longDescription
	 * @param tooltip
	 * @param actionCommand
	 * @param small_icon
	 * @param big_icon
	 * @param mnemonic
	 * @param keyStroke
	 * @param showToolbarText
	 */
	public BasicAction(
		String name,
		String longDescription,
		String tooltip,
		String actionCommand,
		ImageIcon small_icon,
		ImageIcon big_icon,
		int mnemonic,
		KeyStroke keyStroke,
		boolean showToolbarText) {
		this(
			name,
			longDescription,
			tooltip,
			actionCommand,
			small_icon,
			big_icon,
			mnemonic,
			keyStroke);
		this.showToolbarText = showToolbarText;
	}
	
	/**
	 * Return JavaHelp topic ID
	 * 
	 * @return		topic ID
	 */
	public String getTopicID() {
		return topicID;
	}

	/**
	 * Set JavaHelp topic ID
	 * 
	 * @param string	new topic ID
	 */
	public void setTopicID(String string) {
		topicID = string;
	}

	/**
	 * @param showToolbarText
	 */
	public void setShowToolBarText(boolean showToolbarText) {
		this.showToolbarText = showToolbarText;
	}
}
