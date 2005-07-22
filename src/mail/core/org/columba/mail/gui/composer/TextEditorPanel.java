package org.columba.mail.gui.composer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.UIManager;

import org.columba.core.gui.util.FontProperties;

public class TextEditorPanel extends JScrollPane{

	private JPanel contentPane;
	
	public TextEditorPanel() {
		super();
		contentPane = new VerticalScrollablePanel();
		
		contentPane.setBorder(null);
		contentPane.setLayout(new BorderLayout());
		
		setViewportView(contentPane);
		getViewport().setBackground(UIManager.getColor("List.background"));
	}

	/**
	 * @return Returns the contentPane.
	 */
	public JPanel getContentPane() {
		return contentPane;
	}

}

class VerticalScrollablePanel extends JPanel implements Scrollable {

	/**
	 * 
	 */
	public VerticalScrollablePanel() {
		super();
	}

	public boolean getScrollableTracksViewportHeight() {
		return false;
	}

	public boolean getScrollableTracksViewportWidth() {
		return true;
	}

	public Dimension getPreferredScrollableViewportSize() {
		return null;
	}

	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		return FontProperties.getTextFont().getSize() * 10;
	}

	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		return FontProperties.getTextFont().getSize() * 3;
	}
	
}