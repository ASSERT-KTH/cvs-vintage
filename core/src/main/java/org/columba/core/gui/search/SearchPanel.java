package org.columba.core.gui.search;

import java.awt.BorderLayout;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import org.columba.api.gui.frame.IDock;
import org.columba.api.gui.frame.IFrameMediator;
import org.columba.core.gui.search.api.IResultPanel;
import org.columba.core.gui.search.api.ISearchPanel;
import org.columba.core.main.MainInterface;
import org.columba.core.resourceloader.IconKeys;
import org.columba.core.resourceloader.ImageLoader;
import org.columba.core.search.api.ISearchCriteria;
import org.columba.core.search.api.ISearchManager;
import org.columba.core.search.api.ISearchProvider;

public class SearchPanel extends JPanel implements ISearchPanel {

	private static final Logger LOG = Logger
			.getLogger("org.columba.core.search.gui.SearchPanel");

	private IFrameMediator frameMediator;

	// private SearchResultView searchResultView;

	private StackedBox box;

	private IconTextField textField;

	private ImageIcon icon = ImageLoader.getSmallIcon(IconKeys.EDIT_FIND);

	private JButton button;

	private Hashtable<String, ResultBox> map = new Hashtable<String, ResultBox>();

	private SearchBar searchBar;

	public SearchPanel(IFrameMediator frameMediator) {
		super();

		this.frameMediator = frameMediator;

		searchBar = new SearchBar(this, true);

		setLayout(new BorderLayout());
		//
		JPanel top = new JPanel();
		top.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
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

	}

	// search individual provider and individual criteria
	public void searchInCriteria(String searchTerm, String providerName,
			String criteriaName) {

		showSearchDockingView();
		
		ISearchManager manager = MainInterface.searchManager;

		// start a new search -> clear all previous search results
		manager.clearSearch(searchTerm);
		manager.reset();

		createStackedBox(searchTerm, providerName, criteriaName);

		// TODO @author fdietz: no paging used currently
		// show only first 5 results
		manager.executeSearch(searchTerm, providerName, criteriaName, 0, 5);
	}

	// search individual provider
	public void searchInProvider(String searchTerm, String providerName) {

		showSearchDockingView();
		
		ISearchManager manager = MainInterface.searchManager;

		// start a new search -> clear all previous search results
		manager.clearSearch(searchTerm);
		manager.reset();

		createStackedBox(searchTerm, providerName, null);

		// TODO @author fdietz: no paging used currently
		// show only first 5 results
		manager.executeSearch(searchTerm, providerName, 0, 5);
	}

	// search across all providers
	public void searchAll(String searchTerm) {

		showSearchDockingView();
		
		ISearchManager manager = MainInterface.searchManager;

		// start a new search -> clear all previous search results
		manager.clearSearch(searchTerm);
		manager.reset();

		createStackedBox(searchTerm, null, null);

		// TODO @author fdietz: no paging used currently
		// show only first 5 results
		manager.executeSearch(searchTerm, 0, 5);
	}

	// create new stacked box
	private void createStackedBox(String searchTerm, String providerName,
			String criteriaName) {
		if (searchTerm == null)
			throw new IllegalArgumentException("searchTerm == null");

		box.removeAll();

		// search across all providers
		boolean providerAll = (providerName == null) ? true : false;
		// search all criteria in specific provider only
		boolean providerSearch = (providerName != null) ? true : false;
		// search in specific criteria
		boolean criteriaSearch = (criteriaName != null && providerName != null) ? true
				: false;

		ISearchManager manager = MainInterface.searchManager;

		if (criteriaSearch) {
			// query with only a single criteria

			ISearchProvider p = manager.getProvider(providerName);

			ISearchCriteria c = p.getCriteria(criteriaName, searchTerm);

			createResultPanel(p, c);

		} else if (providerSearch) {

			// query only a single provider

			ISearchProvider p = manager.getProvider(providerName);

			Iterator<ISearchCriteria> it2 = p.getAllCriteria(searchTerm)
					.iterator();
			while (it2.hasNext()) {
				ISearchCriteria c = it2.next();
				createResultPanel(p, c);
			}

		} else if (providerAll) {
			// query all criteria of all providers

			Iterator<ISearchProvider> it = manager.getAllProviders().iterator();
			while (it.hasNext()) {
				ISearchProvider p = it.next();
				if (p == null)
					continue;

				Iterator<ISearchCriteria> it2 = p.getAllCriteria(searchTerm)
						.iterator();
				while (it2.hasNext()) {
					ISearchCriteria c = it2.next();
					createResultPanel(p, c);
				}
			}
		}

		// repaint box
		validate();
		repaint();
	}

	private void createResultPanel(ISearchProvider p, ISearchCriteria c) {
		// retrieve result panel for search criteria
		// IResultPanel resultPanel =
		// loadResultPanelExtension(p.getTechnicalName(),
		// c.getTechnicalName());

		IResultPanel resultPanel = p.getResultPanel(c.getTechnicalName());

		// fall-back to default result panel (html based viewer)
		if (resultPanel == null)
			resultPanel = new GenericResultPanel(p.getTechnicalName(), c
					.getTechnicalName());

		// add result panel as listener for new search results
		MainInterface.searchManager.addResultListener(resultPanel);

		// create visual container for result panel
		ResultBox resultBox = new ResultBox(c, resultPanel);
		MainInterface.searchManager.addResultListener(resultBox);

		// add to search panel
		box.add(resultBox);
	}

	// show search docking view
	private void showSearchDockingView() {
		if (frameMediator instanceof IDock) {
			// show docking view
			((IDock) frameMediator).showDockable(IDock.DOCKING_VIEW_SEARCH);
		}

	}

	/**
	 * @see org.columba.core.gui.search.api.ISearchPanel#getView()
	 */
	public JComponent getView() {
		return this;
	}

	// private IResultPanel loadResultPanelExtension(String
	// providerTechnicalName,
	// String searchCriteriaTechnicalName) {
	// try {
	// IExtensionHandler handler = PluginManager.getInstance()
	// .getExtensionHandler(
	// IExtensionHandlerKeys.ORG_COLUMBA_CORE_SEARCH_UI);
	//
	// IExtension extension = handler
	// .getExtension(searchCriteriaTechnicalName);
	// if (extension == null)
	// return null;
	//
	// IResultPanel panel = (IResultPanel) extension
	// .instanciateExtension(new Object[] {
	// searchCriteriaTechnicalName, providerTechnicalName });
	// return panel;
	// } catch (PluginHandlerNotFoundException e) {
	// LOG.severe("Error while loading plugin: " + e.getMessage());
	// if (Logging.DEBUG)
	// e.printStackTrace();
	// } catch (PluginException e) {
	// LOG.severe("Error while loading plugin: " + e.getMessage());
	// if (Logging.DEBUG)
	// e.printStackTrace();
	// }
	// return null;
	// }

}
