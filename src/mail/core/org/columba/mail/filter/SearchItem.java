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
import org.columba.mail.folder.virtual.VirtualFolder;

public class SearchItem extends DefaultItem {
	/*
	private AdapterNode vFolderNode;
	private VirtualFolder vFolder;
	private Filter filter;
	private AdapterNode searchNode;
	*/
	
	public SearchItem(XmlElement root) {
		super(root);
		/*
		this.vFolderNode = node;
		this.vFolder = vFolder;

		vFolder.setSearchFilter(this);

		if (node != null)
			parseNode();
		*/
	}

	/*
	public VirtualFolder getFolder() {
		return vFolder;
	}

	protected void parseNode() {
		searchNode = vFolderNode.getChild("search");
		AdapterNode filterNode = searchNode.getChild("filter");

		filter = new Filter(filterNode);

	}

	public Filter getFilter() {
		return filter;
	}

	public int getUid() {
		AdapterNode uidNode = searchNode.getChild("uid");
		String uidStr = getTextValue(uidNode);
		Integer iStr = new Integer(uidStr);
		int uid = iStr.intValue();

		return uid;
	}

	public void setUid(int i) {
		Integer uid = new Integer(i);

		AdapterNode uidNode = searchNode.getChild("uid");

		setTextValue(uidNode, uid.toString());

	}

	public void setInclude(String s) {
		AdapterNode includeNode = searchNode.getChild("include");
		setTextValue(includeNode, s);
	}

	public boolean isInclude() {
		AdapterNode includeNode = searchNode.getChild("include");
		String include = getTextValue(includeNode);

		if (include.equals("true"))
			return true;
		else
			return false;
	}
	*/
	

	public void addSearchToHistory(VirtualFolder folder) {
		if (folder.getUid() == 106)
			addSearchToHistory();
	}

	
	public void addSearchToHistory() {

		/*
		//System.out.println("selectedfolder:"+ MainInterface.treeViewer.getSelected().getName());
		VirtualFolder folder =
			(VirtualFolder) MainInterface.treeModel.getFolder(106);
		
		if (folder.getChildCount() >= 10)
		{
			Folder child = (Folder) folder.getChildAt(0);
			child.removeFromParent();
		}
		
		String name = "search result";
		VirtualFolder vFolder2 =
			(VirtualFolder) MainInterface.treeModel.addVirtualFolder(folder, name);
		Search s = vFolder2.getSearchFilter();
		s.setUid(getUid());
		s.setInclude((new Boolean(isInclude())).toString());
		s.getFilter().getFilterRule().removeAll();
		s.getFilter().getFilterRule().setCondition(
			getFilter().getFilterRule().getCondition());
		for (int i = 0; i < getFilter().getFilterRule().count(); i++)
		{
			FilterCriteria c = getFilter().getFilterRule().getCriteria(i);
			s.getFilter().getFilterRule().addEmptyCriteria();
			FilterCriteria newc = s.getFilter().getFilterRule().getCriteria(i);
			newc.setCriteria(c.getCriteria());
			newc.setHeaderItem(c.getHeaderItem());
			newc.setPattern(c.getPattern());
			newc.setType(c.getType());
		
			if (i == 0)
			{
				// lets find a good name for our new vfolder
		
				StringBuffer buf = new StringBuffer();
		
				if (newc.getType().equalsIgnoreCase("flags"))
				{
					System.out.println("flags found");
					
					buf.append(newc.getType());
					buf.append(" (");
					buf.append(newc.getCriteria());
					buf.append(" ");
					buf.append(newc.getPattern());
					buf.append(")");
				}
				else if (newc.getType().equalsIgnoreCase("custom headerfield"))
				{
		
					buf.append(newc.getHeaderItem());
					buf.append(" (");
					buf.append(newc.getCriteria());
					buf.append(" ");
					buf.append(newc.getPattern());
					buf.append(")");
				}
				else
				{
					buf.append(newc.getType());
					buf.append(" (");
					buf.append(newc.getCriteria());
					buf.append(" ");
					buf.append(newc.getPattern());
					buf.append(")");
		
				}
				System.out.println("newname:" + buf);
		
				vFolder2.setName(buf.toString());
				TreeNodeEvent updateEvent2 = new TreeNodeEvent(vFolder2, TreeNodeEvent.UPDATE);
				MainInterface.crossbar.fireTreeNodeChanged(updateEvent2);
			}
		
		}
		*/

	}

}