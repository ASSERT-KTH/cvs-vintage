///The contents of this file are subject to the Mozilla Public License Version 1.1
//(the "License"); you may not use this file except in compliance with the 
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License 
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.
package org.columba.mail.gui.config.pop3preprocessor;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;

import org.columba.core.config.Config;
import org.columba.core.main.MainInterface;
import org.columba.core.xml.XmlElement;
import org.columba.mail.gui.config.filter.util.FilterHeaderRenderer;
import org.columba.mail.gui.config.filter.util.StringFilterRenderer;
import org.columba.mail.util.MailResourceLoader;

class FilterListTable extends JTable {
	private FilterListTableModel model;
	private Config config;
	private XmlElement filterList;

	public FilterListTable(XmlElement filterList, ConfigFrame frame) {
		super();
		this.filterList = filterList;
		config = MainInterface.config;

		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		model = new FilterListTableModel(filterList);
		//update();

		setModel(model);

		setShowGrid(false);
		setIntercellSpacing(new java.awt.Dimension(0, 0));

		TableColumn tc =
			getColumn(
				MailResourceLoader.getString(
					"dialog",
					"filter",
					"enabled_tableheader"));
		//tc.setCellRenderer( new BooleanFilterRenderer() );
		tc.setHeaderRenderer(
			new FilterHeaderRenderer(
				MailResourceLoader.getString(
					"dialog",
					"filter",
					"enabled_tableheader")));
		tc.setMaxWidth(80);
		tc.setMinWidth(80);

		tc =
			getColumn(
				"Name");
		tc.setCellRenderer(new StringFilterRenderer());
		tc.setHeaderRenderer(
			new FilterHeaderRenderer(
				"Name"));

		sizeColumnsToFit(AUTO_RESIZE_NEXT_COLUMN);
	}

	public void update() {
		model.fireTableDataChanged();

	}

}
