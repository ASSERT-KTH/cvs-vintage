//The contents of this file are subject to the Mozilla Public License Version 1.1
//(the "License"); you may not use this file except in compliance with the 
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License 
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.
package org.columba.core.util;

import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

/**
 * @author timo
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ListTools {

	private static int compare(int a, int b) {
		if (a < b) {
			return -1;
		} else if (a > b) {
			return 1;
		} else {
			return 0;
		}
	}

	/**
	 * Intersect two Lists that contain Objects that implement
	 * the Comparable Interface. The Result is in List a and sorted. 
	 * Be aware that List b gets also sorted!
	 * 
	 * @param a
	 * @param b
	 */
	public static void intersect(List a, List b) {
		ListIterator aIt, bIt;

		if (a.size() == 0) {
			return;
		}

		if (b.size() == 0) {
			a.clear();
			return;
		}

		Collections.sort(a);
		Collections.sort(b);

		aIt = a.listIterator();
		bIt = b.listIterator();

		Comparable aVal, bVal;

		aVal = (Comparable) aIt.next();
		bVal = (Comparable) bIt.next();

		boolean loop = true;
		int compareResult;

		while (loop) {
			compareResult = aVal.compareTo(bVal);
			if (compareResult < 0) { // a < b
				aIt.remove();
				if (aIt.hasNext())
					aVal = (Comparable) aIt.next();
				else {
					return;
				}
			} else if (compareResult == 0) { // a == b
				if (aIt.hasNext()) {
					aVal = (Comparable) aIt.next();
				} else {
					loop = false;
					return;
				}

				if (bIt.hasNext()) {
					bVal = (Comparable) bIt.next();
				} else {
					loop = false;
					aIt.remove();
				}
			} else { // a > b
				if (bIt.hasNext()) {
					bVal = (Comparable) bIt.next();
				} else {
					loop = false;
					aIt.remove();
				}
			}
		}

		while (aIt.hasNext()) {
			aIt.next();
			aIt.remove();
		}
	}

	/**
	 * Subtract two Lists that contain Objects that implement
	 * the Comparable Interface. The Result is in List a and sorted. 
	 * Be aware that List b gets also sorted!
	 * 
	 * @param a
	 * @param b
	 */
	public static void substract(List a, List b) {
		ListIterator aIt, bIt;

		if ((a.size() == 0) || (b.size() == 0))
			return;

		Collections.sort(a);
		Collections.sort(b);

		aIt = a.listIterator();
		bIt = b.listIterator();

		Comparable aVal, bVal;

		aVal = (Comparable) aIt.next();
		bVal = (Comparable) bIt.next();

		boolean loop = true;
		int compareResult;

		while (loop) {
			compareResult = aVal.compareTo(bVal);
			if (compareResult < 0) { // a < b
				if (aIt.hasNext()) {
					aVal = (Comparable) aIt.next();
				} else {
					return;
				}
			} else if (compareResult == 0) { // a == b
				aIt.remove();
				if (aIt.hasNext())
					aVal = (Comparable) aIt.next();
				else {
					return;
				}

				if (bIt.hasNext())
					bVal = (Comparable) bIt.next();
				else {
					return;
				}
			} else { // a > b
				if (bIt.hasNext()) {
					bVal = (Comparable) bIt.next();
				} else {
					return;
				}
			}

		}

	}

}
