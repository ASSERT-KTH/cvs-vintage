/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc.metadata;

import org.jboss.deployment.DeploymentException;
import org.jboss.metadata.MetaData;
import org.w3c.dom.Element;

/**
 * This immutable class contains information about entity command
 *
 * @author <a href="mailto:loubyansky@ua.fm">Alex Loubyansky</a>
 * @version $Revision: 1.1 $
 */
public final class JDBCEntityCommandMetaData {

   /** The name (alias) of the command. */
   private final String commandName;

   /** The class of the command */
   private final Class commandClass;

   /**
    * Constructs a JDBCEntityMetaData reading the entity-command element
    * @param entity-command element
    */
   public JDBCEntityCommandMetaData(Element element)
      throws DeploymentException {

      commandName = MetaData.getUniqueChildContent(
         element, "command-name");

      String commandClassStr = MetaData.getUniqueChildContent(
         element, "command-class");
      if(commandClassStr == null) {
         throw new DeploymentException(
            "command-class isn't specified for command: " + commandName);
      }

      try {
         commandClass = Thread.currentThread().
            getContextClassLoader().loadClass( commandClassStr );
      } catch (ClassNotFoundException e) {
         throw new DeploymentException(
            "command-class not found: " + commandClassStr);
      }
   }


   /**
    * @return the name of the command
    */
   public String getCommandName() {
      return commandName;
   }

   /**
    * @return the class of the command
    */
   public Class getCommandClass() {
      return commandClass;
   }
}
