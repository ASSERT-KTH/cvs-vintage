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
package org.columba.core.config;

import org.columba.core.xml.XmlElement;

public class DefaultItem {
	XmlElement root;
	//private Document document;

	public DefaultItem(XmlElement root) {
		this.root = root;
	}

	public XmlElement getRoot() {
		return root;
	}

	/************************ composition pattern **********************/

	public XmlElement getElement(String pathToElement) {
		return getRoot().getElement(pathToElement);
	}

	public XmlElement getChildElement(int index) {
		return getRoot().getElement(index);
	}

	public int getChildCount() {
		return getRoot().count();
	}

	public XmlElement getChildElement(String pathToElement, int index) {
		return getRoot().getElement(pathToElement).getElement(index);
	}

	public boolean contains(String key) {
		return getRoot().getAttributes().containsKey(key);
	}

	public String get(String key) {
		return getRoot().getAttribute(key);
	}

	public String get(String pathToElement, String key) {
		return getElement(pathToElement).getAttribute(key);
	}

	/*
	public String get(String pathToElement, String key, String defaultValue) {
		XmlElement parent = getElement(pathToElement);
	
		return parent.getAttribute(key, defaultValue);
	}
	*/
	public void set(String key, String newValue) {
		getRoot().addAttribute(key, newValue);
	}

	public void set(String pathToElement, String key, String newValue) {
		getElement(pathToElement).addAttribute(key, newValue);
	}

	/**************************** helper classes ***************************/

	public int getInteger(String key) {
		String value = get(key);

		return Integer.parseInt(value);
	}

	public int getInteger(String key, int defaultValue) {
		String value = get(key);
		
		if ( value == null )
		{
			value = new Integer(defaultValue).toString();
			set(key, value);
			
		}

		return Integer.parseInt(value);
	}

	public int getInteger(String pathToElement, String key) {
		String value = get(pathToElement, key);

		return Integer.parseInt(value);
	}

	public int getInteger(String pathToElement, String key, int defaultValue) {
		String value = get(pathToElement, key);

		if (value == null) {
			value = new Integer(defaultValue).toString();
			set(pathToElement, key, value);
			
		}

		return Integer.parseInt(value);

	}

	public void set(String key, int value) {
		set(key, Integer.toString(value));
	}

	public void set(String pathToElement, String key, int value) {
		set(pathToElement, key, Integer.toString(value));
	}

	public boolean getBoolean(String key, boolean defaultValue) {
		String value = get(key);
		
		if ( value == null )
		{
			value = new Boolean(defaultValue).toString();
			set(key,value);
			
		}
		

		return new Boolean(value).booleanValue();
	}

	public boolean getBoolean(String key) {
		String value = get(key);

		return new Boolean(value).booleanValue();
	}

	public boolean getBoolean(String pathToElement, String key) {
		String value = get(pathToElement, key);

		return new Boolean(value).booleanValue();
	}

	public boolean getBoolean(
		String pathToElement,
		String key,
		boolean defaultValue) {

		String value = get(pathToElement, key);

		if (value == null) {
			value = new Boolean(defaultValue).toString();
			set(pathToElement, key, value);
			
		}

		return new Boolean(value).booleanValue();
	}

	public void set(String key, boolean value) {
		set(key, value ? Boolean.TRUE.toString() : Boolean.FALSE.toString());
	}

	public void set(String pathToElement, String key, boolean value) {
		set(
			pathToElement,
			key,
			value ? Boolean.TRUE.toString() : Boolean.FALSE.toString());
	}

	/*
	public DefaultItem(Document doc)
	{
		this.document = doc;
	}
	*/

	/*
	public Document getDocument()
	{
		return document;
	}
	*/

	/********************************** set / get *********************************/

	/*
	public String convertToString(String s)
	{
		//StringBuffer sb = new StringBuffer( s );
		int index = -1;
	
		
		return s;
	}
	
	public String convertFromString(String s)
	{
		StringBuffer sb = new StringBuffer(s);
	
		for (int j = 0; j < sb.length(); j++)
		{
			if (sb.charAt(j) == '<')
			{
				sb.setCharAt(j, '&');
				sb.insert(j + 1, "lt;");
				j += 3;
			}
			else if (sb.charAt(j) == '&')
			{
				sb.setCharAt(j, '&');
				sb.insert(j + 1, "amp;");
				j += 4;
			}
			else if (sb.charAt(j) == '>')
			{
				sb.setCharAt(j, '&');
				sb.insert(j + 1, "gt;");
				j += 3;
			}
			else if (sb.charAt(j) == '\'')
			{
				sb.setCharAt(j, '&');
				sb.insert(j + 1, "apos;");
				j += 5;
			}
			else if (sb.charAt(j) == '"')
			{
				sb.setCharAt(j, '&');
				sb.insert(j + 1, "quot;");
				j += 5;
			}
		}
	
		return sb.toString();
	}
	*/

	/*
	public void setTextValue(AdapterNode node, String value)
	{
		org.w3c.dom.NodeList nodeList = node.domNode.getChildNodes();
	
		//System.out.println("length: "+ nodeList.getLength() +"  value: "+value +"  name: "+ node.getName() );
	
		if (nodeList.getLength() == 1)
		{
			org.w3c.dom.Node n = nodeList.item(0);
			int type = n.getNodeType();
	
			n.setNodeValue(value);
		}
		else if (nodeList.getLength() == 0)
		{
			node.appendChild(document.createTextNode(value));
		}
		else if (nodeList.getLength() > 1)
		{
			//System.out.println("lenght1: "+ node.domNode.getChildNodes() );
			for (int i = nodeList.getLength() - 1; i >= 0; i--)
			{
				//System.out.println("nodeList length: "+ nodeList.getLength() );
				node.domNode.removeChild(nodeList.item(i));
			}
			//System.out.println("lenght2: "+ node.domNode.getChildNodes() );
			node.appendChild(document.createTextNode(value));
			//System.out.println("lenght3: "+ node.domNode.getChildNodes() );
		}
	}
	
	public void setCDATAValue(AdapterNode node, String value)
	{
		org.w3c.dom.NodeList nodeList = node.domNode.getChildNodes();
	
		if (nodeList.getLength() >= 1)
		{
			for (int i = 0; i < nodeList.getLength(); i++)
			{
				org.w3c.dom.Node n = nodeList.item(i);
				int type = n.getNodeType();
				AdapterNode adpNode = new AdapterNode(n);
	
				if (type == AdapterNode.CDATA_TYPE)
				{
					n.setNodeValue(value);
				}
			}
		}
		else
		{
			System.out.println("no node found - creating CDATA-node");
			
			CDATASection e = createCDATAElementNode(value);
			node.appendChild( e );
		}
	}
	
	public String getTextValue(AdapterNode node)
	{
		String s = new String("");
		String t = new String();
	
		org.w3c.dom.NodeList nodeList = node.domNode.getChildNodes();
	
		if (nodeList.getLength() >= 1)
		{
			for (int i = 0; i < nodeList.getLength(); i++)
			{
	
				org.w3c.dom.Node n = nodeList.item(i);
				AdapterNode adpNode = new AdapterNode(n);
				int type = n.getNodeType();
	
				if (type == AdapterNode.ENTITYREF_TYPE)
				{
					String value = adpNode.getValue();
					t = value.trim();
				}
				else
				{
					String value = n.getNodeValue();
					t = value.trim();
					int x = t.indexOf("\n");
					if (x >= 0)
						t = t.substring(0, x);
	
				}
				s += t;
			}
		}
	
		return s.trim();
	}
	
	public String getCDATAValue(AdapterNode node)
	{
		String s = new String("");
		org.w3c.dom.NodeList nodeList = node.domNode.getChildNodes();
	
		if (nodeList.getLength() >= 1)
		{
			for (int i = 0; i < nodeList.getLength(); i++)
			{
				org.w3c.dom.Node n = nodeList.item(i);
				int type = n.getNodeType();
				AdapterNode adpNode = new AdapterNode(n);
	
				if (type == AdapterNode.CDATA_TYPE)
				{
					s += n.getNodeValue();
				}
			}
		}
	
		return s.trim();
	}
	*/

	/************************************** ADD **************************************/

	/*
	public void addElement(Element parent, Element child)
	{
		parent.appendChild(child);
	}
	
	public void addCDATASection(Element parent, CDATASection child)
	{
		parent.appendChild(child);
	}
	*/

	/***************************************** CREATE ***********************************/

	/*
	public Element createTextElementNode(String key, String value)
	{
		AdapterNode adpNode = new AdapterNode(document);
	
		Element newElement = (Element) document.createElement(key);
		newElement.appendChild(document.createTextNode(value));
	
		return newElement;
	}
	
	public CDATASection createCDATAElementNode(String key)
	{
		AdapterNode adpNode = new AdapterNode(document);
	
		CDATASection newElement = (CDATASection) document.createCDATASection(key);
	
		return newElement;
	}
	
	public Element createElementNode(String key)
	{
		AdapterNode adpNode = new AdapterNode(document);
	
		Element newElement = (Element) document.createElement(key);
		return newElement;
	}
	
	public AdapterNode addKey(
		AdapterNode parent,
		String elementName,
		String defaultValue)
	{
		Element e = createTextElementNode(elementName, defaultValue);
		AdapterNode newNode = new AdapterNode(e);
	
		parent.add(newNode);
	
		return newNode;
	}
	*/
}