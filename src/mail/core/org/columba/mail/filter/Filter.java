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

import org.columba.core.command.CompoundCommand;
import org.columba.core.config.AdapterNode;
import org.columba.core.config.DefaultItem;
import org.columba.core.gui.FrameController;
import org.columba.mail.config.MailConfig;
import org.columba.mail.filter.action.CopyMessageFilterAction;
import org.columba.mail.filter.action.DeleteMessageFilterAction;
import org.columba.mail.filter.action.MarkMessageAsReadFilterAction;
import org.columba.mail.filter.action.MoveMessageFilterAction;
import org.columba.mail.folder.Folder;

public class Filter extends DefaultItem {
	private AdapterNode node;
	private Vector actionList;
	private FilterRule rule;
	private Folder folder;
	private AdapterNode actionListNode;
	private AdapterNode nameNode;
	private AdapterNode enabledNode;

	public Filter(AdapterNode node) {
		super(MailConfig.getFolderConfig().getDocument());
		this.node = node;
		actionList = new Vector();
		//System.out.println("node: "+node);

		if (node != null)
			parseNode();

	}

	public AdapterNode getRootNode() {
		return node;
	}

	public AdapterNode getActionListNode() {
		return actionListNode;
	}

	public void parseNode() {
		AdapterNode child;

		for (int i = 0; i < node.getChildCount(); i++) {
			child = node.getChild(i);

			if (child.getName().equals("actionlist")) {
				actionListNode = child;

				for (int j = 0; j < child.getChildCount(); j++) {
					AdapterNode subChild = child.getChild(j);
					if (subChild.getName().equals("action"))
						actionList.add(
							new FilterAction(subChild, getDocument()));
				}

			} else if (child.getName().equals("filterrule")) {
				rule = new FilterRule(child, getDocument());
			} else if (child.getName().equals("description")) {
				nameNode = child;
			} else if (child.getName().equals("enabled")) {
				enabledNode = child;
			}
		}

	}

	public FilterRule getFilterRule() {
		return rule;
	}

	public void addEmptyAction() {
		AdapterNode n =
			MailConfig.getFolderConfig().addEmptyFilterAction(
				getActionListNode());

		FilterAction action = new FilterAction(n, getDocument());

		actionList.add(action);
	}

	public int getActionCount() {
		return actionList.size();
	}

	public FilterAction getFilterAction(int index) {
		return (FilterAction) actionList.get(index);
	}

	public void removeAction(int index) {
		actionList.remove(index);

		AdapterNode actionNode = getActionListNode().getChildAt(index);
		actionNode.remove();
	}

	public void removeLastAction() {
		int index = actionList.size() - 1;

		removeAction(index);

	}

	public void addEmptyCriteria() {
		rule.addEmptyCriteria();
	}

	public void removeCriteria(int index) {
		rule.remove(index);
	}

	public void removeLastCriteria() {
		rule.removeLast();
	}

	public void setFolder(Folder f) {
		this.folder = f;
	}

	public AdapterNode getNode() {
		return node;
	}

	public Boolean getEnabled() {
		String str = (String) getTextValue(enabledNode);

		Boolean b = new Boolean(str);

		return b;
	}

	public void setEnabled(Boolean bool) {
		setTextValue(enabledNode, bool.toString());
	}

	public void setName(String s) {
		setTextValue(nameNode, s);
	}
	public String getName() {
		return getTextValue(nameNode);
	}

	public CompoundCommand getCommand(
		FrameController frameController,
		Folder srcFolder,
		Object[] uids) throws Exception{
		CompoundCommand c = new CompoundCommand();

		for (int i = 0; i < getActionCount(); i++) {
			FilterAction action = getFilterAction(i);

			switch (action.getActionInt()) {

				case 0 :
					{
						// move

						System.out.println("moving messages");

						c.add(
							new MoveMessageFilterAction(
								frameController,
								action,
								srcFolder,
								uids)
								.getCommand());

						break;
					}
				case 1 :
					{
						// copy
						System.out.println("copying messages");

						//System.out.println("treepath: "+ treePath );

						c.add( new CopyMessageFilterAction(
							frameController,
							action,
							srcFolder,
							uids)
							.getCommand());

						break;
					}
				case 2 :
					{
						System.out.println("mark messages as read");
						c.add(new MarkMessageAsReadFilterAction(
							frameController,
							action,
							srcFolder,
							uids)
							.getCommand());

						break;
					}
				case 3 :
					{
						System.out.println("delete messages");
						c.add(new DeleteMessageFilterAction(
							frameController,
							action,
							srcFolder,
							uids)
							.getCommand());

						break;
					}
			}
		}
		
		return c;
	}
	/*
	public boolean getFilterResult(  Rfc822Header header )
	{
	    return rule.process( header );
	}
	*/

	/*
	public Object[] getFilterResult( Object[] uids ) throws Exception
	{
	    Vector v = rule.process( folder, uids );
	
	    return v.toArray();
	}
	*/

	/*
	public boolean processFilter( Object[] uids ) throws Exception
	{
	
	
	    if ( getEnabled().equals(Boolean.FALSE) ) return true;
	
	    Vector v = rule.process( folder, uids  );
		if ( v.size() == 0 ) return true;
	
	          //System.out.println("rule is true");
	    FilterAction action;
	
	          //System.out.println(" actionlist.size() = "+ actionList.size() );
	    boolean moved = false;
	
	        for ( int i=0; i<actionList.size(); i++)
	        {
	            action = (FilterAction) actionList.get(i);
	
	            if ( action.processAction( folder, v ) )
	                {
	                    moved = true;
	                    break;
	                }
	
	
	        }
	        //return moved;
	
	    return true;
	}
	*/

}
