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

package org.columba.mail.gui.config.mailboximport;

import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.columba.core.gui.util.MultiLineLabel;
import org.columba.core.gui.util.wizard.DefaultWizardPanel;
import org.columba.core.gui.util.wizard.WizardTextField;

/**
 * @version 	1.0
 * @author
 */
public class SourcePanel extends DefaultWizardPanel
{
	JButton sourceButton;
	JButton destinationButton;

	public SourcePanel(
		JDialog dialog,
		ActionListener listener,
		String title,
		String description,
		ImageIcon icon)
	{
		super(dialog, listener, title, description, icon);
	}
	public SourcePanel(
		JDialog dialog,
		ActionListener listener,
		String title,
		String description,
		ImageIcon icon,
		boolean b)
	{
		super(dialog, listener, title, description, icon);
	}

	public void setSource(String str)
	{
		sourceButton.setText(str);
	}

	public void setDestination(String str)
	{
		destinationButton.setText(str);
	}

	protected JPanel createPanel(ActionListener listener)
	{
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 20, 30));
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		MultiLineLabel label =
			new MultiLineLabel("Please choose your source and destination folder.");

		panel.add(label);

		panel.add(Box.createRigidArea(new java.awt.Dimension(0, 40)));

		WizardTextField middlePanel = new WizardTextField();
		JLabel sourceLabel = new JLabel("Source Folder:");
		middlePanel.addLabel(sourceLabel);
		sourceButton = new JButton("source folder");
		sourceButton.setActionCommand("SOURCE");
		sourceButton.addActionListener(listener);
		middlePanel.addTextField(sourceButton);
		middlePanel.addExample(new JLabel(""));

		JLabel destinationLabel = new JLabel("Destination Folder:");
		middlePanel.addLabel(destinationLabel);
		destinationButton = new JButton("destination folder");
		destinationButton.setActionCommand("DESTINATION");
		destinationButton.addActionListener(listener);
		middlePanel.addTextField(destinationButton);
		middlePanel.addExample(new JLabel("(Destination Folder inside Columba)"));

		panel.add(middlePanel);

		return panel;
	}
}
