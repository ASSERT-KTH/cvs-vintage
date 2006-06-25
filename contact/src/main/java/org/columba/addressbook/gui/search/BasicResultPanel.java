package org.columba.addressbook.gui.search;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.AbstractBorder;
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
import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterPipeline;
import org.jdesktop.swingx.decorator.RolloverHighlighter;

public class BasicResultPanel extends JXList implements IResultPanel {

	

	private String providerNamespace;

	private String providerName;

	private DefaultListModel listModel;

	public BasicResultPanel(String providerName, String providerNamespace) {
		super();

		this.providerName = providerName;
		this.providerNamespace = providerNamespace;


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

	class HeaderSeparatorBorder extends AbstractBorder {

		protected Color color;
		
		public HeaderSeparatorBorder(Color color) {
			super();
			
			this.color = color;
		}

		/**
		 * Paints the border for the specified component with the specified position
		 * and size.
		 * 
		 * @param c
		 *            the component for which this border is being painted
		 * @param g
		 *            the paint graphics
		 * @param x
		 *            the x position of the painted border
		 * @param y
		 *            the y position of the painted border
		 * @param width
		 *            the width of the painted border
		 * @param height
		 *            the height of the painted border
		 */
		public void paintBorder(Component c, Graphics g, int x, int y, int width,
				int height) {
			Color oldColor = g.getColor();
			g.setColor(color);
			g.drawLine(x,y+height-1, x+width-1, y+height-1);
			
			g.setColor(oldColor);
		}

		/**
		 * Returns the insets of the border.
		 * 
		 * @param c
		 *            the component for which this border insets value applies
		 */
		public Insets getBorderInsets(Component c) {
			return new Insets(0, 0, 1, 0);
		}

		/**
		 * Reinitialize the insets parameter with this Border's current Insets.
		 * 
		 * @param c
		 *            the component for which this border insets value applies
		 * @param insets
		 *            the object to be reinitialized
		 */
		public Insets getBorderInsets(Component c, Insets insets) {
			insets.left = insets.top = insets.right = insets.bottom = 1;
			return insets;
		}
		
	}
	
}