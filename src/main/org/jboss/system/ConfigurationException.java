/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.system;

/**
 * Throw to indicate a non-fatal configuration related problem.
 *
 * @see ConfigurationService
 *
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version <pre>$Revision: 1.3 $</pre>
 */
public class ConfigurationException
   extends Exception
{
   /** The root cause of this exception */
   protected Throwable cause;
   
   /**
    * Construct a <tt>ConfigurationException</tt>.
    *
    * @param message    The exception detail message.
    */
   public ConfigurationException(final String message) {
      super(message);
   }
   
   /**
    * Construct a <tt>ConfigurationException</tt>.
    *
    * @param message    The exception detail message.
    * @param cause      The detail cause of the exception.
    */
   public ConfigurationException(final String message, 
      final Throwable cause) 
   {
      super(message);
      this.cause = cause;
   }
   
   /**
    * Get the cause of the exception.
    *
    * @return  The cause of the exception or null if there is none.
    */
   public Throwable getCause() { 
      return cause; 
   }
   
   /**
    * Return a string representation of the exception.
    *
    * @return  A string representation.
    */
   public String toString() {
      return cause == null ? 
         super.toString() : 
         super.toString() + ", Cause: " + cause;
   }
}
