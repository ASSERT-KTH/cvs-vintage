package org.columba.core.config;

import org.columba.core.xml.XmlElement;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ViewItem extends DefaultItem {

	public ViewItem( XmlElement root )
	{
		super(root);
	}
	
	public WindowItem getWindowItem()
	{
		return new WindowItem(getRoot().getElement("window"));
	}
	
}
