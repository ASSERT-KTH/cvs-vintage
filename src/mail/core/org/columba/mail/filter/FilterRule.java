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

import java.util.Vector;

import org.columba.core.config.AdapterNode;
import org.columba.core.config.DefaultItem;
import org.columba.mail.config.MailConfig;
import org.w3c.dom.Document;

public class FilterRule extends DefaultItem {
	
	// Condition
	public final static int MATCH_ALL = 0;
	public final static int MATCH_ANY = 1;
	
	
	// list of FilterCriteria
	private Vector list;

	private AdapterNode node;

	// condition: match all (AND) = 0, match any (OR) = 1
	private AdapterNode conditionNode;

	public FilterRule(AdapterNode node, Document doc) {
		super(doc);

		list = new Vector();
		this.node = node;

		if (node != null) {
			parseNode();
		} else {
			System.out.println(" node == null ");

		}

	}

	public AdapterNode getRootNode() {
		return node;
	}

	public void addEmptyCriteria() {
		AdapterNode n =
			MailConfig.getFolderConfig().addEmptyFilterCriteria(getRootNode());
		FilterCriteria criteria = new FilterCriteria(n, getDocument());

		list.add(criteria);
	}

	public void remove(int index) {
		if ((index >= 0) && (index < list.size())) {
			list.remove(index);

			int result = -1;

			for (int i = 0; i < getRootNode().getChildCount(); i++) {
				AdapterNode child = (AdapterNode) getRootNode().getChildAt(i);
				String name = child.getName();

				if (name.equals("filtercriteria"))
					result++;

				if (result == index) {
					child.remove();
					break;
				}
			}

			//AdapterNode child = getRootNode().getChildAt(index);

		}
	}

	public void removeAll() {
		for (int i = 0; i < count(); i++) {
			remove(0);
		}
	}

	public void removeLast() {
		int index = list.size() - 1;

		remove(index);
	}

	public FilterCriteria get(int index) {
		return (FilterCriteria) list.get(index);
	}

	public int count() {
		return list.size();
	}

	public void parseNode() {
		AdapterNode child;

		//System.out.println("filter rule:  rule size = "+ node.getChildCount() );

		for (int i = 0; i < node.getChildCount(); i++) {
			child = node.getChild(i);
			//System.out.println("filter rule: child-name: "+ child.getName() );

			if (child.getName().equals("filtercriteria"))
				list.add(new FilterCriteria(child, getDocument()));
			else if (child.getName().equals("condition")) {
				//System.out.println("condition value oops "+ child.getValue() );

				conditionNode = child;
			}

		}
	}

	public String getCondition() {
		if (conditionNode == null) {
			System.out.println(
				"---------------------------> failure: conditionNode == null !");

			return new String("matchany");
		} else
			return getTextValue(conditionNode);
	}

	public void setCondition(String s) {
		setTextValue(conditionNode, s);
	}

	public FilterCriteria getCriteria(int index) {
		return (FilterCriteria) list.get(index);
	}

	public int getConditionInt() {
		//System.out.println("condigtion: "+ condition );

		if (getCondition().equals("matchall"))
			return MATCH_ALL;
		if (getCondition().equals("matchany"))
			return MATCH_ANY;
		return -1;
	}

	
	
}
