package org.columba.core.search.api;

import java.util.EventListener;

public interface IResultListener extends EventListener{

	public void resultArrived(IResultEvent event);
	public void clearSearch(IResultEvent event);
	public void reset(IResultEvent event);
}
