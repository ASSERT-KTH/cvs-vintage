package org.columba.addressbook.gui.search;

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

import org.columba.addressbook.facade.IDialogFacade;
import org.columba.api.exception.ServiceNotFoundException;
import org.columba.core.gui.base.DoubleClickListener;
import org.columba.core.gui.search.api.IResultPanel;
import org.columba.core.resourceloader.IconKeys;
import org.columba.core.resourceloader.ImageLoader;
import org.columba.core.search.api.IResultEvent;
import org.columba.core.search.api.ISearchResult;
import org.columba.core.services.ServiceRegistry;
import org.columba.mail.gui.message.viewer.HeaderSeparatorBorder;
import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterPipeline;
import org.jdesktop.swingx.decorator.RolloverHighlighter;

public class BasicResultPanel extends JXList implements IResultPanel {

	private ResourceBundle bundle;

	private String providerNamespace;

	private String providerName;

	private DefaultListModel listModel;

	public BasicResultPanel(String providerName, String providerNamespace) {
		super();

		this.providerName = providerName;
		this.providerNamespace = providerNamespace;

		bundle = ResourceBundle
				.getBundle("org.columba.addressbook.i18n.search");

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
					facade.openContactDialog(result.getLocation());
				} catch (ServiceNotFoundException e) {
					e.printStackTrace();
				}

			}
		});

	}

	public String getProviderName() {
		return providerName;
	}

	public String getProviderNamespace() {
		return providerNamespace;
	}

	public JComponent getView() {
		return this;
	}

	public ImageIcon getIcon() {
		return ImageLoader.getSmallIcon(IconKeys.ADDRESSBOOK);
	}

	public String getTitle(String searchTerm) {
		String result = MessageFormat.format(bundle.getString(providerName
				+ "_title"), new Object[] { searchTerm });
		return result;
	}

	public String getDescription(String searchTerm) {
		String result = MessageFormat.format(bundle.getString(providerName
				+ "_description"), new Object[] { searchTerm });
		return result;
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

		private JLabel iconLabel = new JLabel();
		
		private JLabel titleLabel = new JLabel();

		private JLabel descriptionLabel = new JLabel();

		private JPanel centerPanel;

		private Border lineBorder = new HeaderSeparatorBorder(new Color(230,
				230, 230));

		MyListCellRenderer() {
			setLayout(new BorderLayout());

			centerPanel = new JPanel();
			centerPanel.setLayout(new BorderLayout());
			
			centerPanel.add(titleLabel, BorderLayout.NORTH);
			centerPanel.add(descriptionLabel, BorderLayout.CENTER);
			add(iconLabel, BorderLayout.WEST);
			add(centerPanel, BorderLayout.CENTER);
			
			
			setBorder(BorderFactory.createCompoundBorder(lineBorder,
					BorderFactory.createEmptyBorder(2, 2, 2, 2)));
			iconLabel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
			
			centerPanel.setOpaque(false);
			setOpaque(true);

		}

		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {

			if (isSelected) {
//				setBackground(list.getSelectionBackground());
//				setForeground(list.getSelectionForeground());

			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());

			}

			ISearchResult result = (ISearchResult) value;

			titleLabel.setText(result.getTitle());
			iconLabel.setIcon(ImageLoader.getSmallIcon(IconKeys.USER));
			descriptionLabel.setText(result.getDescription());

			return this;
		}

	}

	public void reset(IResultEvent event) {
		listModel.clear();
	}

}