/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.deployment;

import org.jboss.metadata.MetaData;

import org.w3c.dom.Element;

/**
 *	<description> 
 *      
 *	@see <related>
 *	@author <a href="mailto:daniel.schulze@telkel.com">Daniel Schulze</a>
 *	@version $Revision: 1.5 $
 */
public class J2eeModuleMetaData
   extends MetaData
{
   // Constants -----------------------------------------------------
   public static final int EJB = 0; 
   public static final int WEB = 1; 
   public static final int CLIENT = 2; 
   public static final int CONNECTOR = 3; 
	private static final String[] tags = {"ejb", "web", "java", "connector"}; 
	 
   // Attributes ----------------------------------------------------
	int type;
   
	String fileName;
   String alternativeDD;
	String webContext;

   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   public J2eeModuleMetaData (Element moduleElement)  throws DeploymentException
	{
		importXml (moduleElement);
	}
	
   // Public --------------------------------------------------------

	public boolean isEjb ()
	{
		return (type == EJB);
	}

	public boolean isWeb ()
	{
		return (type == WEB);
	}
	
	public boolean isJava ()
	{
		return (type == CLIENT);
	}
	
	public boolean isConnector ()
	{
		return (type == CONNECTOR);
	}
	
	
	
	public String getFileName ()
	{
		return fileName;
	}
	
	public String getAlternativeDD ()
	{
		return alternativeDD;
	}
	
	public String getWebContext ()
	{
		if (type == WEB)
			return webContext;
		else
			return null;
	}


    public void importXml (Element element) throws DeploymentException 
	 {
		if (element.getTagName ().equals("module")) 
		{
  		 boolean done = false; // only one of the tags can hit! 
         for (int i = 0; i < tags.length; ++i)
			{
				Element child = getOptionalChild (element, tags[i]);
				if (child == null)
					continue;
				
            if (done)
					throw new DeploymentException ("malformed module definition in application dd: "+ element);

            type = i;
				switch (type)
				{
					case EJB:
					case CLIENT:
					case CONNECTOR:
						fileName = getElementContent (child);
					   alternativeDD = getElementContent (getOptionalChild (element, "alt-dd"));
						break;
					case WEB:
						fileName = getElementContent (getUniqueChild (child, "web-uri"));
						webContext = getElementContent (getOptionalChild (child, "context-root"));
					    alternativeDD = getElementContent (getOptionalChild (element, "alt-dd"));
				}
				done = true;
			}
		 } else 
			throw new DeploymentException ("unrecognized module tag in application dd: "+ element);
	}
   
    
   // Y overrides ---------------------------------------------------
   
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
