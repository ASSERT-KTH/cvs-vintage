// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

package org.columba.core.gui.util.wizard;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.border.*;

public class DefaultWizardPanel extends JPanel
implements ActionListener, DocumentListener {
	
	private JButton nextButton;
	private JButton prevButton;
	private JButton cancelButton;
	private JButton helpButton;
	private JButton finishButton;
	private int position;

	public DefaultWizardPanel nextPanel;
	public DefaultWizardPanel prevPanel;

	public static int FIRST = 0;
	public static int LAST = 1;
	public static int MIDDLE = 2;

	private Vector v;

	protected JDialog dialog;

	public DefaultWizardPanel(
		JDialog dialog,
		ActionListener listener,
		String title,
		String description,
		ImageIcon icon,
		int position) {
		this.position = position;
		this.dialog = dialog;

		v = new Vector();

		init(listener, title, description, icon);
	}

	public void register(JTextComponent comp) {
		comp.getDocument().addDocumentListener(this);
		v.add(comp);
	}

	protected JPanel createPanel(ActionListener listener) {
		return new JPanel();
	}

	public void setNext(DefaultWizardPanel panel) {
		nextPanel = panel;
	}

	public void setPrev(DefaultWizardPanel panel) {
		prevPanel = panel;
	}

	public JPanel createTopPanel(
		String title,
		String description,
		ImageIcon icon) {
		JPanel panel = new JPanel();
		panel.setBackground(Color.white);
		panel.setPreferredSize(new Dimension(300, 60));
		panel.setLayout(new BorderLayout());
		panel.setBorder(new CompoundBorder(new WizardBottomBorder(),BorderFactory.createEmptyBorder(12, 12, 11, 11)));

		JPanel leftPanel = new JPanel();
		leftPanel.setBackground(Color.white);

		GridBagLayout layout = new GridBagLayout();
		leftPanel.setLayout(layout);
		GridBagConstraints c = new GridBagConstraints();

		JLabel titleLabel = new JLabel(title);
		//titleLabel.setAlignmentY(0);
		Font font = UIManager.getFont("Label.font");
		font = font.deriveFont(Font.BOLD);
		titleLabel.setFont(font);
		c.gridy = 0;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(titleLabel, c);
		leftPanel.add(titleLabel);

		c.gridy = 1;
		c.insets = new Insets(0, 20, 0, 0);
		JLabel descriptionLabel = new JLabel(description);
		layout.setConstraints(descriptionLabel, c);
		leftPanel.add(descriptionLabel);

		panel.add(leftPanel, BorderLayout.WEST);

		JLabel iconLabel = new JLabel(icon);
		panel.add(iconLabel, BorderLayout.EAST);

		return panel;
	}

	public JPanel createLeftPanel() {
		JPanel panel = new JPanel();

		panel.setBackground(UIManager.getColor("textHighlight"));
		panel.setPreferredSize(new Dimension(100, 200));

		JLabel title =
			new JLabel((ImageIcon) UIManager.getIcon("org/columba/core/images/Zoom24.gif"));
		//title.setForeground( Color.white );
		title.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		return panel;
	}

	protected void init(
		ActionListener listener,
		String title,
		String description,
		ImageIcon icon) {

		setLayout(new BorderLayout());

		add(createTopPanel(title, description, icon), BorderLayout.NORTH);

		add(createPanel(listener), BorderLayout.CENTER);

		helpButton = new JButton("Help");
		helpButton.setEnabled(false);

		nextButton = new JButton("Next >");
		nextButton.setActionCommand("NEXT");
		nextButton.addActionListener(this);
		nextButton.setEnabled(false);

		if (position == LAST) {
			finishButton = new JButton("Finish");
			finishButton.setActionCommand("FINISH");
			finishButton.addActionListener(listener);
			if (v.size() == 0) {
				finishButton.setEnabled(true);
				dialog.getRootPane().setDefaultButton(finishButton);
			} else
				finishButton.setEnabled(false);
		} else {
			//nextButton.setEnabled( true );
			if (v.size() == 0) {
				dialog.getRootPane().setDefaultButton(nextButton);
				nextButton.setEnabled(true);
			}

		}

		prevButton = new JButton("< Prev");
		prevButton.setActionCommand("PREV");
		prevButton.addActionListener(this);

		if (position == FIRST)
			prevButton.setEnabled(false);
		else
			prevButton.setEnabled(true);

		cancelButton = new JButton("Cancel");
		cancelButton.setActionCommand("CANCEL");
		cancelButton.addActionListener(this);

		JPanel lower = new JPanel();
		lower.setLayout(new BorderLayout());
		//lower.setLayout(new BoxLayout(lower, BoxLayout.X_AXIS));	

		lower.setBorder(new CompoundBorder(new WizardTopBorder(),BorderFactory.createEmptyBorder(17, 12, 11, 11)));

		JPanel innerPanel = new JPanel();
		innerPanel.setLayout(new GridLayout(1, 4, 10, 10));
		lower.add(innerPanel, BorderLayout.EAST);

		innerPanel.add(helpButton);

		innerPanel.add(prevButton);

		if (position == LAST) {
			innerPanel.add(finishButton);

		} else {
			innerPanel.add(nextButton);

		}

		innerPanel.add(cancelButton);

		add(lower, BorderLayout.SOUTH);

	}
	
	public void select()
	{
	}

	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();

		if (action.equals("NEXT")) {

			Runnable run = new Runnable() {
				public void run() {
					dialog.getContentPane().removeAll();
					dialog.getContentPane().add(nextPanel);
					nextPanel.select();
					dialog.validate();
					dialog.repaint();
				}
			};
			try {
				if (!SwingUtilities.isEventDispatchThread())
					SwingUtilities.invokeAndWait(run);
				else
					SwingUtilities.invokeLater(run);

			} catch (Exception ex) {
			}

		} else if (action.equals("PREV")) {
			Runnable run = new Runnable() {
				public void run() {
					dialog.getContentPane().removeAll();
					dialog.getContentPane().add(prevPanel);
					prevPanel.select();
					dialog.validate();
					dialog.repaint();

				}
			};
			try {
				if (!SwingUtilities.isEventDispatchThread())
					SwingUtilities.invokeAndWait(run);
				else
					SwingUtilities.invokeLater(run);

			} catch (Exception ex) {
			}

		} else if (action.equals("CANCEL")) {

			dialog.setVisible(false);
		}
	}

	public void insertUpdate(DocumentEvent e) {
		update();
	}

	public void removeUpdate(DocumentEvent e) {
		update();
	}

	public void changedUpdate(DocumentEvent e) {
		update();
		//Plain text components don't fire these events
	}

	public void update() {
		JTextComponent tc;
		String str;
		boolean hit = false;

		for (int i = 0; i < v.size(); i++) {
			tc = (JTextComponent) v.get(i);
			str = tc.getText();

			if (str.length() == 0)
				hit = true;
		}

		if (hit == true) {
			nextButton.setEnabled(false);
			if (finishButton != null)
				finishButton.setEnabled(false);
		} else {
			nextButton.setEnabled(true);
			if (finishButton != null)
				finishButton.setEnabled(true);
		}
	}
}
