package org.jboss.aspect.spi;

import org.dom4j.Namespace;
import org.dom4j.QName;

/**
 * @author Hiram
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public interface AspectDefinitionConstants {

	public static final Namespace NAMESPACE 		= Namespace.get("aspect", "org.jboss.aspect");

	public static final QName ELEM_ASPECT 		= new QName("aspect", NAMESPACE);
	public static final QName ELEM_STACK			= new QName("stack", NAMESPACE);
	public static final QName ELEM_STACK_REF		= new QName("stack-ref", NAMESPACE);
	public static final QName ELEM_INTERCEPTOR	= new QName("interceptor", NAMESPACE);
	public static final QName ELEM_INTERCEPTOR_REF= new QName("interceptor-ref", NAMESPACE);
	
	public static final QName ATTR_NAME 		    = new QName("name", NAMESPACE);
	public static final QName ATTR_CLASS			= new QName("class", NAMESPACE);
	public static final QName ATTR_METHODS		= new QName("methods", NAMESPACE);

}
