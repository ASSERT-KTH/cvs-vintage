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

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;

import org.columba.core.gui.config.themes.ThemePanel;

public class GeneralOptionsDialog extends JDialog implements ActionListener
{
	JTabbedPane centerPane;
	GeneralPanel generalPanel;
	ComposerPanel composerPanel;
	ThemePanel themePanel;
	FontPanel fontPanel;

	JButton okButton;
	JButton cancelButton;
	boolean result;

	public GeneralOptionsDialog( JFrame frame )
	{
		//LOCALIZE
		super( frame, "General Options", true );
		initComponents();
		pack();
		setLocationRelativeTo(null);
	}

	public void updateComponents( boolean b )
	{
		generalPanel.updateComponents( b );
		composerPanel.updateComponents( b );
		themePanel.updateComponents( b );
		fontPanel.updateComponents( b );
	}


	protected void initComponents()
	{
		JPanel contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout(0,0));
		centerPane = new JTabbedPane();
		generalPanel = new GeneralPanel();
		//LOCALIZE
		centerPane.add( generalPanel, "General" );
		composerPanel = new ComposerPanel();
		//LOCALIZE
		centerPane.add( composerPanel, "Composer" );
		themePanel = new ThemePanel();
		//LOCALIZE
		centerPane.add( themePanel, "Themes and Icons" );
		fontPanel = new FontPanel();
		//LOCALIZE
		centerPane.add( fontPanel, "Fonts" );
		contentPane.add(centerPane, BorderLayout.CENTER);
		JPanel bottomPanel = new JPanel(new BorderLayout(0,0));
		bottomPanel.setBorder(BorderFactory.createEmptyBorder(17, 0, 11, 11));
		JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 0));
		//LOCALIZE
		okButton = new JButton("Ok");
		//mnemonic
		okButton.setActionCommand("OK");
		okButton.addActionListener(this);
		buttonPanel.add(okButton);
		//LOCALIZE
		JButton cancelButton = new JButton("Cancel");
		//mnemonic
		cancelButton.setActionCommand("CANCEL");
		cancelButton.addActionListener(this);
		buttonPanel.add(cancelButton);
		bottomPanel.add(buttonPanel, BorderLayout.EAST);
		contentPane.add(bottomPanel, BorderLayout.SOUTH);
		setContentPane(contentPane);
		getRootPane().setDefaultButton(okButton);
		getRootPane().registerKeyboardAction(this,"CANCEL",
						KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0),
						JComponent.WHEN_IN_FOCUSED_WINDOW);
	}

	public void actionPerformed(ActionEvent event)
	{
		String action = event.getActionCommand();

		if (action.equals("OK"))
		{
			result = true;
			setVisible(false);

		}
		else if (action.equals("CANCEL"))
		{
			result = false;
			setVisible(false);

		}
	}

	public boolean getResult()
	{
		return result;
	}
}
