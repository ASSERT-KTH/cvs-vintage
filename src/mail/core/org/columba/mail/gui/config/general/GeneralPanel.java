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
import java.util.Locale;

import javax.swing.*;

import org.columba.core.config.Config;
import org.columba.core.config.WindowItem;
import org.columba.mail.config.MailConfig;

public class GeneralPanel extends JPanel implements ActionListener
{
	JLabel markLabel1, markLabel2;
	JTextField markTextField;

	JLabel codepageLabel;
	JButton codepageButton;

	JCheckBox emptyTrashCheckBox;

	JCheckBox preferHtmlCheckBox;

	JComboBox toolbarComboBox;

	public GeneralPanel()
	{
		initComponent();
	}

	public void updateComponents( boolean b )
	{
		WindowItem item = MailConfig.getMainFrameOptionsConfig().getWindowItem();

		if (b == true)
		{
			String delay =
				Config.getOptionsConfig().getStringGuiOptions(
					"markasreaddelay",
					"2");
			System.out.println("delay=" + delay);
			markTextField.setText(delay);

			boolean preferhtml = MailConfig.getMainFrameOptionsConfig().getWindowItem().getHtmlViewer();
			if ( preferhtml == true )
				preferHtmlCheckBox.setSelected(true);
			else
				preferHtmlCheckBox.setSelected(false);

			toolbarComboBox.setSelectedIndex( item.getToolbarState() );

		}
		else
		{
			Config.getOptionsConfig().setStringGuiOption(
				"markasreaddelay",
		markTextField.getText());

			if ( preferHtmlCheckBox.isSelected() )
				MailConfig.getMainFrameOptionsConfig().getWindowItem().setHtmlViewer(true);
			else
				MailConfig.getMainFrameOptionsConfig().getWindowItem().setHtmlViewer(false);

			item.setToolbarState( toolbarComboBox.getSelectedIndex() );

		}
	}

	protected void initComponent()
	{
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createEmptyBorder(12,12,11,11));
		JPanel markPanel = new JPanel();
		markPanel.setLayout( new FlowLayout(FlowLayout.LEFT) );
		markPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		//LOCALIZE
		markLabel1 = new JLabel("Mark messages as read after");
		markPanel.add( markLabel1 );
		markTextField = new JTextField("2",3);
		markPanel.add( markTextField );
		markLabel2 = new JLabel("seconds");
		markPanel.add( markLabel2 );
		add(markPanel);
		JPanel codepagePanel = new JPanel();
		codepagePanel.setLayout( new FlowLayout(FlowLayout.LEFT) );
		codepagePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		//LOCALIZE
		codepageLabel = new JLabel("Default locale:");
		codepagePanel.add( codepageLabel );
		codepageButton = new JButton(Locale.getDefault().getDisplayName());
		codepageButton.setEnabled(false);
		codepageButton.setActionCommand("CODEPAGE");
		codepageButton.addActionListener(this);
		codepageLabel.setLabelFor(codepageButton);
		codepagePanel.add( codepageButton );
		add(codepagePanel);
		//LOCALIZE
		emptyTrashCheckBox = new JCheckBox("Empty trash on exit");
		emptyTrashCheckBox.setEnabled(false);
		add(emptyTrashCheckBox);
		//LOCALIZE
		preferHtmlCheckBox = new JCheckBox("Prefer HTML messages, if available");
		add(preferHtmlCheckBox);
		JPanel toolbarPanel = new JPanel();
		toolbarPanel.setLayout( new FlowLayout(FlowLayout.LEFT) );
		toolbarPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		//LOCALIZE
		JLabel toolbarLabel = new JLabel("Toolbar:");
		toolbarPanel.add( toolbarLabel );
		//LOCALIZE
		toolbarComboBox = new JComboBox(new String[]{"Icons only","Text only","Text below Icons","Text beside Icons"});
		toolbarLabel.setLabelFor(toolbarComboBox);
		toolbarPanel.add( toolbarComboBox );
		add(toolbarPanel);
		add(Box.createVerticalGlue());
	}

	protected JMenu createSubMenu( Locale[] locales, int startIndex, int stopIndex )
	{
		JMenu menu = new JMenu("more..");

		for ( int i=startIndex; i<stopIndex; i++ )
		{
			JMenuItem item = new JMenuItem( locales[i].getDisplayName() );
			menu.add( item );
		}

		return menu;
	}

	public void actionPerformed( ActionEvent ev )
	{
		String str = ev.getActionCommand();

		if ( str.equals("CODEPAGE") )
		{
			JPopupMenu menu = new JPopupMenu();
			JMenu selectedMenu=null;
			Locale[] locales = Locale.getAvailableLocales();
			int counter = 0;
			boolean firstMenu = true;
			for ( int i=0; i<locales.length; i++ )
			{
				Locale locale = (Locale) locales[i];

				if ( firstMenu == true )
				{
					if ( counter < 10 )
					{
						JMenuItem item = new JMenuItem( locale.getDisplayName() );
						menu.add( item );
					}
					else
					{
						firstMenu = false;
					}
				}
				else
				{
					if ( counter % 10 == 0 )
					{
						JMenu submenu = createSubMenu( locales, i-10, i );
						if ( menu.getComponents().length <= 10 )
						{
							menu.add( submenu );
							selectedMenu = submenu;
						}
						else
						{
							selectedMenu.add( submenu );
							selectedMenu = submenu;
						}

						counter = 0;
					}
				}

				counter++;



				/*
				System.out.println("locale name:"+locale.getDisplayName() );
				System.out.println("locale language:"+locale.getDisplayLanguage() );
				System.out.println("locale variant:"+locale.getDisplayVariant() );
				System.out.println("locale country:"+locale.getCountry() );
				System.out.println("locale language:"+locale.getLanguage() );
				System.out.println("locale language2:"+locale.getISO3Country() );
				System.out.println("locale language3:"+locale.getISO3Language() );
				System.out.println("locale tostring:"+locale.toString() );
				*/



			}

			JButton button  = (JButton) ev.getSource();
			menu.show( button, button.getX(), button.getY() );
		}
	}
}