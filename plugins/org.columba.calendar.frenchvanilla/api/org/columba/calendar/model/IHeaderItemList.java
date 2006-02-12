package org.columba.calendar.model;


import java.util.Iterator;

/**
 * List of <code>IHeaderItem</code>
 * 
 * @author fdietz
 */
public interface IHeaderItemList {

	void add(IHeaderItem item);

	IHeaderItem get(int index);

	void remove(int index);

	void remove(IHeaderItem item);

	int count();

	void clear();

	Iterator iterator();

}
