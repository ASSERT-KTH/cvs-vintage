/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.logging;

/**
 *      
 *   @see <related>
 *   @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 *   @version $Revision: 1.4 $
 */
public interface ConsoleLoggingMBean
{
   // Constants -----------------------------------------------------
   public static final String OBJECT_NAME = ":service=Logging,type=Console";
    
   // Public --------------------------------------------------------
   public void setFormat(String format);
   public String getFormat();
}

