/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.security;

/** An interface for factories of SecurityProxy objects. It is used
to create a SecurityProxy from a security delegate object that is
not a SecurityProxy instance.

@author Scott_Stark@displayscape.com
@version $Revision: 1.1 $
*/
public interface SecurityProxyFactory
{
    public SecurityProxy create(Object proxyDelegate);
}
