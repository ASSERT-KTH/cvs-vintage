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

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

/**
 * Every action should subclass BasicAction.
 * 
 * It provides much more attributes than JAbstractAction,
 * including:
 * - i18n tooltip
 * - i18n short description (tooltip!)
 * - toolbar name (this is shorter to save visual space in the toolbar)
 * - an additional large icon used in the toolbar
 * - JavaHelp topic ID
 * <p>
 * Note: Most constructors of this class are depreceated.
 * 
 * The preferred way should be to use methods instead to add
 * additional information to the action.
 * 
 * In property files this value ends with "tooltip".
 * <p>
 * Note: There exist two descriptions: a LONG_DESCRIPTION and
 * a SHORT_DESCRIPTION.
 * 
 * We only use the SMALL_DESCRIPTION as the tooltip text and the 
 * display text of the statusbar, when hovering over the menuitem
 * associated with this action. This also means much less work 
 * for translator who didn't know how to handle this description
 * anyway.
 * 
 * There is no LONG_DESCRIPTION anymore!! 
 *
 * Example: @see org.columba.core.gui.action.CancelAction
 *
 * @author fdietz
 */
public class BasicAction extends JAbstractAction {

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
		return (KeyStroke) getValue(Action.ACCELERATOR_KEY);
	}
	
	/**
	 * Set accelerator key.
	 * 
	 * @param k		new shortcut
	 */
	public void setAcceleratorKey( KeyStroke k)
	{
		putValue(Action.ACCELERATOR_KEY, k);
	}

	/**
	 * Return long text description used in JToolTip.
	 * 
	 * @return		long description
	 */
	public String getTooltipText() {
		return (String) getValue(Action.SHORT_DESCRIPTION);
	}
	
	/**
	 * Set long description, which is used in tooltips, etc.
	 * 
	 * 
	 * @param s		long description
	 */
	public void setTooltipText( String s)
	{
		putValue( Action.SHORT_DESCRIPTION, s );
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
	public void setToolBarName( String s) 
	{
		TOOLBAR_NAME = s;
	}

	/**
	 * Return small icon, used by menuitems.
	 * 
	 * @return		small icon
	 */
	public ImageIcon getSmallIcon() {
		return (ImageIcon) getValue(Action.SMALL_ICON);
	}
	
	/**
	 * Set small icon, used by the menuitems
	 * 
	 * @param i		new small icon
	 */
	public void setSmallIcon( ImageIcon i)
	{
		putValue(Action.SMALL_ICON, i);
	}
	
	/**
	 * Set new mnemonic for this action.
	 * 
	 * @param mnemonic	new mnemonic
	 */
	public void setMnemonic( int mnemonic)
	{
		putValue(Action.MNEMONIC_KEY, new Integer(mnemonic));
	}
	
	/**
	 * Return mnemonic. Used in the menuitem to underline a 
	 * specific character as a shortcut.
	 * 
	 * @return	mnemonic
	 */
	public int getMnemonic()
	{
		return ((Integer)getValue(Action.MNEMONIC_KEY)).intValue();
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
		putValue(Action.NAME, name);
		putValue(Action.LONG_DESCRIPTION, longDescription);
		putValue(Action.SMALL_ICON, small_icon);
		LARGE_ICON = big_icon;

		putValue(Action.SHORT_DESCRIPTION, longDescription);
		putValue(Action.SHORT_DESCRIPTION, tooltip);
		putValue(Action.ACTION_COMMAND_KEY, actionCommand);
		putValue(Action.ACCELERATOR_KEY, keyStroke);
		putValue(Action.MNEMONIC_KEY, new Integer(mnemonic));

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

}