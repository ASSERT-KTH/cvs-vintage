/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/
package org.jboss.aspect.spi;

import org.dom4j.Namespace;
import org.dom4j.QName;

/**
 * Used to hold constants.
 * 
 * @author <a href="mailto:hchirino@jboss.org">Hiram Chirino</a>
 */
public interface AspectDefinitionConstants
{

    public static final Namespace NAMESPACE = Namespace.get("aspect", "org.jboss.aspect");

    public static final QName ELEM_ASPECT = new QName("aspect", NAMESPACE);
    public static final QName ELEM_STACK = new QName("stack", NAMESPACE);
    public static final QName ELEM_STACK_REF = new QName("stack-ref", NAMESPACE);
    public static final QName ELEM_INTERCEPTOR = new QName("interceptor", NAMESPACE);
    public static final QName ELEM_INTERCEPTOR_REF = new QName("interceptor-ref", NAMESPACE);

    public static final QName ATTR_NAME = new QName("name", NAMESPACE);
    public static final QName ATTR_CLASS = new QName("class", NAMESPACE);
    public static final QName ATTR_METHODS = new QName("methods", NAMESPACE);
    public static final QName ATTR_INTERFACES = new QName("interfaces", NAMESPACE);
    public static final QName ATTR_FILTER = new QName("filter", NAMESPACE);

}
