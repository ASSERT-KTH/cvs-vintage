package org.columba.core.gui.search;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

import org.columba.core.gui.search.api.IResultPanel;
import org.columba.core.search.api.IResultEvent;
import org.columba.core.search.api.IResultListener;
import org.columba.core.search.api.ISearchCriteria;
import org.columba.core.search.api.ISearchProvider;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXHyperlink;

public class ResultBox extends JPanel implements IResultListener {

	private final static Color titleBackground = new Color(248, 248, 248);

	private final static Color borderColor = new Color(230, 230, 230);

	private JXHyperlink link;

	private JXHyperlink moreLink;

	private JXCollapsiblePane collapsible;

	private IResultPanel resultPanel;

	private ISearchProvider searchProvider;

	public ResultBox(ISearchProvider searchProvider, IResultPanel resultPanel) {
		this.resultPanel = resultPanel;
		this.searchProvider = searchProvider;

		collapsible = new JXCollapsiblePane();
		collapsible.getContentPane().setBackground(Color.WHITE);
		collapsible.add(resultPanel.getView());

		Action toggleAction = collapsible.getActionMap().get(
				JXCollapsiblePane.TOGGLE_ACTION);
		// use the collapse/expand icons from the JTree UI
		toggleAction.putValue(JXCollapsiblePane.COLLAPSE_ICON, UIManager
				.getIcon("Tree.expandedIcon"));
		toggleAction.putValue(JXCollapsiblePane.EXPAND_ICON, UIManager
				.getIcon("Tree.collapsedIcon"));
		link = new JXHyperlink(toggleAction);
		ISearchCriteria criteria = searchProvider.getCriteria("");
		link.setText(criteria.getTitle());
		link.setToolTipText(criteria.getDescription());

		// link.setFont(link.getFont().deriveFont(Font.BOLD));
		link.setOpaque(true);
		link.setBackground(titleBackground);
		link.setFocusPainted(false);

		link.setUnclickedColor(UIManager.getColor("Label.foreground"));
		link.setClickedColor(UIManager.getColor("Label.foreground"));

		moreLink = new JXHyperlink();
		moreLink.setEnabled(false);
		moreLink.setText("Show More ..");
		Font font = UIManager.getFont("Label.font");
		Font smallFont = new Font(font.getName(), font.getStyle(), font
				.getSize() - 2);
		moreLink.setFont(smallFont);
		moreLink.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane
						.showMessageDialog(null,
								"This should ideally show all search results using our famous vFolders");
			}
		});

		setLayout(new BorderLayout());
		JPanel top = new JPanel();
		top.setOpaque(true);

		top.setBorder(new CompoundBorder(new SeparatorBorder(), BorderFactory
				.createEmptyBorder(2, 4, 2, 4)));
		top.setBackground(titleBackground);
		top.setLayout(new BorderLayout());
		JLabel iconLabel = new JLabel(criteria.getIcon());
		iconLabel.setBackground(titleBackground);
		iconLabel.setOpaque(true);
		iconLabel.setBorder(BorderFactory.createEmptyBorder(1, 2, 1, 6));
		top.add(iconLabel, BorderLayout.WEST);
		top.add(link, BorderLayout.CENTER);
		top.add(moreLink, BorderLayout.EAST);
		add(top, BorderLayout.NORTH);
		add(collapsible, BorderLayout.CENTER);
	}

	public void resultArrived(IResultEvent event) {
		if (event.getProviderName() == null)
			return;

		if (!event.getProviderName().equals(resultPanel.getProviderName()))
			return;

		ISearchCriteria criteria = searchProvider.getCriteria(event
				.getSearchTerm());

		link.setText(criteria.getTitle());
		link.setToolTipText(criteria.getDescription());

		if (event.getTotalResultCount() == 0) {
			moreLink.setText("Show More ..");
			moreLink.setEnabled(false);
		} else {
			moreLink.setText("Show More" + " (" + event.getTotalResultCount()
					+ ") ..");
			moreLink.setEnabled(true);
		}

		revalidate();
	}

	public void clearSearch(IResultEvent event) {
		if (event.getProviderName() == null)
			return;

		if (!event.getProviderName().equals(resultPanel.getProviderName()))
			return;

		ISearchCriteria criteria = searchProvider.getCriteria(event
				.getSearchTerm());

		link.setText(criteria.getTitle());
		link.setToolTipText(criteria.getDescription());

		moreLink.setText("Show More ..");
		moreLink.setEnabled(false);
	}

	public void reset(IResultEvent event) {
		ISearchCriteria criteria = searchProvider.getCriteria("");

		link.setText(criteria.getTitle());
		link.setToolTipText(criteria.getDescription());

		moreLink.setText("Show More ..");
		moreLink.setEnabled(false);
	}

	/**
	 * The border between the stack components. It separates each component with
	 * a fine line border.
	 */
	class SeparatorBorder implements Border {

		boolean isFirst(Component c) {
			return c.getParent() == null || c.getParent().getComponent(0) == c;
		}

		public Insets getBorderInsets(Component c) {
			// if the collapsible is collapsed, we do not want its border to be
			// painted.
			if (c instanceof JXCollapsiblePane) {
				if (((JXCollapsiblePane) c).isCollapsed()) {
					return new Insets(0, 0, 0, 0);
				}
			}
			return new Insets(isFirst(c) ? 4 : 1, 0, 1, 0);
		}

		public boolean isBorderOpaque() {
			return true;
		}

		public void paintBorder(Component c, Graphics g, int x, int y,
				int width, int height) {
			g.setColor(borderColor);
			if (isFirst(c)) {
				g.drawLine(x, y + 2, x + width, y + 2);
			}
			g.drawLine(x, y + height - 1, x + width, y + height - 1);
		}
	}

}
