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
 *   @version $Revision: 1.3 $
 */
public interface FileLoggingMBean
{
   // Constants -----------------------------------------------------
   public static final String OBJECT_NAME = ":service=Logging,type=File";

   // Public --------------------------------------------------------
   public void setLogName(String logName) throws java.io.FileNotFoundException;
   public String getLogName();
   
   public void setFormat(String format);
   public String getFormat();
}

