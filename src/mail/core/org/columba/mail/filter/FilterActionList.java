package org.columba.mail.filter;

import org.columba.core.config.DefaultItem;
import org.columba.core.xml.XmlElement;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class FilterActionList extends DefaultItem {

	public FilterActionList( XmlElement root )
	{
		super(root);
	}
	
	public FilterAction get( int index )
	{
		return new FilterAction(getRoot().getElement(index));
	}
	
	public void remove( int index )
	{
		getRoot().removeElement(index);
	}
	
	public void addEmptyAction()
	{
		XmlElement action = new XmlElement("action");
		//action.addAttribute("class", "org.columba.mail.filter.action.MarkMessageAsReadFilterAction");
		action.addAttribute("type","Mark as Read");
		
		getRoot().addElement(action);
	}
	
	
}
