// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

package org.columba.mail.filter;

import org.columba.core.config.DefaultItem;
import org.columba.core.xml.XmlElement;

public class FilterList extends DefaultItem {
	//private Vector list;
	// private Folder folder;

	public FilterList(XmlElement root) {
		super(root);
	}

	/*
	public FilterList( Folder folder )
	{
	    this.folder = folder;
	    folder.setFilterList( this );
	    list = new Vector();
	
	    FolderItem item = folder.getFolderItem();
	    AdapterNode filterListNode = item.getFilterListNode();
	
	    if ( filterListNode != null )
	    {
	        AdapterNode child;
	        for ( int i=0; i< filterListNode.getChildCount(); i++)
	        {
	            child = (AdapterNode) filterListNode.getChild( i );
	            Filter filter = new Filter( child );
	            filter.setFolder( folder );
	            add( filter );
	        }
	    }
	}
	*/

	public void removeAllElements() {
		getRoot().removeAllElements();
		/*
		list.removeAllElements();
		getFilterListNode().removeChildren();
		*/
	}

	/*
	public void clear()
	{
		
	    if ( list.size() > 0 )
	        list.clear();
	}
	*/

	public static Filter createEmptyFilter() {
		XmlElement filter = new XmlElement("filter");
		filter.addAttribute("description", "new filter");
		filter.addAttribute("enabled","true");
		XmlElement rules = new XmlElement("rules");
		rules.addAttribute("condition", "match_all");
		XmlElement criteria = new XmlElement("criteria");
		criteria.addAttribute("type", "Subject");
		criteria.addAttribute("headerfield", "Subject");
		criteria.addAttribute("criteria", "contains");
		criteria.addAttribute("pattern", "pattern");
		rules.addElement(criteria);
		filter.addElement(rules);

		XmlElement actionList = new XmlElement("actionlist");
		XmlElement action = new XmlElement("action");
		/*
		action.addAttribute(
			"class",
			"org.columba.mail.filter.action.MarkMessageAsReadFilterAction");
		*/
		
		action.addAttribute("type","Mark as Read");
		
		actionList.addElement(action);
		filter.addElement(actionList);

		
		
		//XmlElement.printNode(getRoot(),"");
		
		return new Filter(filter);
		/*
		//AdapterNode filterListNode = getFilterListNode();
		
		AdapterNode node = MailConfig.getFolderConfig().addEmptyFilterNode( getFolder().getNode() );
		Filter filter = new Filter( node );
		
		add( filter );
		
		return filter;
		*/

	}

	public void add(Filter f) {
		getRoot().addElement(f.getRoot());
		
		//list.add(f);
	}

	public int count() {

		return getChildCount();

	}

	public Filter get(int index) {

		Filter filter = new Filter(getRoot().getElement(index));

		return filter;
	}

	public void remove(int index) {
		getRoot().removeElement(index);

	}

}
