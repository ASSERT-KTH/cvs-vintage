/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.aspect;

import org.jboss.util.NestedException;

/**
 * This exception is thrown when an Aspect fails to initialize.
 * 
 * @author <a href="mailto:hchirino@jboss.org">Hiram Chirino</a>
 */
public class AspectInitizationException extends NestedException
{

    /**
     * Constructor for AspectInitizationException.
     * @param msg
     */
    public AspectInitizationException(String msg)
    {
        super(msg);
    }

    /**
     * Constructor for AspectInitizationException.
     * @param msg
     * @param nested
     */
    public AspectInitizationException(String msg, Throwable nested)
    {
        super(msg, nested);
    }

    /**
     * Constructor for AspectInitizationException.
     * @param nested
     */
    public AspectInitizationException(Throwable nested)
    {
        super(nested);
    }

    /**
     * Constructor for AspectInitizationException.
     */
    public AspectInitizationException()
    {
        super();
    }

}
