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
package org.columba.mail.gui.table.util;

import java.util.Enumeration;

import org.columba.mail.gui.table.HeaderTableModel;
import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.Flags;
import org.columba.mail.message.HeaderInterface;
import org.columba.mail.message.HeaderList;

public class TableModelFilteredView extends TableModelPlugin {

	private boolean newFlag = false;
	//private boolean oldFlag = true;
	private boolean answeredFlag = false;
	private boolean flaggedFlag = false;
	private boolean expungedFlag = false;
	private boolean attachmentFlag = false;
	//private String patternItem = new String("subject");
	private String patternString = new String();

	private boolean dataFiltering = false;

	public TableModelFilteredView(HeaderTableModel tableModel) {
		super(tableModel);
	}

	/************** filter view *********************/

	public void setDataFiltering(boolean b) throws Exception {
		dataFiltering = b;
		manipulateModel(TableModelPlugin.STRUCTURE_CHANGE);

		//getHeaderTableModel().nodeStructureChanged( getHeaderTableModel().getRootNode() );
		getHeaderTableModel().update();
	}

	public boolean getDataFiltering() {
		return dataFiltering;
	}

	public void setNewFlag(boolean b) throws Exception {
		newFlag = b;

		manipulateModel(TableModelPlugin.STRUCTURE_CHANGE);
	}

	public boolean getNewFlag() {
		return newFlag;
	}

	/*
	public void setOldFlag(boolean b) throws Exception {
		oldFlag = b;
	
		manipulateModel(TableModelPlugin.STRUCTURE_CHANGE);
	}
	
	public boolean getOldFlag() {
		return oldFlag;
	}
	*/
	public void setAnsweredFlag(boolean b) throws Exception {
		answeredFlag = b;
		manipulateModel(TableModelPlugin.STRUCTURE_CHANGE);
	}

	public boolean getAnsweredFlag() {
		return answeredFlag;
	}

	public void setFlaggedFlag(boolean b) throws Exception {
		flaggedFlag = b;

		manipulateModel(TableModelPlugin.STRUCTURE_CHANGE);
	}

	public boolean getFlaggedFlag() {
		return flaggedFlag;
	}
	public void setExpungedFlag(boolean b) throws Exception {
		expungedFlag = b;

		manipulateModel(TableModelPlugin.STRUCTURE_CHANGE);
	}

	public boolean getExpungedFlag() {
		return expungedFlag;
	}
	public void setAttachmentFlag(boolean b) throws Exception {
		attachmentFlag = b;

		manipulateModel(TableModelPlugin.STRUCTURE_CHANGE);

	}

	public boolean getAttachmentFlag() {
		return attachmentFlag;
	}

	/*
	public void setPatternItem(String s) {
		patternItem = s;
	}
	*/

	public void setPatternString(String s) throws Exception {
		patternString = s;

		manipulateModel(TableModelPlugin.STRUCTURE_CHANGE);

	}

	/*
	public String getPatternItem() {
		return patternItem;
	}
	*/

	public String getPatternString() {
		return patternString;
	}

	protected boolean testString(HeaderInterface header) {
		Object o = header.get("Subject");
		if (o != null) {
			if (o instanceof String) {
				String item = (String) o;
				
				
				String pattern = getPatternString().toLowerCase();

				if (item.indexOf(pattern) != -1)
					return true;

			}
		}

		o = header.get("From");
		if (o != null) {
			if (o instanceof String) {
				
				String item = (String) o;
				
				
				String pattern = getPatternString().toLowerCase();

				if (item.indexOf(pattern) != -1)
					return true;

			}
		}

		return false;
	}

	public boolean addItem(HeaderInterface header) {
		boolean result = true;
		boolean result2 = false;
		//boolean result3 = true;
		boolean flags1 = false;
		boolean flags2 = false;

		Flags flags = header.getFlags();

		if (flags == null) {
			System.out.println("flags is null");
			return false;
		}

		if (getNewFlag()) {
			if (flags.getSeen())
				result = false;

		}
		/*
		if (getOldFlag()) {
			if (flags.getSeen())
				result = true;
		}
		*/

		if (getAnsweredFlag()) {
			if (!flags.getAnswered())
				result = false;
		}
		if (getFlaggedFlag()) {
			if (!flags.getFlagged())
				result = false;
		}
		if (getExpungedFlag()) {
			if (!flags.getDeleted())
				result = false;

		}
		if (getAttachmentFlag()) {

			Boolean attach = (Boolean) header.get("columba.attachment");
			boolean attachment = attach.booleanValue();

			if (!attachment)
				result = false;
		}

		if (!(getPatternString().equals(""))) {
			flags2 = true;
			result2 = testString(header);

		} else
			result2 = true;

		if (result2 == true) {
			if ((result == true)) {

				return true;

			}

		}

		return false;
	}

	public boolean manipulateModel(int mode) throws Exception {
		switch (mode) {
			case TableModelPlugin.STRUCTURE_CHANGE :
				{
					MessageNode rootNode = getHeaderTableModel().getRootNode();
					HeaderList headerList =
						getHeaderTableModel().getHeaderList();

					if (headerList == null)
						return false;
					if (headerList.count() == 0)
						return false;

					if (getDataFiltering() == true) {
						//System.out.println("starting filtering");

						ColumbaHeader header;

						for (Enumeration e = headerList.keys();
							e.hasMoreElements();
							) {
							Object uid = e.nextElement();
							header = (ColumbaHeader) headerList.get(uid);
							
							boolean result = addItem(header);
						
							//ystem.out.println("item: "+i+" - result: "+result);
						
							if (result) {
						
								
								MessageNode childNode =
									new MessageNode(header, uid);
								rootNode.add(childNode);
							}
						}
						/*
						for (int i = 0; i < headerList.count(); i++) {
						
							header = headerList.getHeader(i);
						
							boolean result = addItem(header);
						
							//ystem.out.println("item: "+i+" - result: "+result);
						
							if (result) {
						
								Object uid = headerList.getUid(i);
								MessageNode childNode =
									new MessageNode(header, uid);
								rootNode.add(childNode);
							}
						
						}
						*/
						// System.out.println("finished filtering");

						return true;

					} else {
						// do not filter anything

						// System.out.println("do not filter anything");

						return false;

					}

				}

			case TableModelPlugin.NODES_INSERTED :
				{
					MessageNode node =
						getHeaderTableModel().getSelectedMessageNode();
					HeaderInterface header = node.getHeader();

					if (getDataFiltering() == true) {
						boolean result = addItem(header);

						if (result == true) {
							return true;
						} else
							return false;

					}

					return true;

				}
		}

		return false;
	}

}
