/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.deployment;

import java.util.Vector;
import java.util.Iterator;

import org.jboss.metadata.MetaData;

import org.w3c.dom.Element;

/**
 *	<description> 
 *      
 *	@see <related>
 *	@author <firstname> <lastname> (<email>)
 *	@version $Revision: 1.4 $
 */
public class J2eeApplicationMetaData
   extends MetaData
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
	String displayName;
   String description;
   String smallIcon;
	String largeIcon;
	
	Vector modules;
	
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   public J2eeApplicationMetaData (Element rootElement) throws DeploymentException
	{
		importXml (rootElement);
	}
	
   // Public --------------------------------------------------------

   public String getDisplayName ()
   {
   	return displayName;
   }
   
   public String getDescription ()
   {
   	return description;
   }
   
   public String getSmallIcon ()
   {
   	return smallIcon;
   }
   
   public String getLargeIcon ()
   {
   	return largeIcon;
   }
   
   public Iterator getModules ()
   {
   	return modules.iterator ();
   }
   



    public void importXml (Element element) throws DeploymentException
    {
		String rootTag = element.getOwnerDocument().getDocumentElement().getTagName();
		
		if (rootTag.equals("application")) {
			
			// get some general info
         displayName = getElementContent (getUniqueChild (element, "display-name"));
         Element e = getOptionalChild (element, "description");
			description = e != null ? getElementContent (e) : "";

         e = getOptionalChild (element, "icon");
			if (e != null)
			{
            Element e2 = getOptionalChild (element, "small-icon");
	 			smallIcon = e2 != null ? getElementContent (e2) : "";
				
            e2 = getOptionalChild (element, "large-icon");
	 			largeIcon = e2 != null ? getElementContent (e2) : "";
		   }
         else
		   {
				smallIcon = "";
				largeIcon = "";
			}
			
			// extract modules...
			modules = new Vector ();
			Iterator it = getChildrenByTagName (element, "module");
			while (it.hasNext ())
			{
				modules.add (new J2eeModuleMetaData ((Element)it.next ()));
			}
		
		} else 
			throw new DeploymentException("Unrecognized root tag in EAR deployment descriptor: "+ element);
	}
   
    
   // Y overrides ---------------------------------------------------
   
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
