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
package org.columba.mail.config;

import java.io.File;

import org.columba.core.config.DefaultXmlConfig;

public class FolderXmlConfig extends DefaultXmlConfig {
	//private File file;

	public FolderXmlConfig(File file) {
		super(file);
	}

/*
	// create uid list from all accounts
	protected void getUids(Vector v, AdapterNode parent) {

		int childCount = parent.getChildCount();

		if (childCount > 0) {
			for (int i = 0; i < childCount; i++) {

				AdapterNode child = parent.getChild(i);

				getUids(v, child);

				//System.out.println("name: "+ child.getName() );

				if (child.getName().equals("folder")) {
					AdapterNode uidNode = child.getChild("uid");

					Integer j = new Integer(uidNode.getValue());

					v.add(j);
				}

			}
		}
	}
	*/
	
	/*
	// find a free uid for a new account
	protected String createUid() {
		Vector v = new Vector();

		AdapterNode rootNode = new AdapterNode(getDocument());
		AdapterNode treeNode = rootNode.getChild(0);

		getUids(v, treeNode);

		int result = -1;
		boolean hit;
		boolean exit = false;

		while (exit == false) {
			hit = false;
			result++;
			for (int i = 0; i < v.size(); i++) {
				Integer j = (Integer) v.get(i);

				if (j.intValue() == result) {
					hit = true;
				}
			}
			if (hit == false)
				exit = true;
		}

		Integer newUid = new Integer(result);

		return newUid.toString();
	}
	*/
	
	/*
	public AdapterNode addVirtualFolderNode( AdapterNode parentNode,
	                                         int uid,
	                                         String name,
	                                         String headerItem,
	                                         String pattern )
	{
	    Element parentElement = createElementNode("folder");
	
	    Element childElement = createTextElementNode("name",name);
	    addElement( parentElement, childElement );
	    childElement = createTextElementNode("accessrights","user");
	    addElement( parentElement, childElement );
	    childElement = createTextElementNode("messagefolder", "true");
	    addElement( parentElement, childElement );
	    childElement = createTextElementNode("subfolder","false");
	    addElement( parentElement, childElement );
	    childElement = createTextElementNode("add", "false");
	    addElement( parentElement, childElement );
	    childElement = createTextElementNode("remove", "true");
	    addElement( parentElement, childElement );
	    childElement = createTextElementNode("uid", createUid() );
	    addElement( parentElement, childElement );
	
	
	    childElement = createTextElementNode("type","virtual");
	        addElement( parentElement, childElement );
	
	        Element searchElement = createElementNode("search");
	
	        childElement = createTextElementNode("include","false");
	
	        addElement( searchElement, childElement );
	
	        Integer newUid = new Integer(uid);
	
	        Element treePathNode = createTextElementNode("uid", newUid.toString() );
	
	
	        addElement( searchElement, treePathNode );
	
	
	
	
	          // create filter
	
	        Element filterParent = createElementNode("filter");
	
	        Element child = createElementNode("filterrule");
	
	        Element subChild = createElementNode( "filtercriteria" );
	
	        Element subNode = createTextElementNode("headeritem", headerItem);
	        addElement( subChild, subNode );
	        subNode = createTextElementNode("criteria", "contains" );
	        addElement( subChild, subNode );
	        subNode = createTextElementNode("pattern", pattern );
	        addElement( subChild, subNode );
	
	        addElement( child, subChild );
	
	        subNode = createTextElementNode("condition", "matchall" );
	        addElement( child, subNode );
	
	        addElement( filterParent, child );
	
	        addElement( searchElement, filterParent );
	
	        addElement( parentElement, searchElement );
	
	
	        AdapterNode childNode=null;
	
	    if ( parentNode == null )
	    {
	        AdapterNode node = getRootNode();
	        childNode =  node.addElement( parentElement );
	
	        System.out.println("parent node == null ");
	        System.out.println("childnode: "+ childNode );
	
	    }
	    else
	        childNode = parentNode.addElement( parentElement );
	
	    return childNode;
	
	
	}
	*/

	/*
	public AdapterNode addFolderNode(
		AdapterNode parentNode,
		String name,
		String access,
		String messagefolder,
		String type,
		String subfolder,
		String add,
		String remove,
		Integer uid) {
		Element parentElement = createElementNode("folder");

		
		Element childElement = createTextElementNode("name", name);
		addElement(parentElement, childElement);
		childElement = createTextElementNode("accessrights", access);
		addElement(parentElement, childElement);
		childElement = createTextElementNode("messagefolder", messagefolder);
		addElement(parentElement, childElement);
		childElement = createTextElementNode("subfolder", subfolder);
		addElement(parentElement, childElement);
		childElement = createTextElementNode("add", add);
		addElement(parentElement, childElement);
		childElement = createTextElementNode("remove", remove);
		addElement(parentElement, childElement);
		childElement = createTextElementNode("uid", createUid());
		addElement(parentElement, childElement);

		if (type.equals("columba")) {
			childElement = createTextElementNode("type", type);
			addElement(parentElement, childElement);
		} else if (type.equals("virtual")) {
			childElement = createTextElementNode("type", type);
			addElement(parentElement, childElement);

			Element searchElement = createElementNode("search");

			childElement = createTextElementNode("include", "false");

			addElement(searchElement, childElement);

			Element treePathNode = createTextElementNode("uid", "101");

			

			addElement(searchElement, treePathNode);

			// create filter

			Element filterParent = createElementNode("filter");

			Element child = createElementNode("filterrule");

			Element subChild = createElementNode("filtercriteria");

			Element subNode = createTextElementNode("headeritem", "Subject");
			addElement(subChild, subNode);
			subNode = createTextElementNode("criteria", "contains");
			addElement(subChild, subNode);
			

			subNode = createTextElementNode("pattern", "pattern");
			addElement(subChild, subNode);
			subNode = createTextElementNode("type", "Subject");
			addElement(subChild, subNode);

			addElement(child, subChild);

			subNode = createTextElementNode("condition", "matchall");
			addElement(child, subNode);

			addElement(filterParent, child);

			addElement(searchElement, filterParent);

			addElement(parentElement, searchElement);

		} else if (type.equals("outbox")) {
			childElement = createTextElementNode("type", type);
			addElement(parentElement, childElement);

		} else if ((type.equals("imap")) || (type.equals("imaproot"))) {
			childElement = createTextElementNode("type", type);
			addElement(parentElement, childElement);

			String uidString = (uid).toString();

			childElement = createTextElementNode("accountuid", uidString);
			addElement(parentElement, childElement);
		}

		AdapterNode childNode = null;

		if (parentNode == null) {
			AdapterNode node = getRootNode();
			childNode = node.addElement(parentElement);

			System.out.println("parent node == null ");
			System.out.println("childnode: " + childNode);

		} else
			childNode = parentNode.addElement(parentElement);

		return childNode;
	}
	*/
	
	/*
	public FolderItem getFolderItem(AdapterNode node) {
		if (node != null) {
			if (node.getName().equals("folder")) {
				FolderItem folderItem = new FolderItem(getDocument());

				folderItem.setNameNode(node.getChild("name"));

				//System.out.println("folder found: "+ node.getChild("name").getValue() );
				folderItem.setAccessRightsNode(node.getChild("accessrights"));
				folderItem.setMessageFolderNode(node.getChild("messagefolder"));
				folderItem.setTypeNode(node.getChild("type"));
				folderItem.setSubfolderNode(node.getChild("subfolder"));
				folderItem.setAddNode(node.getChild("add"));
				folderItem.setRemoveNode(node.getChild("remove"));
				folderItem.setUidNode(node.getChild("uid"));

				String type = node.getChild("type").getValue();

				if ((type.equals("imap")) || (type.equals("imaproot")))
					folderItem.setAccountUidNode(node.getChild("accountuid"));

				AdapterNode filterNode = node.getChild("filterlist");
				if (filterNode != null) {
					folderItem.setFilterListNode(filterNode);

					
				} else {
					AdapterNode searchNode = node.getChild("search");
					if (searchNode != null) {
						//System.out.println("search node found");

						folderItem.setSearchNode(searchNode);
					}

				}

				return folderItem;
			}

		}

		return null;
	}
	*/
	
	/*
	public AdapterNode addEmptyFilterCriteria(AdapterNode filterRuleNode) {
		Element child = createElementNode("filtercriteria");

		Element subNode = createTextElementNode("headeritem", "Subject");
		addElement(child, subNode);
		subNode = createTextElementNode("criteria", "contains");
		addElement(child, subNode);
		subNode = createTextElementNode("pattern", "pattern");
		addElement(child, subNode);
		subNode = createTextElementNode("type", "Subject");
		addElement(child, subNode);


		filterRuleNode.domNode.appendChild(child);

		return new AdapterNode(child);
	}
	*/
	/*
	public AdapterNode addEmptyFilterAction(AdapterNode filterActionNode) {
		Element actionNode = createElementNode("action");

		Element nameNode = createTextElementNode("name", "move");
		addElement(actionNode, nameNode);

		Element uidNode = createTextElementNode("uid", "101");
		addElement(actionNode, uidNode);

		filterActionNode.domNode.appendChild(actionNode);

		return new AdapterNode(actionNode);
	}

	public AdapterNode addEmptyFilterNode(AdapterNode folderNode) {

		Element parent = createElementNode("filter");

		Element child = createTextElementNode("enabled", "true");
		addElement(parent, child);
		child = createTextElementNode("description", "new filter");
		addElement(parent, child);

		addElement(parent, child);

		// create filterrule-node
		child = createElementNode("filterrule");

		Element subChild = createElementNode("filtercriteria");

		Element subNode = createTextElementNode("headeritem", "Subject");
		addElement(subChild, subNode);
		subNode = createTextElementNode("criteria", "contains");
		addElement(subChild, subNode);
		subNode = createTextElementNode("pattern", "pattern");
		addElement(subChild, subNode);
		subNode = createTextElementNode("type", "Subject");
		addElement(subChild, subNode);
		
		addElement(child, subChild);

		subNode = createTextElementNode("condition", "matchall");
		addElement(child, subNode);

		addElement(parent, child);

		// create actionlist-node
		Element actionNode = createElementNode("actionlist");

		Element subActionNode = createElementNode("action");

		Element nameNode = createTextElementNode("name", "move");
		addElement(subActionNode, nameNode);
		Element uidNode = createTextElementNode("uid", "101");
		addElement(subActionNode, uidNode);

		addElement(actionNode, subActionNode);

		addElement(parent, actionNode);

		AdapterNode filterListNode = folderNode.getChild("filterlist");

		if (filterListNode == null) {
			Element listNode = createElementNode("filterlist");

			addElement(listNode, parent);

			Node node = folderNode.domNode.appendChild(listNode);

			AdapterNode result = new AdapterNode(parent);

			return result;
		} else {

			Node node = filterListNode.domNode.appendChild(parent);
			AdapterNode result = new AdapterNode(node);

			return result;
		}

	}
	*/

}
