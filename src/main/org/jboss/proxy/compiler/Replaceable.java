/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.proxy.compiler;

import java.io.Serializable;

/**
 * ???
 *      
 * @author Unknown
 * @version $Revision: 1.2 $
 */
public interface Replaceable 
   extends Serializable
{
   /**
    * ???
    *
    * @return ???
    *
    * @throws ObjectStreamException
    */
   Object writeReplace() throws ObjectStreamException;
}

