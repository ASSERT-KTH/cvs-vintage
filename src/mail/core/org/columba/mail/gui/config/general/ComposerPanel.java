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

package org.columba.mail.gui.config.general;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.*;

import org.columba.main.MainInterface;
import org.columba.mail.config.MailConfig;

public class ComposerPanel extends JPanel implements ActionListener
{

	JLabel spellLabel;
	JButton spellButton;

	JCheckBox emptySubjectCheckBox;

	public ComposerPanel()
	{
		initComponent();
	}

	public void updateComponents( boolean b )
	{

		if (b == true)
		{
			String path =
				MailConfig
					.getComposerOptionsConfig()
					.getSpellcheckItem()
					.getAspellExecutable();
			spellButton.setText(path);
		}
		else
		{
			MailConfig
				.getComposerOptionsConfig()
				.getSpellcheckItem()
				.setAspellExecutable(spellButton.getText());
		}
	}

	protected void initComponent()
	{
		setLayout(new BorderLayout(0,5));
		setBorder(BorderFactory.createEmptyBorder(12,12,11,11));
		JPanel spellPanel=new JPanel();
		spellPanel.setLayout(new BoxLayout(spellPanel,BoxLayout.X_AXIS));
		//LOCALIZE
		spellLabel = new JLabel("Path to aspell executable:");
		spellPanel.add(spellLabel);
		spellPanel.add(Box.createHorizontalStrut(5));
		spellButton = new JButton("aspell.exe");
		spellButton.setActionCommand("PATH");
		spellButton.addActionListener(this);
		spellLabel.setLabelFor(spellButton);
		spellPanel.add(spellButton);
		add(spellPanel,BorderLayout.NORTH);
		JPanel centerPanel=new JPanel();
		centerPanel.setLayout(new BoxLayout(centerPanel,BoxLayout.Y_AXIS));
		//LOCALIZE
		emptySubjectCheckBox =
			new JCheckBox("Ask when sending message with empty subject");
		emptySubjectCheckBox.setEnabled(false);
		centerPanel.add(emptySubjectCheckBox);
		centerPanel.add(Box.createVerticalGlue());
		add(centerPanel,BorderLayout.CENTER);
		//LOCALIZE
		JLabel restartLabel = new JLabel("These options affect only new composer windows.",SwingConstants.CENTER);
		add(restartLabel,BorderLayout.SOUTH);
	}

	public void actionPerformed(ActionEvent event)
	{
		String action = event.getActionCommand();

		if (action.equals("PATH"))
		{
			final JFileChooser fc = new JFileChooser();

			int returnVal = fc.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				File file = fc.getSelectedFile();

				spellButton.setText( file.getPath() );

			}

		}
	}
}