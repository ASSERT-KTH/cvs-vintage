package org.columba.mail.gui.message.viewer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.columba.core.gui.base.SingleSideEtchedBorder;
import org.columba.core.gui.util.DialogHeaderPanel;
import org.frapuccino.checkablelist.CheckableItemImpl;
import org.frapuccino.checkablelist.CheckableItemListTableModel;
import org.frapuccino.checkablelist.CheckableList;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.ButtonStackBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;


public class AddressListDialog extends JDialog implements ActionListener,
		ListSelectionListener {

	private JButton addContactButton;

	private JButton composeButton;

	private JButton replyButton;

	private JButton forwardButton;

	private JButton closeButton;

	private JButton helpButton;

	private CheckableList list;

	private int index;

	private CheckableItemImpl selection;
	
	public AddressListDialog(Frame owner) throws HeadlessException {
		super(owner, true);

		setTitle("Address Manager");

		initComponents();

		layoutComponents();

		getRootPane().registerKeyboardAction(this, "CLOSE",
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	private void layoutComponents() {
		setLayout(new BorderLayout());

		add(new DialogHeaderPanel("Manage Address List",
				"Select email addresses for mass actions"), BorderLayout.NORTH);

		getContentPane().add(createPanel(), BorderLayout.CENTER);

		add(createBottomPanel(), BorderLayout.SOUTH);

	}

	private JPanel createEastPanel() {
		JPanel eastPanel = new JPanel();
		ButtonStackBuilder builder = new ButtonStackBuilder(eastPanel);
		builder.addGridded(addContactButton);
		builder.addUnrelatedGap();
		builder.addGridded(composeButton);
		builder.addRelatedGap();
		builder.addGridded(replyButton);
		builder.addRelatedGap();
		builder.addGridded(forwardButton);
		return eastPanel;
	}

	private JPanel createPanel() {
		JPanel jpanel1 = new JPanel();
		FormLayout formlayout1 = new FormLayout(
				"FILL:DEFAULT:GROW(1.0),3DLU,FILL:DEFAULT:NONE",
				"CENTER:DEFAULT:NONE,1DLU,FILL:DEFAULT:GROW(1.0),3DLU,CENTER:DEFAULT:NONE");
		CellConstraints cc = new CellConstraints();
		jpanel1.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
		jpanel1.setLayout(formlayout1);

		JLabel jlabel1 = new JLabel();
		jlabel1.setText("Email Address:");
		jpanel1.add(jlabel1, cc.xy(1, 1));

		JScrollPane scrollPane = new JScrollPane(list);
		scrollPane.setPreferredSize(new Dimension(250, 200));
		jpanel1.add(scrollPane, cc.xy(1, 3));

		jpanel1.add(createEastPanel(), new CellConstraints(3, 3, 1, 1,
				CellConstraints.DEFAULT, CellConstraints.TOP));

		return jpanel1;
	}

	private JPanel createBottomPanel() {
		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.setBorder(new SingleSideEtchedBorder(SwingConstants.TOP));

		JPanel buttonPanel = new JPanel();
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

		ButtonBarBuilder builder2 = new ButtonBarBuilder(buttonPanel);
		// builder.setDefaultButtonBarGapBorder();
		builder2.addGlue();
		builder2.addGriddedButtons(new JButton[] { closeButton, helpButton });

		bottomPanel.add(buttonPanel, BorderLayout.EAST);

		return bottomPanel;
	}

	private void initComponents() {

		list = new CheckableList();
		list.getSelectionModel().addListSelectionListener(this);

		addContactButton = new JButton("Add Contacts");

		composeButton = new JButton("Compose");

		replyButton = new JButton("Reply");

		forwardButton = new JButton("Forward");

		// TODO i18n "Close" button
		closeButton = new JButton("Close");
		closeButton.setDefaultCapable(true);
		closeButton.setMnemonic('C');
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				setVisible(false);
			}
		});

		// TODO i18n "Help" button
		helpButton = new JButton("Help");
		helpButton.setMnemonic('h');
	}

	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) {
			return;
		}

		DefaultListSelectionModel theList = (DefaultListSelectionModel) e
				.getSource();
		if (!theList.isSelectionEmpty()) {
			index = theList.getAnchorSelectionIndex();

			selection = (CheckableItemImpl) ((CheckableItemListTableModel) list
					.getModel()).getElement(index);
			
		}
	}

	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();

		if (action.equals("OK")) {
		} else if (action.equals("CANCEL")) {
			setVisible(false);
		}
	}

}
