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

	
	public static void intersect(List a, List b) {
	ListIterator aIt, bIt;

	if( a.size() == 0) {
		return;
	}

	if( b.size()==0 ) {
		a.clear();
		return;
	}

	Collections.sort(a);
	Collections.sort(b);

	aIt = a.listIterator();
	bIt = b.listIterator();

	Integer aVal, bVal;

	aVal = (Integer) aIt.next();
	bVal = (Integer) bIt.next();

	boolean loop = true; 

	while (loop) {
		switch (compare(aVal.intValue(), bVal.intValue())) {
			case -1 :
				{ // a < b
					aIt.remove();
					if( aIt.hasNext())
						aVal = (Integer) aIt.next();
					else {
						return;
					}
					break;
				}
			case 0 :
				{ // a == b
					if (aIt.hasNext()) {
						aVal = (Integer) aIt.next();
					} else {
						loop = false;
						return;
					}

					if (bIt.hasNext()) {
						bVal = (Integer) bIt.next();
					} else {
						loop = false;
						aIt.remove();
					}
				
					break;
				}
			case 1 :
				{ // a > b
					if (bIt.hasNext()) {
						bVal = (Integer) bIt.next();
					} else {
						loop = false;
						aIt.remove();
					}
				}
		}
	}
		
	while(aIt.hasNext()) {
		aIt.next();
		aIt.remove();
	}
}




	public static void substract(List a, List b) {
		ListIterator aIt, bIt;

		if( (a.size() == 0) || (b.size()==0))
			return;

		Collections.sort(a);
		Collections.sort(b);

		aIt = a.listIterator();
		bIt = b.listIterator();

		Integer aVal, bVal;

		aVal = (Integer) aIt.next();
		bVal = (Integer) bIt.next();

		boolean loop = true; 

		while (loop) {
			switch (compare(aVal.intValue(), bVal.intValue())) {
				case -1 :
					{ // a < b
						if( aIt.hasNext()) {
							aVal = (Integer) aIt.next();
						} else {
							return;
						}
						break;
					}
				case 0 :
					{ // a == b
						aIt.remove();
						if (aIt.hasNext())
							aVal = (Integer) aIt.next();
						else {
							return;
						}

						if (bIt.hasNext())
							bVal = (Integer) bIt.next();
						else {
							return;
						}

						break;
					}
				case 1 :
					{ // a > b
						if (bIt.hasNext()) {
							bVal = (Integer) bIt.next();
						} else {
							return;
						}
					}
			}
		}

	}
	


}
