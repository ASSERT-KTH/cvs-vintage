/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.metadata;

/**
 * The metadata for a field of an EJB.  It is generally used to hold persistance
 * properties, but other uses are possible.
 */
public interface FieldMetaData extends MetaData {
    /**
     * Gets the name of the field.  This is the unique identifier for a field
     * (within the scope of the owning EJB).
     */
    public String getName();
}