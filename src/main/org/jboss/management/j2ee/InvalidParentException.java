/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.management.j2ee;

/**
* Exception thrown when a given parent has either a
* wrong ObjectName, is not available or could not be
* reached.
*
* @author <a href="mailto:andreas@jboss.com">Andreas Schaefer</a>
* @version $Revision: 1.1 $
**/
public class InvalidParentException
   extends WrapperException
{
   public InvalidParentException( String pMessage, Throwable pThrowable ) {
      super( pMessage, pThrowable );
   }
}
