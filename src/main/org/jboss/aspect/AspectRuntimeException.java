/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/
package org.jboss.aspect;

import org.jboss.util.NestedRuntimeException;

/**
 * This exception is thrown by an aspect when an unexpected error is
 * encountered by the aspect during a method invocation.
 * 
 * @author <a href="mailto:hchirino@jboss.org">Hiram Chirino</a>
 */
public class AspectRuntimeException extends NestedRuntimeException
{

    /**
     * Constructor for AspectInitizationException.
     * @param msg
     */
    public AspectRuntimeException(String msg)
    {
        super(msg);
    }

    /**
     * Constructor for AspectInitizationException.
     * @param msg
     * @param nested
     */
    public AspectRuntimeException(String msg, Throwable nested)
    {
        super(msg, nested);
    }

    /**
     * Constructor for AspectInitizationException.
     * @param nested
     */
    public AspectRuntimeException(Throwable nested)
    {
        super(nested);
    }

    /**
     * Constructor for AspectInitizationException.
     */
    public AspectRuntimeException()
    {
        super();
    }

}
