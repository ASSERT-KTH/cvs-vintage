/*
 * Copyright 1999 by dreamBean Software,
 * All rights reserved.
 */
package org.jboss.ejb.plugins.jaws.deployment;

import java.beans.Customizer;

import com.dreambean.awt.GenericCustomizer;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author Rickard Oberg (rickard@dreambean.com)
 *   @version $Revision: 1.1 $
 */
public class JawsEjbJarViewer
   extends com.dreambean.ejx.ejb.EjbJarViewer
	implements Customizer
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   // Customizer implementation  ------------------------------------
	public void setObject(Object obj)
	{
		super.setObject(obj);
		
		// Remove assembler descriptor tab
		removeTabAt(indexOfTab("Assembly descriptor"));
		
		JawsEjbJar ejbJar = (JawsEjbJar)obj;
		
		// Init UI
		addTab("Type mappings", ejbJar.getTypeMappings().getComponent());
	}
}
