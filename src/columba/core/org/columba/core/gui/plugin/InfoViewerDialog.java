/*
 * Created on 07.08.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.core.gui.plugin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import net.javaprog.ui.wizard.plaf.basic.SingleSideEtchedBorder;

import org.columba.core.gui.util.NotifyDialog;
import org.columba.mail.gui.util.URLController;
import org.columba.mail.util.MailResourceLoader;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class InfoViewerDialog extends JDialog implements ActionListener {

	JButton helpButton;
	JButton closeButton;

	JTextPane textPane;
	URL url;
	/**
	 * @throws java.awt.HeadlessException
	 */
	public InfoViewerDialog(URL url) {
		// modal dialog
		super(new JFrame(), true);

		this.url = url;

		initComponents();

		pack();
		setLocationRelativeTo(null);

		try {

			textPane.setPage(url);
		} catch (IOException ex) {
			
			NotifyDialog d = new NotifyDialog();
			d.showDialog(ex);
			
			return;	
		}
		
		setVisible(true);
	}

	protected void initComponents() {
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 11, 11));
		getContentPane().add(mainPanel);

		// centerpanel

		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));

		textPane = new JTextPane();
		JScrollPane scrollPane = new JScrollPane(textPane);
		scrollPane.setPreferredSize(new Dimension(450, 300));
		scrollPane.getViewport().setBackground(Color.white);
		centerPanel.add(scrollPane);

		mainPanel.add(centerPanel);

		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.setBorder(new SingleSideEtchedBorder(SwingConstants.TOP));
		JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 0));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(17, 12, 11, 11));
		JButton closeButton =
			new JButton(MailResourceLoader.getString("global", "close"));
		closeButton.setActionCommand("CLOSE"); //$NON-NLS-1$
		closeButton.addActionListener(this);
		buttonPanel.add(closeButton);
		JButton helpButton =
			new JButton(MailResourceLoader.getString("global", "help"));
		helpButton.setActionCommand("HELP");
		helpButton.addActionListener(this);
		buttonPanel.add(helpButton);
		bottomPanel.add(buttonPanel, BorderLayout.EAST);
		getContentPane().add(bottomPanel, BorderLayout.SOUTH);
		getRootPane().setDefaultButton(closeButton);
		getRootPane().registerKeyboardAction(
			this,
			"CLOSE",
			KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
			JComponent.WHEN_IN_FOCUSED_WINDOW);
		getRootPane().registerKeyboardAction(
			this,
			"HELP",
			KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0),
			JComponent.WHEN_IN_FOCUSED_WINDOW);

	}

	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();

		if (action.equals("CLOSE")) {

			setVisible(false);
		} else if (action.equals("HELP")) {
			URLController c = new URLController();
			try {
				c.open(new URL("help.html"));
			} catch (MalformedURLException mue) {
			}
		}

	}

}
