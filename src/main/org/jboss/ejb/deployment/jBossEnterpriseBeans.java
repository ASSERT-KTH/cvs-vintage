/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.deployment;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.beans.beancontext.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.dreambean.awt.GenericCustomizer;
import com.dreambean.awt.GenericMethodDialog;
import com.dreambean.awt.BeanContextPanel;
import com.dreambean.awt.BeanContextTreeView;
import com.dreambean.awt.GenericCustomizer;
import com.dreambean.ejx.xml.XMLManager;
import com.dreambean.ejx.xml.XmlExternalizable;
import com.dreambean.ejx.Util;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.2 $
 */
public class jBossEnterpriseBeans
   extends com.dreambean.ejx.ejb.EnterpriseBeans
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   boolean secure = true;
	
	Container c;
	Component com;
    
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
    
   // Public --------------------------------------------------------
   public void setSecure(boolean s) { secure = s; }
   public boolean isSecure() { return secure; }

	
   public com.dreambean.ejx.ejb.Entity addEntity()
      throws IOException, ClassNotFoundException
   {
      return (com.dreambean.ejx.ejb.Entity)instantiateChild("org.jboss.ejb.deployment.jBossEntity");
   }

   public com.dreambean.ejx.ejb.Session addSession()
      throws IOException, ClassNotFoundException
   {
      return (com.dreambean.ejx.ejb.Session)instantiateChild("org.jboss.ejb.deployment.jBossSession");
   }
   
	public void addJndiPrefix(String prefix)
	{
		Iterator enum = super.iterator();
		while(enum.hasNext())
		{
			jBossEnterpriseBean bean = (jBossEnterpriseBean)enum.next();
			bean.setJndiName(prefix + bean.getJndiName());
		}
	}

   // BeanContextContainerProxy implementation ----------------------
   public Container getContainer()
   {
      if (c == null)
      {
   		c = new BeanContextPanel(this);
   		JSplitPane sp = (JSplitPane)c;
   		JScrollPane scrollPane = (JScrollPane)sp.getLeftComponent();
   		((BeanContextTreeView)scrollPane.getViewport().getView()).expandPath(((BeanContextTreeView)scrollPane.getViewport().getView()).getPathForRow(0));
   		((BeanContextTreeView)scrollPane.getViewport().getView()).setRootVisible(false);
   		JToolBar toolBar = new JToolBar();
   		toolBar.add(new AbstractAction("Add JNDI name prefix")
   		{
   			public void actionPerformed(ActionEvent evt)
   			{
   				try
   				{
   					BeanInfo bi = Introspector.getBeanInfo(jBossEnterpriseBeans.class);
   					
   					MethodDescriptor[] mdList = bi.getMethodDescriptors();
   					for (int i = 0; i < mdList.length; i++)
   					{
   						if (mdList[i].getName().equals("addJndiPrefix"))
   						{
   							new GenericMethodDialog(jBossEnterpriseBeans.this, mdList[i], (Frame)SwingUtilities.getRoot(c));
   							break;
   						}
   					}
   				} catch (Exception e)
   				{
   					e.printStackTrace();
   				}
   			}
   		});
   		
   		JPanel p = new JPanel(new BorderLayout());
   		p.add("Center", c);
   		p.add("North", toolBar);
   		
   		c = p;
   		c.setName("Enterprise beans");
      }
      return (Container)c;
   }
	
   // BeanContextChildComponentProxy implementation -----------------
   public Component getComponent()
   {
      if (com == null)
      {
	      com = new GenericCustomizer(false, this);
      	com.setName("Application settings");
      }
      return com;
   }
	
   // XmlExternalizable implementation ------------------------------
   public Element exportXml(Document doc)
      throws Exception
   {
      Element enterprisebeans = super.exportXml(doc);
      
      XMLManager.addElement(enterprisebeans,"secure",new Boolean(isSecure()).toString());
      
      return enterprisebeans;
   }
   
   public void importXml(Element elt)
      throws Exception
   {
      if (elt.getOwnerDocument().getDocumentElement().getTagName().equals(jBossEjbJar.JBOSS_DOCUMENT))
      {
         NodeList nl = elt.getChildNodes();
         for (int i = 0; i < nl.getLength(); i++)
         {
            Node n = nl.item(i);
            String name = n.getNodeName();
            
            if (name.equals("entity"))
            {
               try
               {
                  NodeList enl = ((Element)n).getElementsByTagName("ejb-name");
                  String ejbName = XMLManager.getString(enl.item(0));
                  
                  jBossEntity bean = (jBossEntity)getEjb(ejbName);
                  bean.importXml((Element)n);
               } catch (IllegalArgumentException e)
               {
                  e.printStackTrace();
                  // Does not exist anymore...
               }
            } else if (name.equals("session"))
            {
               try
               {
                  NodeList enl = ((Element)n).getElementsByTagName("ejb-name");
                  String ejbName = XMLManager.getString(enl.item(0));
                  jBossSession bean = (jBossSession)getEjb(ejbName);
                  bean.importXml((Element)n);
               } catch (IllegalArgumentException e)
               {
                  e.printStackTrace();
                  // Does not exist anymore...
               }
            } else if (name.equals("secure"))
            {
               setSecure(new Boolean(XMLManager.getString(n)).booleanValue());
            } 
         }
      } else
      {
         super.importXml(elt);
      }
   }
    
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
