/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jrmp.interfaces;

/**
 * A factory for producing {@link InitialContextHandle} objects.
 *      
 * @author  Jason Dillon <a href="mailto:jason@planet57.com">&lt;jason@planet57.com&gt;</a>
 * @version $Revision: 1.1 $
 */
public interface InitialContextHandleFactory
{
    /**
     * Creates an initial context handle suitable creating an initial
     * context for the current virtual machine.
     *
     * @return  An initial context handle suitable for the current vm.
     */
    InitialContextHandle create();
}
