/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.metadata;

/**
 * The metadata for one method of an EJB or the EJB's home interface.
 */
public interface MethodMetaData extends MetaData {
    /**
     * Gets the name of this method.  The name and parameter class types of a
     * method together are the unique identifier.
     */
    public String getName();

    /**
     * Gets the parameter types of this method.  The name and parameter class
     * types of a method together are the unique identifier.
     */
    public Class[] getParameterTypes();
}