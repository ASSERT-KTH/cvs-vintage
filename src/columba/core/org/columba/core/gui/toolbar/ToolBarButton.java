package org.columba.core.gui.toolbar;

import java.awt.Insets;

import javax.swing.Action;
import javax.swing.JButton;

/**
 * ToolBar button.
 * 
 * @author Frederik Dietz
 */
public class ToolBarButton extends JButton {

	public ToolBarButton() {
		initButton();
	}
	
	public ToolBarButton(Action action) {
		super(action);
		
		initButton();
	}

	private void initButton() {
		setRolloverEnabled(true);
		setRequestFocusEnabled(false);
		setMargin(new Insets(1, 1, 1, 1));
		putClientProperty("JToolBar.isRollover", Boolean.TRUE);
	}

	public boolean isFocusTraversable() {
		return isRequestFocusEnabled();
	}

	/**
	 * @see javax.swing.JButton#updateUI()
	 */
	public void updateUI() {
		super.updateUI();

		setRolloverEnabled(true);
		putClientProperty("JToolBar.isRollover", Boolean.TRUE);
	}

}
