package org.columba.core.gui.search;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import org.columba.api.gui.frame.IFrameMediator;
import org.columba.api.plugin.IExtension;
import org.columba.api.plugin.IExtensionHandler;
import org.columba.api.plugin.IExtensionHandlerKeys;
import org.columba.api.plugin.PluginException;
import org.columba.api.plugin.PluginHandlerNotFoundException;
import org.columba.core.gui.search.api.IResultPanel;
import org.columba.core.logging.Logging;
import org.columba.core.main.MainInterface;
import org.columba.core.plugin.PluginManager;
import org.columba.core.search.api.ISearchCriteria;
import org.columba.core.search.api.ISearchManager;
import org.columba.core.search.api.ISearchProvider;

public class SearchPanel extends JPanel {

	private static final Logger LOG = Logger
			.getLogger("org.columba.core.search.gui.SearchPanel");

	private SearchBar searchBar;

	private IFrameMediator frameMediator;

	// private SearchResultView searchResultView;

	private StackedBox box;

	private Hashtable<String, ResultBox> map = new Hashtable<String, ResultBox>();

	public SearchPanel(IFrameMediator frameMediator) {
		super();

		this.frameMediator = frameMediator;

		// setBackground(UIManager.getColor("TextField.background"));
		searchBar = new SearchBar();
		// searchResultView = new SearchResultView();

		setLayout(new BorderLayout());
		//
		JPanel top = new JPanel();
		top.setLayout(new BorderLayout());

		top.add(searchBar, BorderLayout.CENTER);

		add(top, BorderLayout.NORTH);

		JPanel center = new JPanel();
		// center.setLayout(new VerticalLayout());

		center.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		center.setLayout(new BorderLayout());
		// center.setBackground(UIManager.getColor("TextField.background"));
		box = new StackedBox();
		box.setBackground(UIManager.getColor("TextField.background"));
		
		JScrollPane pane = new JScrollPane(box);
		// pane.getViewport().setBackground(UIManager.getColor("TextField.background"));
		center.add(pane, BorderLayout.CENTER);
		add(center, BorderLayout.CENTER);

		searchBar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// name of provider
				String command = e.getActionCommand();

				if (command.equals("ALL")) {
					// search over all providers
					String searchTerm = searchBar.getSearchTerm();
					ISearchManager manager = MainInterface.searchManager;

					// start a new search -> clear all previous search results
					manager.clearSearch(searchTerm);
					manager.reset();
					
					createStackedBox(searchTerm, null);

					// TODO @author fdietz: no paging used currently
					// show only first 5 results
					manager.executeSearch(searchTerm, 0, 5);
				} else {
					// search individual provider
					String searchTerm = searchBar.getSearchTerm();
					ISearchManager manager = MainInterface.searchManager;

					// start a new search -> clear all previous search results
					manager.clearSearch(searchTerm);
					manager.reset();

					createStackedBox(searchTerm, command);
					// TODO @author fdietz: no paging used currently
					// show only first 5 results
					manager.executeSearch(searchTerm, command, 0, 5);
				}
			}
		});

	}

	/**
	 * @param searchTerm
	 * @param command		can be <code>null</code>, in this case show all search providers
	 */
	private void createStackedBox(String searchTerm, String command) {
		box.removeAll();
		
		ISearchManager manager = MainInterface.searchManager;
		List<ISearchProvider> list = manager.getAllProviders();
		Iterator<ISearchProvider> it = list.iterator();
		while (it.hasNext()) {
			ISearchProvider p = it.next();
			if (p == null)
				continue;
			ISearchCriteria c = p.getCriteria(searchTerm);
			if (c == null)
				continue;
			
			if ( command != null ) {
				if ( !command.equals(p.getName())) continue;
			}
			
			IResultPanel resultPanel = getResultPanel(p.getName(), p
					.getNamespace());
			if (resultPanel == null)
				resultPanel = new GenericResultPanel(p.getName(), p
						.getNamespace());
			MainInterface.searchManager.addResultListener(resultPanel);
			ResultBox resultBox = new ResultBox(p, resultPanel);
			MainInterface.searchManager.addResultListener(resultBox);

			box.add(resultBox);

		}

		// repaint box
		validate();
		repaint();
	}

	private IResultPanel getResultPanel(String providerName,
			String providerNamespace) {
		try {
			IExtensionHandler handler = PluginManager.getInstance()
					.getExtensionHandler(
							IExtensionHandlerKeys.ORG_COLUMBA_CORE_SEARCH_UI);

			IExtension extension = handler.getExtension(providerName);
			if (extension == null)
				return null;

			IResultPanel panel = (IResultPanel) extension
					.instanciateExtension(new Object[] { providerName,
							providerNamespace });
			return panel;
		} catch (PluginHandlerNotFoundException e) {
			LOG.severe("Error while loading plugin: " + e.getMessage());
			if (Logging.DEBUG)
				e.printStackTrace();
		} catch (PluginException e) {
			LOG.severe("Error while loading plugin: " + e.getMessage());
			if (Logging.DEBUG)
				e.printStackTrace();
		}
		return null;
	}

}
