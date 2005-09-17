// The contents of this file are subject to the Mozilla Public License Version 1.1
// (the "License"); you may not use this file except in compliance with the 
// License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License 
// for the specific language governing rights and
// limitations under the License.
//
// The Original Code is "The Columba Project"
//
// The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
// Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
// All Rights Reserved.
//$Log: Filter.java,v $
//Revision 1.3  2005/09/17 12:18:18  fdietz
//[bug] filter helper didn't work (Utilities-> create filter on message)
//
//Revision 1.2  2005/01/16 22:52:43  fdietz
//[intern]configurationn cleanup, removed obsolete classes, replaced usage of XmlElement with Item wrapper classes, added constants instead of direct string usage
//
//Revision 1.1  2004/12/23 10:41:49  fdietz
//[feature]added new filter toolbar, using vFolder search capability instead of table-model, moved Ifolder, foldercommand, foldercommandreference, and all filter/filteractions code in core package
//
package org.columba.core.filter;

import org.columba.core.config.DefaultItem;
import org.columba.core.xml.XmlElement;

/**
 * Wrapper for the filter xml configuration, which makes code easier to read in
 * comparison to using the XmlElement stuff.
 * 
 * @author frd
 */

// example configuration (tree.xml):
//
// <filter description="gnome" enabled="true">
// <rules condition="matchany">
// <criteria criteria="contains" headerfield="To or Cc" pattern="gnome" type="To
// or Cc"></criteria>
// </rules>
// <actionlist>
// <action uid="120" type="Move Message"></action>
// </actionlist>
// </filter>
//
public class Filter extends DefaultItem {

	private static final String DESCRIPTION = "description";

	private static final String ENABLED = "enabled";

	private static final String RULES = "rules";

	private static final String ACTIONLIST = "actionlist";

	private static final String FILTER = "filter";

	private Filter() {
		super(new XmlElement(Filter.FILTER));
	}

	/**
	 * 
	 * Constructor for Filter
	 * 
	 * XmlElement should be "filter" in this case
	 * 
	 * @see org.columba.core.config.DefaultItem#DefaultItem(XmlElement)
	 */
	public Filter(XmlElement root) {
		super(root);
	}

	/**
	 * 
	 * @return FilterActionList this is also a simple wrapper
	 */
	public FilterActionList getFilterActionList() {
		XmlElement element = getRoot().getElement(Filter.ACTIONLIST);
		
		return new FilterActionList(element);

	}

	/**
	 * 
	 * 
	 * @return FilterRule this is also a simple wrapper
	 */
	public FilterRule getFilterRule() {
		XmlElement element = getRoot().getElement(Filter.RULES);
		

		return new FilterRule(element);
	}

	/**
	 * Is filter enabled?
	 * 
	 * @return boolean true if enabled
	 */
	public boolean getEnabled() {
		return getBooleanWithDefault(Filter.ENABLED, true);
	}

	/**
	 * 
	 * enable Filter
	 * 
	 * @param bool
	 *            if true enable filter otherwise disable filter
	 */
	public void setEnabled(boolean bool) {
		setBoolean(Filter.ENABLED, bool);
	}

	/**
	 * Set filter name
	 * 
	 * @param s
	 *            new filter name
	 */
	public void setName(String s) {
		setString(Filter.DESCRIPTION, s);
	}

	/**
	 * 
	 * return Name of Filter
	 * 
	 * @return String
	 */
	public String getName() {
		return get(Filter.DESCRIPTION);
	}

	/** {@inheritDoc} */
	public Object clone() {
		return super.clone();
	}

	/** {@inheritDoc} */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Filter[name=");
		sb.append(getName());
		sb.append(", enabled=");
		sb.append(getEnabled());
		sb.append("]");

		return sb.toString();
	}
}