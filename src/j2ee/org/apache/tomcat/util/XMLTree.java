/*
 * $Header: /tmp/cvs-vintage/tomcat/src/j2ee/org/apache/tomcat/util/XMLTree.java,v 1.1 2000/02/11 00:22:39 costin Exp $
 * $Revision: 1.1 $
 * $Date: 2000/02/11 00:22:39 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:  
 *       "This product includes software developed by the 
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * [Additional notices, if required by prior licensing conditions]
 *
 */ 


package org.apache.tomcat.util;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;

/**
 *
 * @author James Todd [gonzo@eng.sun.com]
 */

public class XMLTree {
    private String name = null;
    private String value = null;
    private Hashtable attributes = new Hashtable();
    private Vector elements = new Vector();

    public XMLTree() {
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
	return value;
    }

    public void setValue(String value) {
	if (value != null) {
	    this.value = value.trim();
	} else {
	    this.value = value;
	}
    }

    public Object getAttribute(Object key) {
       if (key == null) {
            throw new NullPointerException("getAttribute(key)");
	}

        return attributes.get(key);
    }

    public void addAttribute(Object key, Object value)
    throws NullPointerException {
        if (key == null) {
	    throw new NullPointerException("addAttribute(key, value)");
	}

        attributes.put(key, value);
    }

    public void removeAttribute(Object key)
    throws NullPointerException {
       if (key == null) {
            throw new NullPointerException("removeAttribute(key)");
	}

        attributes.remove(key);
    }

    public Enumeration attributes() {
        return attributes.keys();
    }

    public Hashtable getAttributes() {
        return attributes;
    }

    public void addElement(Object value)
    throws NullPointerException {
        if (value == null) {
            throw new NullPointerException("addElement(value)");
        }

        elements.addElement(value);
    }

    public void removeElement(Object value)
    throws NullPointerException {
        if (value == null) {
            throw new NullPointerException("removeElement(value)");
	}

        elements.removeElement(value);
    }

    public Enumeration elements() {
        return elements.elements();
    }
    
    public XMLTree getFirstElement(Object key) {
	if (getElements(key).size() > 0) {
	    return (XMLTree) getElements(key).elementAt(0);
	} else {
	    return null;
	}
    }


    public Enumeration elements(Object key) {
        return getElements(key).elements();
    }

    public Vector getElements() {
        return elements;
    }

    public Vector getElements(Object key) {
        Vector v = new Vector();
	Enumeration e = getElements().elements();

	while (e.hasMoreElements()) {
	    XMLTree element = (XMLTree)e.nextElement();

	    if (element.getName().equals(key)) {
	        v.addElement(element);
	    }
	}

	return v;
    }
    public void clear() {
        attributes.clear();
        elements.removeAllElements();
    }

    public String toString() {
	String s = "Tree: name " + name + " value " + value;
	s = s + "\n elements: " + elements;
	return s;
    }

/*
    public String
    toString () {
        java.lang.Class clazz = getClass();
        java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
        java.util.Hashtable ht = new java.util.Hashtable();
        java.lang.String object = null;
        java.lang.Object value = null;

        for (int i = 0; i < fields.length; i++) {
            try {
                object = fields[i].getName();
                value = fields[i].get(this);

                if (value == null) {
                    value = new String("null");
                }

                ht.put(object, value);
            } catch (IllegalAccessException e) {
                System.out.println("Exception: " + e);
            }
        }

        if (clazz.getSuperclass().getSuperclass() != null) {
            ht.put("super", clazz.getSuperclass().toString());
        }

        return clazz.getName() + ht;
    }
*/
}
