package org.columba.core.search;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;

import org.columba.api.command.ICommandReference;
import org.columba.api.command.IWorkerStatusController;
import org.columba.api.plugin.IExtension;
import org.columba.api.plugin.IExtensionHandler;
import org.columba.api.plugin.IExtensionHandlerKeys;
import org.columba.api.plugin.PluginException;
import org.columba.api.plugin.PluginHandlerNotFoundException;
import org.columba.core.command.Command;
import org.columba.core.command.CommandProcessor;
import org.columba.core.logging.Logging;
import org.columba.core.plugin.PluginManager;
import org.columba.core.search.api.IResultEvent;
import org.columba.core.search.api.IResultListener;
import org.columba.core.search.api.ISearchCriteria;
import org.columba.core.search.api.ISearchManager;
import org.columba.core.search.api.ISearchProvider;
import org.columba.core.search.api.ISearchResult;

public class SearchManager implements ISearchManager {

	private static final Logger LOG = Logger
			.getLogger("org.columba.core.search.SearchManager");

	protected EventListenerList listenerList = new EventListenerList();

	/**
	 * command hashtable used for paging to call the same command several times
	 * for a given <code>startIndex</code> and <code>resultCount</code>
	 */
	private Map<String, Command> commandMap = new Hashtable<String, Command>();

	public SearchManager() {
		// ensure map can be used by multiple threads
		commandMap = Collections.synchronizedMap(commandMap);

	}

	/**
	 * @see org.columba.core.search.api.ISearchManager#executeSearch(java.lang.String,
	 *      int, int)
	 */
	public void executeSearch(String searchTerm, int startIndex, int resultCount) {
		if (searchTerm == null)
			throw new IllegalArgumentException("searchTerm == null");
		if (startIndex < 0)
			throw new IllegalArgumentException("startIndex must be >= 0");
		if (resultCount <= 0)
			throw new IllegalArgumentException("resultCount must be > 0");

		Command command = null;
		if (commandMap.containsKey(searchTerm))
			command = commandMap.get(searchTerm);
		else {
			command = new SearchCommand(new SearchCommandReference(searchTerm,
					startIndex, resultCount));
			// store command for later reuse with different startIndex and/or
			// resultCount
			commandMap.put(searchTerm, command);
		}

		// fire up search command
		CommandProcessor.getInstance().addOp(command);
	}

	/**
	 * @see org.columba.core.search.api.ISearchManager#executeSearch(java.lang.String,
	 *      java.lang.String, int, int)
	 */
	public void executeSearch(String searchTerm, String providerName,
			int startIndex, int resultCount) {
		if (searchTerm == null)
			throw new IllegalArgumentException("searchTerm == null");
		if (providerName == null)
			throw new IllegalArgumentException("providerName == null");
		if (startIndex < 0)
			throw new IllegalArgumentException("startIndex must be >= 0");
		if (resultCount <= 0)
			throw new IllegalArgumentException("resultCount must be > 0");

		Command command = null;
		if (commandMap.containsKey(searchTerm))
			command = commandMap.get(searchTerm);
		else {
			command = new SearchCommand(new SearchCommandReference(searchTerm,
					providerName, startIndex, resultCount));
			// store command for later reuse with different startIndex and/or
			// resultCount
			commandMap.put(searchTerm, command);
		}

		// fire up search command
		CommandProcessor.getInstance().addOp(command);
	}

	public void executeSearch(String searchTerm, String providerName,
			String criteriaName, int startIndex, int resultCount) {
		if (searchTerm == null)
			throw new IllegalArgumentException("searchTerm == null");
		if (providerName == null)
			throw new IllegalArgumentException("providerName == null");
		if (criteriaName == null)
			throw new IllegalArgumentException("criteriaName == null");
		if (startIndex < 0)
			throw new IllegalArgumentException("startIndex must be >= 0");
		if (resultCount <= 0)
			throw new IllegalArgumentException("resultCount must be > 0");

		Command command = null;
		if (commandMap.containsKey(searchTerm))
			command = commandMap.get(searchTerm);
		else {
			command = new SearchCommand(new SearchCommandReference(searchTerm,
					providerName, criteriaName, startIndex, resultCount));
			// store command for later reuse with different startIndex and/or
			// resultCount
			commandMap.put(searchTerm, command);
		}

		// fire up search command
		CommandProcessor.getInstance().addOp(command);
	}

	/**
	 * @see org.columba.core.search.api.ISearchManager#getAllProviders()
	 */
	public List<ISearchProvider> getAllProviders() {
		List<ISearchProvider> list = createProviderList();
		return list;
	}

	/**
	 * @see org.columba.core.search.api.ISearchManager#clearSearch(java.lang.String)
	 */
	public void clearSearch(String searchTerm) {
		// we assume user cancelled search
		// -> remove cached command
		if (commandMap.containsKey(searchTerm))
			commandMap.remove(searchTerm);

		fireClearSearch(searchTerm);
	}

	public void reset() {
		fireReset();
	}

	/**
	 * Propagates an event to all registered listeners notifying them of a item
	 * addition.
	 */
	private void fireNewResultArrived(String searchTerm,
			String providerTechnicalName, ISearchCriteria criteria,
			List<ISearchResult> result, int totalResultCount) {

		IResultEvent e = new ResultEvent(this, searchTerm,
				providerTechnicalName, criteria, result, totalResultCount);
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
	private void fireClearSearch(String searchTerm) {

		IResultEvent e = new ResultEvent(this, searchTerm);
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

	private void fireReset() {

		IResultEvent e = new ResultEvent(this);
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == IResultListener.class) {
				((IResultListener) listeners[i + 1]).reset(e);
			}
		}
	}

	/**
	 * @see org.columba.core.search.api.ISearchManager#addResultListener(org.columba.core.search.api.IResultListener)
	 */
	public void addResultListener(IResultListener l) {
		listenerList.add(IResultListener.class, l);

	}

	/**
	 * @see org.columba.core.search.api.ISearchManager#removeResultListener(org.columba.core.search.api.IResultListener)
	 */
	public void removeResultListener(IResultListener l) {
		listenerList.remove(IResultListener.class, l);

	}

	private List<ISearchProvider> createProviderList() {
		List<ISearchProvider> list = new Vector<ISearchProvider>();
		try {

			IExtensionHandler handler = PluginManager.getInstance()
					.getExtensionHandler(
							IExtensionHandlerKeys.ORG_COLUMBA_CORE_SEARCH);

			String[] ids = handler.getPluginIdList();
			for (int i = 0; i < ids.length; i++) {
				try {
					IExtension extension = handler.getExtension(ids[i]);

					ISearchProvider provider = (ISearchProvider) extension
							.instanciateExtension(null);
					list.add(provider);
				} catch (PluginException e) {
					LOG.severe("Error while loading plugin: " + e.getMessage());
					if (Logging.DEBUG)
						e.printStackTrace();
				}
			}

		} catch (PluginHandlerNotFoundException e) {
			LOG.severe("Error while loading plugin: " + e.getMessage());
			if (Logging.DEBUG)
				e.printStackTrace();
		}

		return list;
	}

	/**
	 * Command executes the search.
	 * <p>
	 * In case new result results arrive, it ensures that all interested
	 * listeners are notified from inside the EDT.
	 * <p>
	 * FIXME: fdietz: No locking of folders currently implemented!
	 * 
	 * @author fdietz
	 */
	class SearchCommand extends Command {

		public SearchCommand(SearchCommandReference reference) {
			super(reference);
		}

		@Override
		public void execute(IWorkerStatusController worker) throws Exception {
			final SearchCommandReference ref = (SearchCommandReference) getReference();

			String providerTechnicalName = ref.getProviderTechnicalName();

			// create list of all registered providers
			List<ISearchProvider> list = createProviderList();

			Iterator<ISearchProvider> it = list.iterator();
			while (it.hasNext()) {
				final ISearchProvider p = it.next();

				// if providerName specified
				// -> skip if this isn't the matching provider
				if (providerTechnicalName != null) {
					if (!providerTechnicalName.equals(p.getTechnicalName()))
						continue;
				}

				// keep search history
				//SearchHistoryList.getInstance().add(ref.getSearchTerm(), p, c);

				if (ref.getSearchCriteriaTechnicalName() == null) {
					// query using all criteria
					Iterator<ISearchCriteria> it2 = p.getAllCriteria(
							ref.getSearchTerm()).iterator();
					while (it2.hasNext()) {
						ISearchCriteria c = it2.next();
						String searchCriteriaTechnicalName = c
								.getTechnicalName();
						// execute search
						doExecute(ref, p, searchCriteriaTechnicalName);
					}
				} else {
					// query only a single criteria

					// execute search
					doExecute(ref, p, ref.getSearchCriteriaTechnicalName());
				}

			}
		}

		private void doExecute(final SearchCommandReference ref,
				final ISearchProvider p,
				final String searchCriteriaTechnicalName) {

			// query provider
			final List<ISearchResult> resultList = p.query(ref.getSearchTerm(),
					searchCriteriaTechnicalName, ref.getStartIndex(), ref
							.getResultCount());
			// retrieve total result count
			final int totalResultCount = p.getTotalResultCount();

			// notify all listeners that new search results arrived

			// ensure this is called in the EDT
			Runnable run = new Runnable() {
				public void run() {
					fireNewResultArrived(ref.getSearchTerm(), p
							.getTechnicalName(), p.getCriteria(
							searchCriteriaTechnicalName, ref.getSearchTerm()),
							resultList, totalResultCount);
				}
			};
			SwingUtilities.invokeLater(run);
		}

	}

	/**
	 * FIXME:
	 * 
	 * @author fdietz: No locking of folders currently implemented!
	 * 
	 * @author frd
	 */
	public class SearchCommandReference implements ICommandReference {

		private String searchTerm;

		private String providerTechnicalName;

		private int startIndex;

		private int resultCount;

		private String searchCriteriaTechnicalName;

		public SearchCommandReference(String searchTerm, int startIndex,
				int resultCount) {
			super();

			this.searchTerm = searchTerm;
			this.startIndex = startIndex;
			this.resultCount = resultCount;
		}

		public SearchCommandReference(String searchTerm,
				String providerTechnicalName, int startIndex, int resultCount) {
			super();

			this.searchTerm = searchTerm;
			this.providerTechnicalName = providerTechnicalName;
			this.startIndex = startIndex;
			this.resultCount = resultCount;
		}

		public SearchCommandReference(String searchTerm,
				String providerTechnicalName,
				String searchCriteriaTechnicalName, int startIndex,
				int resultCount) {
			super();

			this.searchTerm = searchTerm;
			this.providerTechnicalName = providerTechnicalName;
			this.searchCriteriaTechnicalName = searchCriteriaTechnicalName;
			this.startIndex = startIndex;
			this.resultCount = resultCount;
		}

		public boolean tryToGetLock(Object locker) {
			return true;
		}

		public void releaseLock(Object locker) {
		}

		public String getSearchTerm() {
			return searchTerm;
		}

		public int getResultCount() {
			return resultCount;
		}

		public int getStartIndex() {
			return startIndex;
		}

		public String getProviderTechnicalName() {
			return providerTechnicalName;
		}

		public String getSearchCriteriaTechnicalName() {
			return searchCriteriaTechnicalName;
		}

	}

	/**
	 * @see org.columba.core.search.api.ISearchManager#getProvider(java.lang.String)
	 */
	public ISearchProvider getProvider(String technicalName) {
		try {

			IExtensionHandler handler = PluginManager.getInstance()
					.getExtensionHandler(
							IExtensionHandlerKeys.ORG_COLUMBA_CORE_SEARCH);

			try {
				IExtension extension = handler.getExtension(technicalName);

				ISearchProvider provider = (ISearchProvider) extension
						.instanciateExtension(null);
				return provider;
			} catch (PluginException e) {
				LOG.severe("Error while loading plugin: " + e.getMessage());
				if (Logging.DEBUG)
					e.printStackTrace();
			}

		} catch (PluginHandlerNotFoundException e) {
			LOG.severe("Error while loading plugin: " + e.getMessage());
			if (Logging.DEBUG)
				e.printStackTrace();
		}
		return null;
	}

}
