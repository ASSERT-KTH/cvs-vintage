/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.invocation.nrmi.algorithm;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * Since the supported JDKs include JDK1.3 and @see java.util.IdentityHashMap
 * was not introduced until JDK1.4, use this factory class to create all maps
 * used in this NRMI implementation
 * @author <a href="mailto:tilevich@cc.gatech.edu">Eli Tilevich</a>
 * @version $Revision: 1.1 $
 * */

public class FastHashMapFactory {

private static Constructor _emptyArgsConstructor = null;
public static synchronized Map createHashMap () {
	return createHashMap (16);
}

public static synchronized Map createHashMap (int capacity) {
try {

if (null == _emptyArgsConstructor) {
Class identityHashMapClass = Class.forName ("java.util.IdentityHashMap");
_emptyArgsConstructor = identityHashMapClass.getDeclaredConstructor (new Class[]{int.class});
}
return (Map)_emptyArgsConstructor.newInstance (new Object[]{new Integer(capacity)});

} catch (Exception e) {
	return new HashMap ();
}

}

}
