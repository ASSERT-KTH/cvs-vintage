/*   
 *  Copyright 1999-2004 The Apache Sofware Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.tomcat.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;

// from tomcat.session.SessionSerializer

/**
 * Helper for object reloading. Will change the classloader of a
 * serializable object. 
 *
 * The object must be serializable - the caller should do special
 * actions for all non-serializable objects ( like using their
 *  specific protocol ).
 * 
 * Old comment: ( code works now for any object, not only HttpSession )
 *
 * This class manages the serialization of HttpSession object across
 * classloader reloads. It does this by first getting a copy of the 
 * HttpSessions hashtable from the SessionManager and then using a
 * special internal class ObjectInputStream that uses the newly created
 * classloader to de-serialize the sessions. This class is called from
 * within the Handler.handleReload() method. Much of this code
 * is essentially the same as what I put into the Apache JServ release
 * so it is pretty well tested. It also depends on having HttpSession
 * implement the read/writeObject methods properly to only accept 
 * objects that are serializable.
 * 
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @author cmanolache@yahoo.com
 */
public final class ObjectSerializer
{
    static org.apache.commons.logging.Log logger = 
	org.apache.commons.logging.LogFactory.getLog(ObjectSerializer.class);
    /**
       This is the method that does the serialization.
    */
    public static final Object doSerialization(ClassLoader cl,
					       Object sessions)
    {
	// get the hashtable of sessions
	try {
	    // writes the session data out, but loses the contexts
	    // because they cannot be serialized
	    ByteArrayOutputStream b = new ByteArrayOutputStream();
	    ObjectOutputStream o = new ObjectOutputStream(b);
	    
	    // write out the hashtable to the OOS
	    o.writeObject(sessions);
	    o.flush();
	    
	    // create the streams to read the sessions back in from.
	    byte data[]=b.toByteArray();
	    ByteArrayInputStream bIn = new ByteArrayInputStream (data);
	    ObjectInputStream oOut= new ACLObjectInputStream(cl, bIn);
			
	    // unserialize the sessions
	    sessions = oOut.readObject();

	    return sessions;
	} catch (Exception e) {
	    // log the error. there shouldn't be one here though.
	    // XXX We should call Logger.log - this is a problem, but
	    // it's better to have a bug ( writing to out instead of log)
	    // than adding dependencies to context.
	    logger.error( "SessionSerializer: " , e );
	}
	return sessions;
    }
	
    /**
     * When deserializing the sessions during a class
     * loader reload, override the resolveClass() method 
     * so that it uses the AdaptiveClassLoader to deserialize
     * the sessions. This has the benefit of allowing 
     * objects that are only within the ACL's classpath 
     * to be found and deserialized.
     */
    private static final class ACLObjectInputStream extends ObjectInputStream {
	ClassLoader loader;
	
        ACLObjectInputStream(ClassLoader loader, InputStream bIn)
	    throws IOException
	{
            super(bIn);
	    this.loader=loader;
        }

	protected Class resolveClass(ObjectStreamClass v)
            throws IOException, ClassNotFoundException
	{
	    // use our new loader instead of the system loader
            return loader.loadClass(v.getName());
        }
    }
}	
