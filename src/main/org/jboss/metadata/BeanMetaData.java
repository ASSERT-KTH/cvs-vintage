/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.metadata;

import java.util.Set;

/**
 * The metadata for a specific EJB.  The bean may iself have properties, and
 * it has several collections of other metadata instances that may have
 * additional properties.
 */
public interface BeanMetaData extends MetaData {
    /**
     * Gets the metadata for a method of this bean.  The method is identified
     * by its name and argument class names.
     * @throws java.lang.IllegalArgumentException
     *      Occurs when no method with the specified name and arguments can be
     *      found.
     */
    public MethodMetaData getMethod(String name, String[] args);

    /**
     * Gets the metadata for a method of this bean.  The method is identified
     * by its name and argument classes.
     * @throws java.lang.IllegalArgumentException
     *      Occurs when no method with the specified name and arguments can be
     *      found.
     */
    public MethodMetaData getMethod(String name, Class[] args);

    /**
     * Gets the metadata for a method of this bean's home interface.  The
     * method is identified by its name and argument class names.
     * @throws java.lang.IllegalArgumentException
     *      Occurs when no method with the specified name and arguments can be
     *      found.
     */
    public MethodMetaData getHomeMethod(String name, String[] args);

    /**
     * Gets the metadata for a method of this bean's home interface.  The
     * method is identified by its name and argument classes.
     * @throws java.lang.IllegalArgumentException
     *      Occurs when no method with the specified name and arguments can be
     *      found.
     */
    public MethodMetaData getHomeMethod(String name, Class[] args);

    /**
     * Gets the metadata for a field of this bean.  This is generally used for
     * persistence but may have properties for other things as well.
     * @throws java.lang.IllegalArgumentException
     *      Occurs when no field with the specified name can be found.
     */
    public FieldMetaData getField(String name);

    /**
     * Gets the metadata for this bean's container.  One set of container
     * metadata may be shared across several beans, or they may all have
     * individual instances.
     */
    public ContainerMetaData getContainer();

    /**
     * Gets the metadata for all the methods of this bean.  Each element in the
     * Set is of type MethodMetaData.
     * @see org.jboss.metadata.MethodMetaData
     */
    public Set getMethods();

    /**
     * Gets the metadata for all the methods of this bean's home interface.
     * Each element in the Set is of type MethodMetaData.
     * @see org.jboss.metadata.MethodMetaData
     */
    public Set getHomeMethods();

    /**
     * Gets the metadata for all the fields of this bean.  Each element in the
     * Set is of type FieldMetaData.
     * @see org.jboss.metadata.FieldMetaData
     */
    public Set getFields();

    /**
     * Gets the EJB name of this bean.  This is the unique identifier for each
     * bean (it must be globally unique).
     */
    public String getName();
}