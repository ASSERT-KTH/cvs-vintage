/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/
package org.jboss.aspect.spi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.dom4j.Element;
import org.jboss.aspect.AspectInitizationException;
import org.jboss.aspect.internal.AspectSupport;

/**
 * The AspectDefinition holds the definition for a aspect.
 * <p>
 * 
 * Multiple instances of the same aspect will share the AspectDefinition
 * configuration object. 
 * <p>
 * 
 * AspectDefinition objects can be dynamicaly created at runtime and passed
 * to the AspectFactory to create dynamicaly generated aspects.
 * <p>
 * 
 * @see org.jboss.aspect.AspectFactory#createAspect(AspectDefinition, Object)
 *
 * @author <a href="mailto:hchirino@jboss.org">Hiram Chirino</a>
 */
final public class AspectDefinition implements AspectDefinitionConstants, Serializable, Cloneable
{
    /** the name of the aspect definition */
    public String name;
    /** the interceptor stack of the aspect */
    public AspectInterceptorHolder interceptors[];
    /** the interfaces that the aspect exposes */
    public Class interfaces[];

    /**
     * Constructor.
     */
    public AspectDefinition(String name, AspectInterceptorHolder interceptors[], Class interfaces[])
    {
        this.name = name;
        this.interceptors = interceptors;
        this.interfaces = interfaces;
    }

    /**
     * Build the AspectDefinition from a XML Element Fragment.  If the fragment
     * contains any interceptor-ref elements or stack-ref, they will be looked up 
     * in the namedInterceptors and namedStacks Maps respectivly.
     */
    public AspectDefinition(Element xml, Map namedInterceptors, Map namedStacks) throws AspectInitizationException
    {

        if (xml.attribute(ATTR_NAME) == null)
            throw new AspectInitizationException("attribute " + ATTR_NAME.getQualifiedName() + " is required");
        name = xml.attribute(ATTR_NAME).getValue();

        ArrayList v = AspectSupport.loadAspectInterceptorHolderList(xml, namedInterceptors, namedStacks);
        interceptors = new AspectInterceptorHolder[v.size()];
        v.toArray(interceptors);

        v.clear();
        for (int i = 0; i < interceptors.length; i++)
        {
            Class x[] = interceptors[i].getInterfaces();
            if (x == null)
                continue;
            for (int j = 0; j < x.length; j++)
            {
                if (!v.contains(x[j]))
                    v.add(x[j]);
            }
        }

        interfaces = new Class[v.size()];
        v.toArray(interfaces);

    }

	public AspectDefinition cloneAspectDefinition() {
        try
        {
            return (AspectDefinition)clone();
        }
        catch (CloneNotSupportedException e)
        {
        	return new AspectDefinition(name, interceptors, interfaces);
        }
    }
		
    /**
     * Creates a duplicate AspectDefinition but with the provided
     * interceptor inserted at the index position in the stack.
     * 
     * @throws IndexOutOfBoundsException - if the index is out of range.
     */
    public void insertInterceptor(int index, AspectInterceptorHolder holder)
        throws IndexOutOfBoundsException
    {
        if (index < 0 || index > interceptors.length)
            throw new IndexOutOfBoundsException();
        AspectInterceptorHolder dest[] = new AspectInterceptorHolder[interceptors.length + 1];
        arrayInsetShift(interceptors, dest, index);
        dest[index] = holder;
		interceptors = dest;
    }

    /**
     * Creates a duplicate AspectDefinition but with the 
     * interceptor at the index position removed from the stack.
     * 
     * @throws IndexOutOfBoundsException - if the index is out of range.
     */
    public void removeInterceptor(int index) throws IndexOutOfBoundsException
    {
        if (index < 0 || index >= interceptors.length)
            throw new IndexOutOfBoundsException();
        AspectInterceptorHolder dest[] = new AspectInterceptorHolder[interceptors.length - 1];
        arrayRemoveShift(interceptors, dest, index);
		interceptors = dest;
    }

    private static void arrayInsetShift(Object src[], Object dest[], int position)
    {
        System.arraycopy(src, 0, dest, 0, position);
        System.arraycopy(src, position, dest, position + 1, src.length - position);
    }

    private static void arrayRemoveShift(Object src[], Object dest[], int position)
    {
        System.arraycopy(src, 0, dest, 0, position);
        System.arraycopy(src, position + 1, dest, position, (src.length - position) - 1);
    }

}
