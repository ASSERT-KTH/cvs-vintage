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

package org.columba.core.xml;
/////////////////////////////////////////////////////////////////////////////
//                          IMPORT STATEMENTS                              //
/////////////////////////////////////////////////////////////////////////////

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Vector;
/////////////////////////////////////////////////////////////////////////////
//                                 CODE                                    //
/////////////////////////////////////////////////////////////////////////////

/**
 * The XmlElement is a generic containment class for elements within an XML
 * file.
 * <p>
 * 
 * It extends Observable which should be used for gui elements which are interested 
 * in configuration changes.
 * <p>
 * 
 * Show interested in:
 * 
 * <pre>
 *  xmlElement.addObserver( yourObserver );
 * </pre>
 * 
 * <p>
 * When making bigger changes on XmlElement and probably its
 * subnodes and/or a greater number of attributes at once,
 * you should just change XmlElement directly and manually
 * notify the Observers by calling:
 * <p>
 * 
 * <pre>
 *  xmlElement.setChanged();
 *  xmlElement.notifyObservers();
 * </pre>
 * 
 * <p> 
 * There a good introduction for the Observable/Observer pattern in
 * Model/View/Controller based applications at www.javaworld.com:
 * - http://www.javaworld.com/javaworld/jw-10-1996/jw-10-howto.html
 * 
 * @see org.columba.mail.gui.config.general.MailOptionsDialog#initObservables()
 * @see org.columba.mail.gui.table.util.MarkAsReadTimer
 *
 * @author       Tony Parent, fdietz
 */
public class XmlElement extends Observable {
	String name;
	String data;
	Hashtable attributes;
	List subElements;
	XmlElement parent;

	/**
	 * **FIXME** This function needs documentation
	 *
	 * Constructor
	 *
	 */
	public XmlElement() {
		subElements = new Vector();
		this.attributes = new Hashtable(10);
	}
	/**
	 * **FIXME** This function needs documentation
	 *
	 * Constructor
	 * @param String Name
	 *
	 */
	public XmlElement(String name) {
		this.name = name;
		this.attributes = new Hashtable(10);
		subElements = new Vector();
		data = "";
	}
	/**
	 * **FIXME** This function needs documentation
	 *
	 * Constructor
	 * @param String Name
	 * @param Hashtable Attributes
	 *
	 */
	public XmlElement(String name, Hashtable attributes) {
		this.name = name;
		this.attributes = attributes;
		subElements = new Vector();
	}
	/**
	 * **FIXME** This function needs documentation
	 *
	 * Constructor
	 * @param Name String
	 * @param Data String
	 *
	 */
	public XmlElement(String name, String data) {
		this.name = name;
		this.data = data;
		subElements = new Vector();
	}

	/**
	 * Add attribute to this xml element.
	 * 
	 * @param name		name of key
	 * @param value		new attribute value
	 * @return			new attribute value
	 * 
	 */
	public Object addAttribute(String name, String value) {
		if ((value != null) && (name != null)) {
			Object returnValue = (attributes.put(name, value));
			
			return returnValue;
		}

		return null;
	}
	/**
	 * **FIXME** This function needs documentation
	 *
	 * @return  String
	 * @param String Name
	 *
	 */
	public String getAttribute(String name) {
		return ((String) attributes.get(name));
	}

	public String getAttribute(String name, String defaultValue) {
		if (getAttribute(name) == null) {
			addAttribute(name, defaultValue);
		}

		return getAttribute(name);
	}
	/**
	 * **FIXME** This function needs documentation
	 *
	 * @return  String
	 * @param String Name
	 *
	 */
	public Hashtable getAttributes() {
		return attributes;
	}
	/**
	 * **FIXME** This function needs documentation
	 *
	 *
	 * @param Attrs Hashtable to use as the attributes
	 *
	 */
	public void setAttributes(Hashtable attrs) {
		attributes = attrs;
	}
	/**
	 * **FIXME** This function needs documentation
	 *
	 * @return  Enumeration
	 *
	 */
	public Enumeration getAttributeNames() {
		return (attributes.keys());
	}
	/**
	 * **FIXME** This function needs documentation
	 *
	 * @return  boolean
	 * @param XmlElement E
	 *
	 */
	public boolean addElement(XmlElement e) {
		e.setParent(this);
		return (subElements.add(e));
	}

	public XmlElement removeElement(XmlElement e) {
		XmlElement child = null;
		for (int i = 0; i < subElements.size(); i++) {
			child = (XmlElement) subElements.get(i);
			// FIXME -- This will most likely not work.
			//          You want the element removed if the contents are the same
			//          Not just if the element reference is the same.
			if (child == e) {
				subElements.remove(i);
			}
		}
		return (child);
	}

	public XmlElement removeElement(int index) {
		return (XmlElement) subElements.remove(index);
	}

	public void removeAllElements() {
		subElements.clear();
	}

	/**
	 * convienience method for the TreeView
	 * 
	 * this method is modeled after the DefaultMutableTreeNode-class
	 * 
	 * DefaultMutableTreeNode wraps XmlElement for this purpose
	 * 
	 */
	public void removeFromParent() {
		XmlElement parent = getParent();
		if (parent == null)
			return;

		parent.removeElement(this);
	}

	public void append(XmlElement e) {
		e.removeFromParent();

		addElement(e);
	}
	/**
	 * 
	 * convienience method for the TreeView 
	 * 
	 * @param e
	 * @param index
	 */
	public void insertElement(XmlElement e, int index) {
		e.removeFromParent();

		subElements.add(index, e);
		e.setParent(this);
	}

	/**
	 * **FIXME** This function needs documentation
	 *
	 * @return  Vector
	 *
	 */
	public List getElements() {
		return subElements;
	}

	public int count() {
		return subElements.size();
	}

	/**
	 * **FIXME** This function needs documentation
	 *
	 * @return  XmlElement
	 * @param String Path
	 *
	 */
	public XmlElement getElement(String path) {
		int i = path.indexOf('/');
		String topName, subName;
		if (i == 0) {
			path = path.substring(1);
			i = path.indexOf('/');
		}
		if (i > 0) {
			topName = path.substring(0, i);
			subName = path.substring(i + 1);
		} else {
			topName = path;
			subName = null;
		}
		int j;
		for (j = 0; j < subElements.size(); j++) {
			if (((XmlElement) subElements.get(j)).getName().equals(topName)) {
				if (subName != null) {
					return (
						((XmlElement) subElements.get(j)).getElement(subName));
				} else {
					return ((XmlElement) subElements.get(j));
				}
			}
		}
		return null;
	}

	public XmlElement getElement(int index) {
		return (XmlElement) subElements.get(index);
	}

	/**
	 * Adds a sub element to this one
	 *
	 * @return  XmlElement
	 * @param Name The name of the sub element to add
	 *
	 */
	public XmlElement addSubElement(String name) {
		XmlElement e = new XmlElement(name);
		e.setParent(this);
		subElements.add(e);
		return e;
	}

	/**
	 * Adds a sub element to this one
	 *
	 * @return  XmlElement
	 * @param   element The XmlElement to add
	 *
	 */
	public XmlElement addSubElement(XmlElement e) {
		e.setParent(this);
		subElements.add(e);
		return e;
	}

	/**
	 * Adds a sub element to this one
	 *
	 * @return  XmlElement
	 * @param Name The name of the sub element to add
	 * @param Data String Data for this element
	 */
	public XmlElement addSubElement(String name, String data) {
		XmlElement e = new XmlElement(name);
		e.setData(data);
		e.setParent(this);
		subElements.add(e);

		return e;
	}

	/**
	 * Sets the parent element
	 *
	 * @param Parent The XmlElement that contains this one
	 *
	 */
	public void setParent(XmlElement parent) {
		this.parent = parent;
	}
	/**
	 * Gives the XmlElement containing the current element
	 *
	 * @return  XmlElement
	 *
	 */
	public XmlElement getParent() {
		return parent;
	}
	/**
	 * Sets the data for this element
	 *
	 * @param D The String representation of the data
	 *
	 */
	public void setData(String d) {
		data = d;
	}
	/**
	 * Returns the data associated with the current Xml element
	 *
	 * @return  String
	 *
	 */
	public String getData() {
		return data;
	}

	/**
	 * Returns the name of the current Xml element
	 *
	 * @return  String
	 *
	 */
	public String getName() {
		return name;
	}

	/**
	 * **FIXME** This function needs documentation
	 *
	 * @param out OutputStream to print the data to
	 *
	 */
	/*
	public void write(OutputStream out) throws IOException {
		PrintWriter PW = new PrintWriter(out);
		PW.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		if (SubElements.size() > 0) {
			for (int i = 0; i < SubElements.size(); i++) {
				((XmlElement) SubElements.get(i))._writeSubNode(PW, 4);
			}
		}
		PW.flush();
	}
	*/
	/**
	 * Prints sub nodes to the given data stream
	 *
	 * @param out    PrintWriter to use for printing
	 * @param indent Number of spaces to indent things
	 *
	 */
	/*
	private void _writeSubNode(PrintWriter out, int indent)
		throws IOException {
		_writeSpace(out, indent);
		out.print("<" + Name);
		//if ( Attributes.size()>1) out.print(" ");
	
		for (Enumeration e = Attributes.keys(); e.hasMoreElements();) {
			String K = (String) e.nextElement();
			out.print(K + "=\"" + Attributes.get(K) + "\" b");
	
		}
		out.print(">");
	
		if (Data != null && !Data.equals("")) {
			if (Data.length() > 20) {
				out.println("");
				_writeSpace(out, indent + 2);
			}
			out.print(Data);
		}
		if (SubElements.size() > 0) {
			out.println("");
			for (int i = 0; i < SubElements.size(); i++) {
				((XmlElement) SubElements.get(i))._writeSubNode(
					out,
					indent + 4);
			}
			_writeSpace(out, indent);
		}
		out.println("</" + Name + ">");
	
	}
	*/
	/**
	 * Prints out a given number of spaces
	 *
	 * @param out       PrintWriter to use for printing
	 * @param numSpaces Number of spaces to print
	 *
	 */

	/*
	private void _writeSpace(PrintWriter out, int numSpaces)
		throws IOException {
	
		for (int i = 0; i < numSpaces; i++)
			out.print(" ");
	}
	
	public static void printNode(XmlElement Node, String indent) {
		String Data = Node.getData();
		if (Data == null || Data.equals("")) {
			System.out.println(indent + Node.getName());
		} else {
			System.out.println(indent + Node.getName() + " = '" + Data + "'");
		}
		Vector Subs = Node.getElements();
		int i, j;
		for (i = 0; i < Subs.size(); i++) {
			printNode((XmlElement) Subs.get(i), indent + "    ");
		}
	}
	*/

	public static void printNode(XmlElement node, String indent) {
		String data = node.getData();
		if (data == null || data.equals("")) {
			System.out.println(indent + node.getName());
		} else {
			System.out.println(indent + node.getName() + " = '" + data + "'");
		}

		// print attributes
		for (Enumeration enum = node.getAttributes().keys();
			enum.hasMoreElements();
			) {
			String key = (String) enum.nextElement();
			String value = node.getAttribute(key);
			System.out.println(indent + key + ":" + value);
		}

		List subs = node.getElements();
		int i, j;
		for (Iterator it = subs.iterator(); it.hasNext();) {
			printNode((XmlElement) it.next(), indent + "    ");
			// for (i = 0; i < subs.size(); i++) {
			// printNode((XmlElement) subs.get(i), indent + "    ");
		}
	}

	public Object clone() {
		XmlElement clone = new XmlElement(new String(getName()));
		clone.setName(getName());
		clone.setAttributes((Hashtable) getAttributes().clone());
		clone.setData(new String(getData()));

		List childs = getElements();
		XmlElement child;
		for (Iterator it = childs.iterator(); it.hasNext();) {
			child = (XmlElement) it.next();
			// for( int i=0; i<childs.size(); i++ ) {
			// child = (XmlElement) childs.get(i);
			clone.addSubElement((XmlElement) child.clone());
		}

		return clone;
	}
	/**
	 * Sets the name.
	 * @param name The name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Notify all Observers.
	 * 
	 * @see java.util.Observable#notifyObservers()
	 */
	public void notifyObservers() {
		setChanged();
		super.notifyObservers();
	}

} // END public class XmlElement
