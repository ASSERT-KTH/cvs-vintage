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

package org.columba.addressbook.config;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

// This class wraps a DOM node
public class AdapterNode //implements TreeNode
{
	public org.w3c.dom.Node domNode;

	static final String[] typeName =
		{
			"none",
			"Element",
			"Attr",
			"Text",
			"CDATA",
			"EntityRef",
			"Entity",
			"ProcInstr",
			"Comment",
			"Document",
			"DocType",
			"DocFragment",
			"Notation",
			};

	public static final int ELEMENT_TYPE = 1;
	public static final int ATTR_TYPE = 2;
	public static final int TEXT_TYPE = 3;
	public static final int CDATA_TYPE = 4;
	public static final int ENTITYREF_TYPE = 5;
	public static final int ENTITY_TYPE = 6;
	public static final int PROCINSTR_TYPE = 7;
	public static final int COMMENT_TYPE = 8;
	public static final int DOCUMENT_TYPE = 9;
	public static final int DOCTYPE_TYPE = 10;
	public static final int DOCFRAG_TYPE = 11;
	public static final int NOTATION_TYPE = 12;

	public AdapterNode(org.w3c.dom.Node node) {
		domNode = node;
	}

	public String getName() {
		return domNode.getNodeName().trim();
	}

	public AdapterNode addElement(org.w3c.dom.Element e) {
		AdapterNode node = new AdapterNode(domNode.appendChild(e));

		return node;
	}

	public int getIndex(AdapterNode child) {
		int count = getChildCount();

		for (int i = 0; i < count; i++) {
			AdapterNode n = this.getChild(i);
			if (child.domNode == n.domNode)
				return i;
		}
		return -1;
	}

	public AdapterNode getChild(int searchIndex) {
		NodeList list = domNode.getChildNodes();
		org.w3c.dom.Node node = list.item(searchIndex);
		int elementNodeIndex = 0;

		for (int i = 0; i < list.getLength(); i++) {
			node = list.item(i);

			if ((node.getNodeType() == ELEMENT_TYPE)
				&& (elementNodeIndex++ == searchIndex)) {
				break;
			}
		}
		return new AdapterNode(node);
	}

	public AdapterNode getChild(String s) {
		for (int i = 0; i < getChildCount(); i++) {
			AdapterNode child = getChild(i);
			if (child.getName().equals(s))
				return child;
		}
		return null;
	}

	public String getChildValue(int searchIndex) {
		return getChild(searchIndex).getValue();
	}

	public String getValue() {
		String s = new String("");
		org.w3c.dom.NodeList nodeList = domNode.getChildNodes();

		if (nodeList.getLength() >= 1) {
			org.w3c.dom.Node node = nodeList.item(0);
			int type = node.getNodeType();
			s += node.getNodeValue();
		}
		return s;
	}

	public void setValue(String str) {
		org.w3c.dom.NodeList nodeList = domNode.getChildNodes();

		//System.out.println("adapterNode->setvalue: "+ str );

		if (nodeList.getLength() == 1) {
			org.w3c.dom.Node node = nodeList.item(0);
			int type = node.getNodeType();
			node.setNodeValue(str);
		} else if (nodeList.getLength() == 0) {

		} else {
			System.out.println(
				"xml file contains whitespaces where not expected:\n");
			System.out.println("please convert:<test> hallo </test>");
			System.out.println("to:<test>hallo</test>");
			System.out.println(
				"this is going to be fixed in a future release!");
		}

	}

	public String getCDATAValue() {
		String s = new String("");
		org.w3c.dom.NodeList nodeList = domNode.getChildNodes();

		if (nodeList.getLength() >= 1) {
			for (int i = 0; i < nodeList.getLength(); i++) {
				org.w3c.dom.Node node = nodeList.item(i);
				int type = node.getNodeType();
				AdapterNode adpNode = new AdapterNode(node);

				if (type == CDATA_TYPE) {
					s += node.getNodeValue();
				}
			}
		}
		return s.trim();
	}

	public void setCDATAValue(String str) {
		org.w3c.dom.NodeList nodeList = domNode.getChildNodes();

		if (nodeList.getLength() >= 1) {

			for (int i = 0; i < nodeList.getLength(); i++) {
				org.w3c.dom.Node node = nodeList.item(i);
				int type = node.getNodeType();
				AdapterNode adpNode = new AdapterNode(node);

				if (type == CDATA_TYPE) {
					node.setNodeValue(str);
				}

			}
		}
	}

	public int getChildCount() {
		int count = 0;
		NodeList list = domNode.getChildNodes();

		for (int i = 0; i < list.getLength(); i++) {
			org.w3c.dom.Node node = list.item(i);

			if (node.getNodeType() == ELEMENT_TYPE) {
				++count;
			}
		}
		return count;
	}

	public void remove() {
		AdapterNode parent = new AdapterNode(domNode.getParentNode());
		parent.removeChild(this);
	}

	public void appendChild(AdapterNode node) {
		domNode.appendChild(node.domNode);
	}

	public void appendChild(Node node) {
		domNode.appendChild(node);
	}

	public void add(AdapterNode node) {
		domNode.appendChild(node.domNode);
	}

	public void removeChild(AdapterNode child) {
		domNode.removeChild(child.domNode);
	}

	public void removeChildren() {
		for (int i = 0; i < getChildCount(); i++) {
			AdapterNode node = getChild(i);
			removeChild(node);
		}

	}

	public void insert(AdapterNode childNode, int childIndex) {

		AdapterNode nextSibling = getChild(childIndex);
		if (nextSibling == null)
			return;

		//System.out.println( nextSibling.getName() );
		AdapterNode nameNode = nextSibling.getChild("name");
		//System.out.println( "name: "+ nameNode.getValue() );
		//System.out.println( childNode.getName() );
		AdapterNode nameNode2 = childNode.getChild("name");
		//System.out.println( "name: "+ nameNode2.getValue() );
		domNode.insertBefore(childNode.domNode, nextSibling.domNode);

	}

	public java.util.Enumeration children() {
		return null;
	}

	public boolean getAllowsChildren() {
		return true;
	}

	public AdapterNode getChildAt(int childIndex) {
		return getChild(childIndex);
	}

	/*
	  public int getChildCount()
	  {
	  }
	*/

	public int getIndex(Object node) {
		return getIndex(node);
	}

	public AdapterNode getParent() {
		AdapterNode parent = new AdapterNode(domNode.getParentNode());

		return parent;
	}

	public Node getAttrib(String attribName) {
		return domNode.getAttributes().getNamedItem(attribName);
	}


	/*
	
	public boolean isLeaf()
	{
	    if ( domNode.hasChildNodes() ) return false;
	    else return true;
	}
	
	public int getIndex(javax.swing.tree.TreeNode treeNode) {
	}
	*/
}
