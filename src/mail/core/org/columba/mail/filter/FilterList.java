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
//All Rights Reserved.undation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

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
		rules.addAttribute("condition", "matchall");
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

	/**
	 * Adds the filter to this list.
	 * @param f the filter.
	 */
	public void add(Filter f) {
		if ( f != null ) {
			getRoot().addElement(f.getRoot());
		}
		
		//list.add(f);
	}
	
	/**
	 * Remove the <code>Filter</code> from the list.
	 * @param f the filter to remove.
	 */
	public void remove(Filter f) {
		if ( f != null ) {
			getRoot().getElements().remove(f.getRoot());
		}
	}
	
	/**
	 * Inserts the filter into the specified index in the list.
	 * @param filter filter to add.
	 * @param index the index where to insert the filter. 
	 */
	public void insert(Filter filter, int index) {
		if (filter != null) {
			getRoot().insertElement(filter.getRoot(), index);
		}
	}

	/**
	 * Moves the specified filter up in the list.
	 * @param filter the filter to move up.
	 */
	public void moveUp( Filter filter ) {
		move(indexOf(filter), -1);
	}
	
	/**
	 * Moves the specified filter down in the list.
	 * @param filter the filter to move down.
	 */
	public void moveDown( Filter filter ) {
		move(indexOf(filter), 1);
	}
	
	/**
	 * Moves the specified filter a number of positions in the list.
	 * @param filter the filter to move.
	 * @param nrOfPositions the number of positions to move in the list, can be negative.
	 */
	public void move( Filter filter, int nrOfPositions ) {
		move(indexOf(filter), nrOfPositions);
	}

	/**
	 * Moves the filter at the specified index a number of positions in the list.
	 * @param filterIndex the filters index.
	 * @param nrOfPositions the number of positions to move in the list, can be negative.
	 */
	public void move( int filterIndex, int nrOfPositions ) {
		if ((filterIndex >= 0) && (filterIndex < count())) {
			XmlElement filterXML = getRoot().getElement(filterIndex);			
			int newFilterIndex = filterIndex + nrOfPositions;	
			newFilterIndex = ( newFilterIndex < 0 ? 0 : newFilterIndex );		
			
			getRoot().removeElement(filterIndex);
			if (newFilterIndex > count()) {
				getRoot().addElement(filterXML);
			} else {
				getRoot().insertElement(filterXML, newFilterIndex);
			}			
		}
	}
	
	/**
	 * Returns the index in this list of the first occurrence of the specified 
	 * filter, or -1 if this list does not contain this element.
	 * @param filter filter to search for.
	 * @return the index in this list of the first occurrence of the specified filter, 
	 * or -1 if this list does not contain this element.
	 */
	public int indexOf( Filter filter ) {
		int index = -1;		
		if (filter != null) {
			int childCount = getChildCount();			
			for (int i = 0; (index==-1) && (i<childCount); i++ ) {
				if (getRoot().getElement(i).equals(filter.getRoot())) {
					index = i;
				}
			}
		}
		return index;
	}

	public int count() {

		return getChildCount();

	}
	
	/**
	 * Returns the filter at the specified position in the list.
	 * @param index the index
	 * @return a Filter
	 * @throws IndexOutOfBoundsException if the index is out of range (index
     * 		  &lt; 0 || index &gt;= count()).
	 */
	public Filter get(int index) throws IndexOutOfBoundsException {

		Filter filter = new Filter(getRoot().getElement(index));
		return filter;
	}

	public void remove(int index) {
		getRoot().removeElement(index);

	}

}
