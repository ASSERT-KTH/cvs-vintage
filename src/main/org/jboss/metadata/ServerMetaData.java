/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.metadata;

import java.util.Set;

/**
 * The metadata for a server.  This is generally just a collection of all the
 * EJBs and related metadata that appear in a particular configuration file
 * or resource.  Most of the properties are stored in the metadata for the
 * beans, containers, etc.
 */
public interface ServerMetaData extends MetaData {
    /**
     * Gets the metadata for all the beans available here.  Each element of the
     * Set is of type org.jboss.metadata.BeanMetaData.
     * @see org.jboss.metadata.BeanMetaData
     */
    public Set getBeans();

    /**
     * Gets the metadata for one bean available here.  Each bean is identified
     * by its unique EJB name.
     * @throws java.lang.IllegalArgumentException
     *      Occurs when no bean with the specified name can be found.
     */
    public BeanMetaData getBean(String name);
}