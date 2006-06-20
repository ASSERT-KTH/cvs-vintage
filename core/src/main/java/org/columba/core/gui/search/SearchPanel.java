package org.columba.core.gui.search;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.columba.api.gui.frame.IFrameMediator;
import org.columba.core.main.MainInterface;
import org.columba.core.search.api.ISearchManager;

public class SearchPanel extends JPanel {

	private SearchBar searchBar;

	private IFrameMediator frameMediator;

	
	private SearchResultView searchResultView;
	
	public SearchPanel(IFrameMediator frameMediator) {
		super();

		this.frameMediator = frameMediator;

		searchBar = new SearchBar();
		searchResultView = new SearchResultView();
		
		setLayout(new BorderLayout());

		JPanel top = new JPanel();
		top.setLayout(new BorderLayout());

		top.add(searchBar, BorderLayout.CENTER);

		add(top, BorderLayout.NORTH);

		JPanel center = new JPanel();
		center.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
		center.setLayout(new BorderLayout());
		center.add(searchResultView, BorderLayout.CENTER);
		add(center, BorderLayout.CENTER);

		searchBar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String searchTerm = searchBar.getSearchTerm();
				ISearchManager manager = MainInterface.searchManager;
				
				// start a new search -> clear all previous search results
				manager.clearSearch(searchTerm);
				
				// TODO @author fdietz: no paging used currently 
				// show only first 20 results
				manager.executeSearch(searchTerm, 0, 20);
			}
		});
		
		// add interested on search result
		MainInterface.searchManager.addResultListener(searchResultView);
	}

}
