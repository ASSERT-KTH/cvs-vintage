/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.metadata;

import java.util.*;

/**
 * The base class for all the metadata classes.  All the metadata type support
 * named properties, which you can get or set.  In addition, individual metadata
 * implementation may be part of a set, represented by the metadata manager.
 * Each set generally holds all the metadata from a specific configuration file
 * or source.
 */
public interface MetaData extends Map {
    /**
     * Tells whether this specific metadata has a property by the specified name.
     */
    public boolean hasProperty(String name);

    /**
     * Gets a property by name.
     * @param name The name of the property you want
     * @return The value of the specified property, or null if the property
     *      exists but has not been set or has been set to null
     * @throws java.lang.IllegalArgumentException
     *      Occurs when the specified property does not exist
     */
    public Object getProperty(String name);

    /**
     * Sets the value of a property.
     * @param name The name of the property you're setting
     * @param value The value you want to set for the specified property (may be
     *      null)
     * @throws java.lang.IllegalArgumentException
     *      Occurs when the specified property does not exist
     */
    public void setProperty(String name, Object value);

    /**
     * Gets the names of all the properties present in this metadata.  These
     * properties may have a value of null, but they definitely exist.
     */
    public String[] getPropertyNames();


    /**
     * Gets the manager for the plugin that this metadata is a part of (i.e.
     * jBoss, JAWS, etc.).  This will be null if the metadata is not part of
     * a specific plugin (for example, if it is the aggregate of all plugins).
     */
    public MetaDataPlugin getManager();
}