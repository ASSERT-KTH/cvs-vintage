/*
 * Copyright 1999 by dreamBean Software,
 * All rights reserved.
 */
package org.jboss.ejb.plugins.jaws.deployment;

import java.beans.Customizer;
import java.beans.beancontext.BeanContext;

import javax.swing.JTabbedPane;

import com.dreambean.awt.GenericCustomizer;
import com.dreambean.awt.GenericPropertySheet;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author Rickard Oberg (rickard@dreambean.com)
 *   @version $Revision: 1.2 $
 */
public class JawsEntityViewer
   extends JTabbedPane
	implements Customizer
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   // Customizer implementation  ------------------------------------
	public void setObject(Object obj)
	{
		// Init UI
		addTab("Entity", new GenericCustomizer(true, obj));
		
		try
		{
		   addTab("CMP mappings", new GenericPropertySheet((BeanContext)obj, JawsCMPField.class));
		} catch (Exception e) {}
		
		try
		{
		   addTab("Finders", new GenericPropertySheet((BeanContext)obj, Finder.class));
		} catch (Exception e) {}
	}
}
