/*
 * Copyright 1999 by dreamBean Software,
 * All rights reserved.
 */
package org.jboss.ejb.deployment;

import java.awt.*;
import java.beans.*;
import java.beans.beancontext.*;
import java.io.*;
import java.util.*;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.dreambean.awt.GenericCustomizer;
import com.dreambean.ejx.xml.XMLManager;
import com.dreambean.ejx.xml.XmlExternalizable;
import com.dreambean.ejx.Util;

/**
 *   Castor JDO support
 *
 *   @author Oleg Nitz (on@ibis.odessa.ua)
 *   @version $Revision: 1.1 $
 */
public class CastorJDOResource
        extends BeanContextChildSupport
        implements BeanContextChildComponentProxy, XmlExternalizable, ResourceManager
{
    String name= "";
   
    String jndiName= "";

    Customizer c;
   
    public void setName(String n) { name = n; }
    public String getName() { return name; }
   
    public void setJndiName(String n) { jndiName = n; }
    public String getJndiName() { return jndiName; }
   
    public String getType() { return "org.exolab.castor.jdo.DataObjects"; }
   
    public void removeResource()
    {
        getBeanContext().remove(this);
    }
    
    // BeanContextChildComponentProxy implementation -----------------
    public Component getComponent()
    {
        if (c == null)
            c = new GenericCustomizer(false, this);
        return (Component)c;
    }
   
    // XmlExternalizable implementation ------------------------------
    public Element exportXml(Document doc)
        throws Exception
    {
        Element resourcemanager = doc.createElement("resource-manager");
        XMLManager.addAttribute(resourcemanager,"res-class",getClass().getName());
        XMLManager.addElement(resourcemanager,"res-name",getName());
      
        XMLManager.addElement(resourcemanager,"res-jndi-name",getJndiName());
      
        return resourcemanager;
    }
   
    public void importXml(Element elt)
        throws Exception
    {
        if (elt.getOwnerDocument().getDocumentElement().getTagName().equals("jboss"))
        {
            NodeList nl = elt.getChildNodes();
            for (int i = 0; i < nl.getLength(); i++)
            {
                Node n = nl.item(i);
                String name = n.getNodeName();
             
                if (name.equals("res-jndi-name"))
                {
                    setJndiName(n.hasChildNodes() ? XMLManager.getString(n) : "");
                } else if (name.equals("res-name")) 
                {
                    setName(n.hasChildNodes() ? XMLManager.getString(n) : "");
                }
            }
        }
    }
   
    public void propertyChange(PropertyChangeEvent evt)
    {
    }
}
