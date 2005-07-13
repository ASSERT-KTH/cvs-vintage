package org.columba.mail.gui.composer;

import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class TextEditorPanel extends JScrollPane  {

	private JPanel contentPane;
	
	public TextEditorPanel() {
		super();
		contentPane = new JPanel();
		
		contentPane.setBorder(null);
		contentPane.setLayout(new GridLayout(2,1));
		
		setViewportView(contentPane);
	}

	/**
	 * @return Returns the contentPane.
	 */
	public JPanel getContentPane() {
		return contentPane;
	}
	

}
