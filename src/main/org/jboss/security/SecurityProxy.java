/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.security;

import java.lang.reflect.Method;
import javax.ejb.EJBContext;

/** An interface describing the requirements for a SecurityInterceptor proxy.
A SecurityProxy allows for the externalization of custom security checks 
on a per-method basis for both the EJB home and remote interface methods.
Custom security checks are those that cannot be described using the
standard EJB deployment time declarative role based security.

@author Scott.Stark@jboss.org
@version $Revision: 1.4 $
 * @stereotype plug-in point
*/
public interface SecurityProxy
{
    public void init(Class beanHome, Class beanRemote, Object securityMgr) throws InstantiationException;
    /** Called prior to any method invocation to set the current EJB context.
    */
    public void setEJBContext(EJBContext ctx);
    /** Called to allow the security proxy to perform any custom security
        checks required for the EJB home interface method.
    @param m, the EJB home interface method? Or is this the EJB bean impl method?
    */
    public void invokeHome(Method m, Object[] args) throws SecurityException;
    /** Called to allow the security proxy to perform any custom security
        checks required for the EJB remote interface method.
    @param m, the EJB remote interface method? Or is this the EJB bean impl method?
    @param bean, the EJB implementation class instance
    */
    public void invoke(Method m, Object[] args, Object bean) throws SecurityException;
}
