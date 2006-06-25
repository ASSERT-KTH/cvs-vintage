package org.columba.core.search;

import javax.swing.ImageIcon;

import org.columba.core.search.api.ISearchCriteria;

public class SearchCriteria implements ISearchCriteria {

	private String name;
	private String description;
	private ImageIcon icon;
	public SearchCriteria(String name, String description,ImageIcon icon) {
		this.name = name;
		this.description = description;
		this.icon = icon;
	}

	public String getTitle() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public ImageIcon getIcon() {
		return icon;
	}

}
