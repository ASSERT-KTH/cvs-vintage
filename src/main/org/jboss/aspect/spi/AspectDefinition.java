/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.aspect.spi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.dom4j.Element;
import org.jboss.aspect.AspectInitizationException;
import org.jboss.aspect.internal.*;

/**
 * The AspectDefinition holds the aspect definition for a single aspect.
 * <p>
 * 
 * Multiple instances of the same aspect will share the AspectDefinition
 * configuration object.  This class is immutable and, therefore, threadsafe.
 * <p>
 * 
 * AspectDefinition objects can be dynamicaly created at runtime and passed
 * to the AspectFactory to create dynamicaly generated aspects.
 * <p>
 * 
 * @see org.jboss.aspect.AspectFactory#createAspect(AspectDefinition)
 *
 * @author <a href="mailto:hchirino@jboss.org">Hiram Chirino</a>
 */
final public class AspectDefinition implements AspectDefinitionConstants, Serializable
{

    final public String name;
    final public AspectInterceptorHolder interceptors[];
    final public Class interfaces[];

    public AspectDefinition(Element xml) throws AspectInitizationException
    {
        this(xml, new HashMap(), new HashMap());
    }

    public AspectDefinition(Element xml, Map namedInterceptors, Map namedStacks) throws AspectInitizationException
    {

        if (xml.attribute(ATTR_NAME) == null)
            throw new AspectInitizationException("attribute " + ATTR_NAME.getQualifiedName() + " is required");
		name = xml.attribute(ATTR_NAME).getValue();

		ArrayList v = AspectSupport.loadAspectInterceptorHolderList(xml, namedInterceptors, namedStacks);
        interceptors = new AspectInterceptorHolder[v.size()];
        v.toArray(interceptors);
        
		v.clear();
		for( int i=0; i < interceptors.length; i++ ) {
			Class x[] = interceptors[i].getInterfaces();
			if( x == null )
				continue;
			for( int j=0; j < x.length; j++ ) {
				if( !v.contains(x[j]) )
					v.add(x[j]);
			}
		}
		
		interfaces = new Class[v.size()];
		v.toArray(interfaces);

    }

}
