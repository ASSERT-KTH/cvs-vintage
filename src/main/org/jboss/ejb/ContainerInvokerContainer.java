/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

/**
 * This is an interface for Containers that uses ContainerInvokers.
 *
 * <p>ContainerInvokers may communicate with the Container through
 *    this interface.
 *
 * @see ContainerInvoker
 * 
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:docodan@mvcsoft.com">Daniel OConnor</a>
 * @version $Revision: 1.7 $
 */
public interface ContainerInvokerContainer
{
   /**
    * ???
    *
    * @return ???
    */
   Class getHomeClass();
   
   /**
    * ???
    *
    * @return ???
    */
   Class getRemoteClass();
   
   /**
    * ???
    *
    * @return ???
    */
   Class getLocalHomeClass();
   
   /**
    * ???
    *
    * @return ???
    */
   Class getLocalClass();
	
   /**
    * ???
    *
    * @return ???
    */
   ContainerInvoker getContainerInvoker();
   
   /**
    * ???
    *
    * @return ???
    */
   LocalContainerInvoker getLocalContainerInvoker();
}

