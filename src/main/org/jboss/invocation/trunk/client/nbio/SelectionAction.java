/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.invocation.trunk.client.nbio;

import java.nio.channels.SelectionKey;

/**
 * This is the interface that the attachments of SelectorKeys
 * that are registed with the Selector of the SelectorManager manager
 * must implement.
 *
 * @author    <a href="mailto:hiram.chirino@jboss.org">Hiram Chirino</a>
 */
public interface SelectionAction
{
   /** 
    * When the SelectorKey is triggered, the service method will
    * be called on the attachment.
    */
   public void service(SelectionKey selection);
}
