/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.aspect;

/**
 * An AspectNotFoundException is throw when a aspect definition cannot be 
 * found.
 * 
 * Throw from the createAspect methods of the AspectFactory and also from 
 * the AspectClassLoader when a named aspect is not found.
 * 
 * @see org.jboss.aspect.AspectFactory#createAspect(AspectComposition)
 *
 * @author <a href="mailto:hchirino@jboss.org">Hiram Chirino</a>
 */
public class AspectNotFoundException extends ClassNotFoundException {

	/**
	 * Constructor for AspectNotFoundException.
	 */
	public AspectNotFoundException() {
		super();
	}

	/**
	 * Constructor for AspectNotFoundException.
	 * @param message
	 */
	public AspectNotFoundException(String message) {
		super(message);
	}
}
