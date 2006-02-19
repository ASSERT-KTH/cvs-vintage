package org.columba.calendar.model.api;

import java.util.Iterator;

/**
 * List of <code>IHeaderItem</code>
 * 
 * @author fdietz
 */
public interface IComponentInfoList {

	public abstract void add(IComponentInfo item);

	public abstract IComponentInfo get(int index);

	public abstract void remove(int index);

	public abstract void remove(IComponentInfo item);

	public abstract int count();

	public abstract void clear();

	public abstract Iterator<IComponentInfo> iterator();

}
