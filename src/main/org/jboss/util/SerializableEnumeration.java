/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.util;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.NoSuchElementException;



public class SerializableEnumeration extends ArrayList implements Enumeration {
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
	private int index;

   // Static --------------------------------------------------------
   // Constructors --------------------------------------------------
	
	public SerializableEnumeration () {
		super();
		index = 0;
	}

	public SerializableEnumeration (Collection c) {
		super(c);
		index = 0;
	}
	 
	public SerializableEnumeration (int initialCapacity) {
		super(initialCapacity);
		index = 0;
	}
	 
   // Public --------------------------------------------------------
    // from java.util.Enumeration
	public boolean hasMoreElements() {
		return (index < size());
	}
	
	public Object nextElement() throws NoSuchElementException {
		try {
			Object nextObj = get(index);
			index++;
			return nextObj;
		} catch (IndexOutOfBoundsException e) {
			throw new NoSuchElementException();
		}
	}


   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------

	private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
		// the only thing to write is the index field
    	out.defaultWriteObject();
	}
	
 	private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
		in.defaultReadObject();
	}

   // Inner classes -------------------------------------------------


}