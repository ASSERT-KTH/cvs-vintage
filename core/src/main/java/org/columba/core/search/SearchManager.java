package org.columba.core.search;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;

import org.columba.api.command.ICommandReference;
import org.columba.api.command.IWorkerStatusController;
import org.columba.core.command.Command;
import org.columba.core.command.CommandProcessor;
import org.columba.core.search.api.IResultEvent;
import org.columba.core.search.api.IResultListener;
import org.columba.core.search.api.ISearchCriteria;
import org.columba.core.search.api.ISearchManager;
import org.columba.core.search.api.ISearchProvider;
import org.columba.core.search.api.ISearchResult;

public class SearchManager implements ISearchManager {

	private Hashtable<String, ISearchProvider> map = new Hashtable<String, ISearchProvider>();

	protected EventListenerList listenerList = new EventListenerList();

	public SearchManager() {
		register(new ExampleSearchProvider());
	}

	public void register(ISearchProvider provider) {
		map.put(provider.getTechname(), provider);
	}

	public void unregister(ISearchProvider provider) {
		map.remove(provider.getTechname());
	}

	public void executeSearch(String searchTerm) {
		// start new search
		fireClearSearch();

		// fire up search command
		Command command = new SearchCommand(new SearchCommandReference(
				searchTerm));
		CommandProcessor.getInstance().addOp(command);
	}

	public List<ISearchProvider> getAllProviders() {
		Vector<ISearchProvider> v = new Vector<ISearchProvider>();
		v.addAll(map.values());
		return v;
	}

	/**
	 * Propagates an event to all registered listeners notifying them of a item
	 * addition.
	 */
	private void fireNewResultArrived(String searchTerm,
			ISearchCriteria criteria, List<ISearchResult> result) {

		IResultEvent e = new ResultEvent(this, searchTerm, criteria, result);
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == IResultListener.class) {
				((IResultListener) listeners[i + 1]).resultArrived(e);
			}
		}
	}

	/**
	 * Propagates an event to all registered listeners
	 */
	private void fireClearSearch() {

		IResultEvent e = new ResultEvent(this);
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == IResultListener.class) {
				((IResultListener) listeners[i + 1]).clearSearch(e);
			}
		}
	}

	public void addResultListener(IResultListener l) {
		listenerList.add(IResultListener.class, l);

	}

	public void removeResultListener(IResultListener l) {
		listenerList.remove(IResultListener.class, l);

	}

	class SearchCommand extends Command {

		public SearchCommand(SearchCommandReference reference) {
			super(reference);
		}

		@Override
		public void execute(IWorkerStatusController worker) throws Exception {
			final SearchCommandReference ref = (SearchCommandReference) getReference();

			Enumeration<ISearchProvider> e = map.elements();
			while (e.hasMoreElements()) {
				final ISearchProvider p = e.nextElement();

				final List<ISearchResult> list = p.query(ref.getSearchTerm());
				// notify all listeners that new search results arrived

				// ensure this is called in the EDT
				Runnable run = new Runnable() {
					public void run() {
						fireNewResultArrived(ref.getSearchTerm(), p
								.getCriteria(ref.getSearchTerm()), list);
					}
				};
				SwingUtilities.invokeLater(run);

			}
		}

	}

	public class SearchCommandReference implements ICommandReference {

		private String searchTerm;

		public SearchCommandReference(String searchTerm) {
			super();

			this.searchTerm = searchTerm;
		}

		public boolean tryToGetLock(Object locker) {
			return true;
		}

		public void releaseLock(Object locker) {
		}

		public String getSearchTerm() {
			return searchTerm;
		}

	}
}
