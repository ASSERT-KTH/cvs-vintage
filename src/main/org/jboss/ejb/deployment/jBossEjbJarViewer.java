/*
 * Copyright 1999 by dreamBean Software,
 * All rights reserved.
 */
package org.jboss.ejb.deployment;

import java.beans.Customizer;

import com.dreambean.awt.GenericCustomizer;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author Rickard Oberg (rickard@dreambean.com)
 *   @version $Revision: 1.1 $
 */
public class jBossEjbJarViewer
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
		
		jBossEjbJar ejbJar = (jBossEjbJar)obj;
		
		// Init UI
		addTab("Container configurations", ejbJar.getContainerConfigurations().getComponent());
		addTab("Resource managers", ejbJar.getResourceManagers().getComponent());
	}
}
