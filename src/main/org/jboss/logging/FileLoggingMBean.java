/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.logging;

/**
 *
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.2 $
 */
public interface FileLoggingMBean
{
   // Constants -----------------------------------------------------

   // Public --------------------------------------------------------
   public void setLogName(String logName) throws java.io.FileNotFoundException;
   public String getLogName();
}

