/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.metadata;

import org.jboss.metadata.io.*;

/**
 * A metadata "plugin" - that is, all the metadata provided by a specific
 * configuration file or resource.  This interface gives access to the Class
 * object used to store each type of metadata, and Visitors that read in, write
 * out, or reformat the metadata.
 */
public interface MetaDataPlugin {
    /**
     * Gets the implementation of ServerMetaData for the plugin.
     * @see org.jboss.metadata.ServerMetaData
     */
    public Class getServerClass();

    /**
     * Gets the implementation of BeanMetaData for the plugin.
     * @see org.jboss.metadata.BeanMetaData
     */
    public Class getBeanClass();

    /**
     * Gets the implementation of ContainerMetaData for the plugin.
     * @see org.jboss.metadata.ContainerMetaData
     */
    public Class getContainerClass();

    /**
     * Gets the implementation of MethodMetaData for the plugin.
     * @see org.jboss.metadata.MethodMetaData
     */
    public Class getMethodClass();

    /**
     * Gets the implementation of FieldMetaData for the plugin.
     * @see org.jboss.metadata.FieldMetaData
     */
    public Class getFieldClass();

    /**
     * Gets the implementation of XMLReader for the plugin.
     * @see org.jboss.metadata.io.XMLReader
     */
    public XMLReader getXMLReader();
}