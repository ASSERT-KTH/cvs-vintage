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

package org.columba.core.gui.util;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.UIManager;

public class CInfoPanel extends JPanel
{
	protected JPanel panel;
	protected GridBagLayout gridbagLayout;
	protected GridBagConstraints gridbagConstraints;
	
	protected Font font;
	
	public CInfoPanel()
	{
		super();
		
		
		font = UIManager.getFont("Label.font");
	
        font = font.deriveFont( Font.BOLD );
        int size = font.getSize();
        font = font.deriveFont( Font.BOLD, size+2 );
	
	
		setBorder( BorderFactory.createEmptyBorder(1,1,1,1) );
		
		setLayout( new BorderLayout() );
		      
		panel = new JPanel();
		
		add( panel, BorderLayout.CENTER );
		
		 initComponents();
	}
	
	
	public void initComponents()
    {
        panel.removeAll();

		
		
        gridbagLayout = new GridBagLayout();
        panel.setLayout( gridbagLayout );
        
        gridbagConstraints = new GridBagConstraints();;

        panel.setLayout( gridbagLayout );

        panel.setBackground( UIManager.getColor("controlShadow") );
    }
}
