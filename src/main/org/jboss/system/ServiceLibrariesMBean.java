/*
* JBoss, the OpenSource EJB server
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.system;

import java.net.URL;
/**
*   <description>
*
*   @see <related>
*   @author  <a href="mailto:marc@jboss.org">Marc Fleury</a>
*   @version $Revision: 1.1 $
* 
*/
public interface ServiceLibrariesMBean
{
   // Constants -----------------------------------------------------
   public static final String OBJECT_NAME = "JBOSS-SYSTEM:service=Libraries";
   // Static --------------------------------------------------------
   
   // Public --------------------------------------------------------
   
	
	/* The ServicesLibraries MBean should expose "soft" information like the dependencies graph 
	for example give a URL and find out what MBean need to be restarted in case you cycle that URL
	*/
	
}

/**
* <p><b>Revisions:</b>
* <p><b>2001/06/22 , marcf</b>:
* <ol> 
*    <li> Initial import
* </ol>
*/

