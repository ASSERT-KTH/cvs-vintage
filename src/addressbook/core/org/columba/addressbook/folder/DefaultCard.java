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
package org.columba.addressbook.folder;

import java.util.Hashtable;

import org.columba.addressbook.config.AdapterNode;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * @version         1.0
 * @author
 */
public class DefaultCard {
    protected Hashtable hashtable;
    protected Object uid;
    protected AdapterNode rootNode;
    protected Document document;

    public DefaultCard(Document doc, AdapterNode rootNode) {
        //super( doc );
        this.document = doc;
        this.rootNode = rootNode;

        hashtable = new Hashtable();
    }

    public DefaultCard() {
    }

    public AdapterNode getRootNode() {
        return rootNode;
    }

    public void formatSet(String key, String subkey, String value) {
        AdapterNode child = getRootNode().getChild(key);

        if (child != null) {
            AdapterNode subchild = child.getChild(subkey);

            if (subchild != null) {
                setCDATAValue(child, value);
            } else {
                Element sub = createElementNode(subkey);

                CDATASection cdata = createCDATAElementNode(value);
                addCDATASection(sub, cdata);

                AdapterNode newNode = new AdapterNode(sub);
                child.add(newNode);
            }
        } else {
            Element e = createElementNode(key);
            Element sub = createElementNode(subkey);
            addElement(e, sub);

            CDATASection cdata = createCDATAElementNode(value);
            addCDATASection(sub, cdata);

            AdapterNode newNode = new AdapterNode(e);
            getRootNode().add(newNode);
        }
    }

    public void formatSet(String key, String value) {
        AdapterNode child = getRootNode().getChild(key);

        if (child != null) {
            setCDATAValue(child, value);
        } else {
            Element e = createElementNode(key);

            CDATASection cdata = createCDATAElementNode(value);
            addCDATASection(e, cdata);

            AdapterNode newNode = new AdapterNode(e);
            getRootNode().add(newNode);
        }
    }

    public void set(String key, String value) {
        AdapterNode child = getRootNode().getChild(key);

        if (child != null) {
            setTextValue(child, value);
        } else {
            addKey(getRootNode(), key, value);
        }
    }

    public void set(String key, String subkey, String value) {
        AdapterNode child = getRootNode().getChild(key);

        if (child != null) {
            AdapterNode subchild = child.getChild(subkey);

            if (subchild != null) {
                setTextValue(subchild, value);
            } else {
                addKey(child, subkey, value);

                //addKey( getRootNode(), key, value );	
            }
        } else {
            Element e = createElementNode(key);

            /*
            Element subelement = createTextElementNode(subkey,value);
            addElement( subelement, e );
            */
            AdapterNode newNode = new AdapterNode(e);
            getRootNode().add(newNode);

            addKey(newNode, subkey, value);
        }
    }

    public String get(String key) {
        if (key == null) {
            return "";
        }

        AdapterNode child = getRootNode().getChild(key);

        if (child != null) {
            return getTextValue(child);
        }

        return "";
    }

    public String formatGet(String key) {
        if (key == null) {
            return "";
        }

        AdapterNode child = getRootNode().getChild(key);

        if (child != null) {
            return getCDATAValue(child);
        }

        return "";
    }

    public String get(String key, String subkey) {
        if ((key == null) || (subkey == null)) {
            return "";
        }

        AdapterNode child = getRootNode().getChild(key);

        if (child != null) {
            AdapterNode subchild = child.getChild(subkey);

            if (subchild != null) {
                return subchild.getValue();
            }
        }

        return "";
    }

    /*
    public void add( String key, String attribut, String value )
    {
            key = key.toLowerCase();
            for ( int i=0; i<getRootNode().getChildCount(); i++ )
            {
                    AdapterNode child = getRootNode().getChildAt(i);
                    System.out.println("node-name:"+child.getName() );
                    String name = child.getName();

                    if ( name.equalsIgnoreCase(key) == true )
                    {
                            // found key, now search for attributes
                            //String value = (String) child.getValue();
                            //System.out.println("node-value:"+value);
                            child.setValue( value );
                    }

            }
    }

    public String get( String key, String attribut )
    {
            key = key.toLowerCase();

            for ( int i=0; i<getRootNode().getChildCount(); i++ )
            {
                    AdapterNode child = getRootNode().getChildAt(i);
                    System.out.println("node-name:"+child.getName() );
                    String name = child.getName();

                    if ( name.equalsIgnoreCase(key) == true )
                    {
                            // found key, now search for attributes
                            String value = (String) child.getValue();


                            System.out.println("node-value:"+value);
                            return value;
                    }

            }

            return "";
    }

    public String get( String key  )
    {
            key = key.toLowerCase();

            for ( int i=0; i<getRootNode().getChildCount(); i++ )
            {
                    AdapterNode child = getRootNode().getChildAt(i);
                    System.out.println("node-name:"+child.getName() );
                    String name = child.getName();

                    if ( name.equalsIgnoreCase(key) == true )
                    {
                            // found key, now search for attributes
                            String value = (String) child.getValue();


                            System.out.println("node-value:"+value);
                            return value;
                    }

            }

            return "";
    }


    */
    public void setUid(Object uid) {
        this.uid = uid;
    }

    public Object getUid() {
        return uid;
    }

    /************************************** ADD **************************************/
    public void addElement(Element parent, Element child) {
        parent.appendChild(child);
    }

    public void addCDATASection(Element parent, CDATASection child) {
        parent.appendChild(child);
    }

    /***************************************** CREATE ***********************************/
    public Element createTextElementNode(String key, String value) {
        AdapterNode adpNode = new AdapterNode(document);

        Element newElement = (Element) document.createElement(key);
        newElement.appendChild(document.createTextNode(value));

        return newElement;
    }

    public CDATASection createCDATAElementNode(String key) {
        AdapterNode adpNode = new AdapterNode(document);

        CDATASection newElement = (CDATASection) document.createCDATASection(key);

        return newElement;
    }

    public Element createElementNode(String key) {
        AdapterNode adpNode = new AdapterNode(document);

        Element newElement = (Element) document.createElement(key);

        return newElement;
    }

    public AdapterNode addKey(AdapterNode parent, String elementName,
        String defaultValue) {
        Element e = createTextElementNode(elementName, defaultValue);
        AdapterNode newNode = new AdapterNode(e);

        parent.add(newNode);

        return newNode;
    }

    public void setTextValue(AdapterNode node, String value) {
        org.w3c.dom.NodeList nodeList = node.domNode.getChildNodes();

        //System.out.println("length: "+ nodeList.getLength() +"  value: "+value +"  name: "+ node.getName() );
        if (nodeList.getLength() == 1) {
            org.w3c.dom.Node n = nodeList.item(0);
            int type = n.getNodeType();

            n.setNodeValue(value);
        } else if (nodeList.getLength() == 0) {
            node.appendChild(document.createTextNode(value));
        } else if (nodeList.getLength() > 1) {
            //System.out.println("lenght1: "+ node.domNode.getChildNodes() );
            for (int i = nodeList.getLength() - 1; i >= 0; i--) {
                //System.out.println("nodeList length: "+ nodeList.getLength() );
                node.domNode.removeChild(nodeList.item(i));
            }

            //System.out.println("lenght2: "+ node.domNode.getChildNodes() );
            node.appendChild(document.createTextNode(value));

            //System.out.println("lenght3: "+ node.domNode.getChildNodes() );
        }
    }

    public void setCDATAValue(AdapterNode node, String value) {
        org.w3c.dom.NodeList nodeList = node.domNode.getChildNodes();

        if (nodeList.getLength() >= 1) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                org.w3c.dom.Node n = nodeList.item(i);
                int type = n.getNodeType();
                AdapterNode adpNode = new AdapterNode(n);

                if (type == AdapterNode.CDATA_TYPE) {
                    n.setNodeValue(value);
                }
            }
        }
    }

    public String getTextValue(AdapterNode node) {
        String s = "";
        String t = "";

        org.w3c.dom.NodeList nodeList = node.domNode.getChildNodes();

        if (nodeList.getLength() >= 1) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                org.w3c.dom.Node n = nodeList.item(i);
                AdapterNode adpNode = new AdapterNode(n);
                int type = n.getNodeType();

                if (type == AdapterNode.ENTITYREF_TYPE) {
                    String value = adpNode.getValue();
                    t = value.trim();
                } else {
                    String value = n.getNodeValue();
                    t = value.trim();

                    int x = t.indexOf("\n");

                    if (x >= 0) {
                        t = t.substring(0, x);
                    }
                }

                s += t;
            }
        }

        return s.trim();
    }

    public String getCDATAValue(AdapterNode node) {
        String s = "";
        org.w3c.dom.NodeList nodeList = node.domNode.getChildNodes();

        if (nodeList.getLength() >= 1) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                org.w3c.dom.Node n = nodeList.item(i);
                int type = n.getNodeType();
                AdapterNode adpNode = new AdapterNode(n);

                if (type == AdapterNode.CDATA_TYPE) {
                    s += n.getNodeValue();
                }
            }
        }

        return s.trim();
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document doc) {
        this.document = doc;
    }
}
