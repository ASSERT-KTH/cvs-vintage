/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.resource.security;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.resource.spi.ManagedConnectionFactory;

import org.jboss.logging.Log;
import org.jboss.resource.RARMetaData;

/**
 *   Base class for <code>PrincipalMapping</code> implementations that
 *   want to share some implementation tedium.
 *
 *   <p> The implementation of <code>setProperties</code> assumes that
 *   the properties string is in <code>Properties.load</code> format
 *   and takes care of converting to a <code>Properties</code> object.
 *
 *   @author Toby Allsopp (toby.allsopp@peace.com)
 *   @version $Revision: 1.1 $
 */
public abstract class PrincipalMappingSupport
   implements PrincipalMapping
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   protected Log log;
   protected ManagedConnectionFactory mcf;
   protected RARMetaData metadata;
   protected Properties properties;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   // PrincipalMapping implementation -------------------------------

   public void setLog(Log log) { this.log = log; }

   public void setManagedConnectionFactory(ManagedConnectionFactory mcf)
   {
      this.mcf = mcf;
   }

   public void setRARMetaData(RARMetaData metadata)
   {
      this.metadata = metadata;
   }

   public void setProperties(String propStr)
   {
      properties = new Properties();
      try
      {
         properties.load(
            new ByteArrayInputStream(propStr.getBytes("ISO-8859-1")));
      }
      catch (IOException ioe)
      {
         log.error("Couldn't convert properties string '" + propStr + "' to " +
                   "Properties");
         log.exception(ioe);
      }
      afterSetProperties();
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   /**
    * Called once the <code>properties</code> field has been
    * initialised and populated.
    */
   protected void afterSetProperties() {}

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
