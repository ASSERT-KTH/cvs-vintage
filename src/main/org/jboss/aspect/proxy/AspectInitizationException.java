/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.aspect.proxy;

/**
 * Thrown when an error occurs during the initialization
 * of an aspect.
 * 
 * @author <a href="mailto:hchirino@jboss.org">Hiram Chirino</a>
 */
public class AspectInitizationException extends Exception {

	/**
	 * Constructor for AspectInitizationException.
	 */
	public AspectInitizationException() {
		super();
	}

	/**
	 * Constructor for AspectInitizationException.
	 * @param message
	 */
	public AspectInitizationException(String message) {
		super(message);
	}

}
