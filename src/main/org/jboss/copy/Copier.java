/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 *
 */
package org.jboss.copy;

import java.util.IdentityHashMap;

/**
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 */
public interface Copier 
{
   /**
    * Returns a deep copy of the source. 
    * @param source object to be copied
    * @param referenceMap map from old object to new copies; used to maintain
    * internal references in new copied object
    * @return a deep copy of the source
    * @throws CopyException if a problem occurs during copy
    */
   Object copy(Object source, IdentityHashMap referenceMap);
}
