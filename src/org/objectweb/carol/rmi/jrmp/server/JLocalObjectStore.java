/*
 * Created on Oct 7, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.objectweb.carol.rmi.jrmp.server;

import java.util.ArrayList;

/**
 * @author riviereg
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class JLocalObjectStore {
	private static int counter = 0;

	public static ArrayList lists = new ArrayList();

	// The number of arraylists MAX must be less that MASK
	private static final int MAX = 100;
	// MASK is used to divide the key into the two indexes.
	private static final int MASK = 256;

	static {
		int i = 0;
		while (i != MAX) {
			lists.add(new ArrayList());
			i++;
		}

	}

	/**
	 * Stote an object 
	 */
	public static int storeObject(Object ob) {
		// The context is often null so return a key that can be decoded
		// quickly. This coresponding to a "no context send"
		if (ob == null) {
			return -1;
		}

		int i = 0;
		ArrayList ar;

		// pick the next array list to use
		synchronized (lists) {
			counter++;
			if (counter == MAX) {
				counter = 0;
			}
			i = counter;
		}

		ar = (ArrayList) lists.get(i);

		// add the object at position j.
		int j;
		synchronized (ar) {
			ar.add(ob);
			j = ar.size() - 1;
		}

		i = j * MASK + i;
		return i;
	}


	/**
	 * Get an object from the store
	 *
	 */
	public static Object getObject(int key) {
		if (key==-1) {
			return null;
		}
		int i = key % MASK;
		int j = key / MASK;
		return ((ArrayList) lists.get(i)).get(j);
	}

	/**
	 * Remove object from the arrayList. Mark
	 * empty slots in the arrayList with Boolean.FALSE.
	 *
	 */
	public static Object removeObject(int key) {
		if (key==-1) {
			return null;
		}
		Object ob;
		int i = key % MASK;
		int j = key / MASK;
		ArrayList ar = (ArrayList) lists.get(i);

		synchronized (ar) {
			ob = ar.get(j);
			ar.set(j, Boolean.FALSE);
			int k = ar.size() - 1;

			// only remove keys from the end so as not to alter
			// the index of other keys.
			while (k != -1 && (ar.get(k) == Boolean.FALSE)) {
				ar.remove(k);
				k--;
			}
		}
		return ob;
	}
}
