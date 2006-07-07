// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.mail.gui.search;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;

import org.columba.api.exception.ServiceNotFoundException;
import org.columba.core.gui.base.DoubleClickListener;
import org.columba.core.gui.base.EmptyIcon;
import org.columba.core.gui.search.api.IResultPanel;
import org.columba.core.search.api.IResultEvent;
import org.columba.core.search.api.ISearchResult;
import org.columba.core.services.ServiceRegistry;
import org.columba.mail.facade.IDialogFacade;
import org.columba.mail.gui.message.viewer.HeaderSeparatorBorder;
import org.columba.mail.resourceloader.IconKeys;
import org.columba.mail.resourceloader.MailImageLoader;
import org.columba.mail.search.MailSearchResult;
import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterPipeline;
import org.jdesktop.swingx.decorator.RolloverHighlighter;

/**
 * BasicResultPanel class
 * 
 * @author fdietz
 */
public class BasicResultPanel extends JXList implements IResultPanel {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -4489416716453628164L;

	private ResourceBundle bundle;

	private String providerNamespace;

	private String providerName;

	private DefaultListModel listModel;

	/**
	 * BasicResultPanel parameterised constructor
	 * 
	 * @param providerName
	 * @param providerNamespace
	 */
	public BasicResultPanel(String providerName, String providerNamespace) {
		super();

		this.providerName = providerName;
		this.providerNamespace = providerNamespace;

		bundle = ResourceBundle.getBundle("org.columba.mail.i18n.search");

		listModel = new DefaultListModel();
		setModel(listModel);
		setCellRenderer(new MyListCellRenderer());
		setBorder(null);
		setHighlighters(new HighlighterPipeline(
				new Highlighter[] { new RolloverHighlighter(new Color(248, 248,
						248), Color.white) }));
		setRolloverEnabled(true);

		addMouseListener(new DoubleClickListener() {

			@Override
			public void doubleClick(MouseEvent event) {
				ISearchResult result = (ISearchResult) getSelectedValue();

				try {
					IDialogFacade facade = (IDialogFacade) ServiceRegistry
							.getInstance().getService(IDialogFacade.class);
					facade.openMessage(result.getLocation());
				} catch (ServiceNotFoundException e) {
					e.printStackTrace();
				}
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.columba.core.gui.search.api.IResultPanel#getProviderName()
	 */
	public String getProviderName() {
		return providerName;
	}

	/* (non-Javadoc)
	 * @see org.columba.core.gui.search.api.IResultPanel#getProviderNamespace()
	 */
	public String getProviderNamespace() {
		return providerNamespace;
	}

	/* (non-Javadoc)
	 * @see org.columba.core.gui.search.api.IResultPanel#getView()
	 */
	public JComponent getView() {
		return this;
	}

	/**
	 * @return small icon
	 */
	public ImageIcon getIcon() {
		return MailImageLoader.getSmallIcon(IconKeys.MESSAGE_READ);
	}

	/**
	 * @param searchTerm
	 * @return search result title
	 */
	public String getTitle(String searchTerm) {
		String result = MessageFormat.format(bundle.getString(providerName
				+ "_title"), new Object[] { searchTerm });
		return result;
	}

	/**
	 * @param searchTerm
	 * @return search result description
	 */
	public String getDescription(String searchTerm) {
		String result = MessageFormat.format(bundle.getString(providerName
				+ "_description"), new Object[] { searchTerm });
		return result;
	}

	/* (non-Javadoc)
	 * @see org.columba.core.search.api.IResultListener#resultArrived(org.columba.core.search.api.IResultEvent)
	 */
	public void resultArrived(IResultEvent event) {
		if (!event.getProviderName().equals(this.providerName))
			return;

		List<ISearchResult> result = event.getSearchResults();

		Iterator<ISearchResult> it = result.iterator();
		while (it.hasNext()) {
			listModel.addElement(it.next());
		}

		// setPreferredSize(list.getPreferredSize());
		revalidate();
	}

	/* (non-Javadoc)
	 * @see org.columba.core.search.api.IResultListener#clearSearch(org.columba.core.search.api.IResultEvent)
	 */
	public void clearSearch(IResultEvent event) {
		listModel.clear();
	}

	/**
	 * MyListCellRenderer class
	 * @author fdietz
	 */
	class MyListCellRenderer extends JPanel implements ListCellRenderer {

		/**
		 * serialVersionUID
		 */
		private static final long serialVersionUID = -262733555786698831L;

		private JPanel centerPanel;

		private JPanel topPanel;

		private Border lineBorder = new HeaderSeparatorBorder(new Color(230,
				230, 230));

		private JLabel statusLabel = new JLabel();

		private JLabel fromLabel = new JLabel();

		private JLabel dateLabel = new JLabel();

		private JLabel subjectLabel = new JLabel();

		private JLabel flagLabel = new JLabel();

		private ImageIcon flagIcon = MailImageLoader.getSmallIcon("flag.png");

		MyListCellRenderer() {
			setLayout(new BorderLayout());

			topPanel = new JPanel();
			topPanel.setLayout(new BorderLayout());
			topPanel.add(fromLabel, BorderLayout.CENTER);
			topPanel.add(dateLabel, BorderLayout.EAST);

			centerPanel = new JPanel();
			centerPanel.setLayout(new BorderLayout());
			centerPanel.add(topPanel, BorderLayout.NORTH);
			centerPanel.add(subjectLabel, BorderLayout.CENTER);

			add(statusLabel, BorderLayout.WEST);
			add(centerPanel, BorderLayout.CENTER);
			add(flagLabel, BorderLayout.EAST);

			setBorder(BorderFactory.createCompoundBorder(lineBorder,
					BorderFactory.createEmptyBorder(2, 2, 2, 2)));

			statusLabel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
			flagLabel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

			topPanel.setOpaque(false);
			centerPanel.setOpaque(false);
			setOpaque(true);
		}

		/* (non-Javadoc)
		 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
		 */
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {

			if (isSelected) {
				// setBackground(list.getSelectionBackground());
				// setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}

			MailSearchResult result = (MailSearchResult) value;

			statusLabel.setIcon(result.getStatusIcon());
			subjectLabel.setText(result.getTitle());
			fromLabel.setText(result.getFrom().getShortAddress());
			dateLabel.setText(result.getDate());

			if (result.isFlagged())
				flagLabel.setIcon(flagIcon);
			else
				flagLabel.setIcon(new EmptyIcon());

			return this;
		}
	}

	/* (non-Javadoc)
	 * @see org.columba.core.search.api.IResultListener#reset(org.columba.core.search.api.IResultEvent)
	 */
	public void reset(IResultEvent event) {
		listModel.clear();
	}
}
