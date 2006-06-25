package org.columba.core.gui.search;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;

import org.columba.core.gui.base.DoubleClickListener;
import org.columba.core.gui.search.api.IResultPanel;
import org.columba.core.resourceloader.IconKeys;
import org.columba.core.resourceloader.ImageLoader;
import org.columba.core.search.api.IResultEvent;
import org.columba.core.search.api.ISearchResult;
import org.jdesktop.swingx.JXHyperlink;
import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.decorator.AlternateRowHighlighter;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterPipeline;

public class GenericResultPanel extends JXList implements IResultPanel {

	private String providerNamespace;

	private DefaultListModel listModel;

	private String providerName;

	public GenericResultPanel(String providerName, String providerNamespace) {
		super();

		this.providerName = providerName;
		this.providerNamespace = providerNamespace;

		listModel = new DefaultListModel();
		setModel(listModel);
		setCellRenderer(new MyListCellRenderer());
		setBorder(null);
		setHighlighters(new HighlighterPipeline(
				new Highlighter[] { AlternateRowHighlighter.genericGrey }));

		addMouseListener(new DoubleClickListener() {

			@Override
			public void doubleClick(MouseEvent event) {
				// do something with link
			}
		});
	}
	
	public String getProviderName() {
		return providerName;
	}

	public String getProviderNamespace() {
		return providerNamespace;
	}

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

	public void clearSearch(IResultEvent event) {
		listModel.clear();
	}

	class MyListCellRenderer extends JPanel implements ListCellRenderer {
		// private JLabel iconLabel = new JLabel();

		private JXHyperlink titleLabel = new JXHyperlink();

		private JLabel descriptionLabel = new JLabel();

		private JPanel topPanel;

		MyListCellRenderer() {
			setLayout(new BorderLayout());

			titleLabel.setClickedColor(UIManager.getColor("Label.foreground"));
			titleLabel
					.setUnclickedColor(UIManager.getColor("Label.foreground"));
			// titleLabel.setBackground(UIManager.getColor("Label.background"));
			titleLabel.setOpaque(false);
			titleLabel.setBorderPainted(true);

			topPanel = new JPanel();
			topPanel.setLayout(new BorderLayout());

			// topPanel.add(iconLabel, BorderLayout.WEST);
			topPanel.add(titleLabel, BorderLayout.CENTER);

			add(topPanel, BorderLayout.NORTH);
			add(descriptionLabel, BorderLayout.CENTER);

			topPanel.setOpaque(true);
			setOpaque(true);

			setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		}

		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {

			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());

				topPanel.setBackground(list.getSelectionBackground());
				topPanel.setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
				topPanel.setBackground(list.getBackground());
				topPanel.setForeground(list.getForeground());
			}

			ISearchResult result = (ISearchResult) value;

			titleLabel.setText(result.getTitle());

			ImageIcon icon = null;
			if (providerNamespace.equals("org.columba.contact"))
				icon = ImageLoader.getSmallIcon(IconKeys.USER);
			else if (providerNamespace.equals("org.columba.mail"))
				icon = ImageLoader.getSmallIcon(IconKeys.COMPUTER);

			titleLabel.setIcon(icon);
			descriptionLabel.setText(result.getDescription());

			return this;
		}

	}

	public JComponent getView() {
		return this;
	}

	public ImageIcon getIcon() {
		return null;
	}

	public String getTitle(String searchTerm) {
		return providerName;
	}

	public String getDescription(String searchTerm) {
		return providerNamespace;
	}

	public void reset(IResultEvent event) {
		listModel.clear();
	}
}
