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

package org.columba.core.gui.themes.contrastcolumba;

import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
import javax.swing.plaf.metal.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

import org.columba.mail.message.*;
import org.columba.core.config.*;
import org.columba.core.gui.themes.*;
import org.columba.core.gui.themes.thincolumba.*;
import org.columba.core.gui.util.*;
import org.columba.core.gui.util.ImageLoader;
import org.columba.mail.config.*;

public class ContrastColumbaTheme extends DefaultCTheme
{


    /*
    private final ColorUIResource primary1 = new ColorUIResource(218,165,32);
    private final ColorUIResource primary2 = new ColorUIResource(218,165,32);
    private final ColorUIResource primary3 = new ColorUIResource(218,165,32);
    */

      /*
    private final ColorUIResource secondary1 = new ColorUIResource(148,148,148);
    private final ColorUIResource secondary2 = new ColorUIResource(160,160,160);
    private final ColorUIResource secondary3 = new ColorUIResource(213,213,213);
      */


    private final ColorUIResource secondary1 = new ColorUIResource(148,148,148);
    private final ColorUIResource secondary2 = new ColorUIResource(160,160,160);
    private final ColorUIResource secondary3 = new ColorUIResource(213,213,213);

	private final ColorUIResource primary1 = new ColorUIResource(148,148,148);
    private final ColorUIResource primary2 = new ColorUIResource(148,148,148);
     private final ColorUIResource primary3 = new ColorUIResource(148,148,148);
     
     
     private final ColorUIResource foreground;
    private final ColorUIResource background;
    /*
    private final ColorUIResource secondary1 = new ColorUIResource(128,128,128);
    private final ColorUIResource secondary2 = new ColorUIResource(140,140,140);
    private final ColorUIResource secondary3 = new ColorUIResource(193,193,193);
    */

    public ContrastColumbaTheme( ThemeItem item )
    {
        super( item );
        
        
        foreground = new ColorUIResource( item.getForeground() );

	background = new ColorUIResource( item.getBackground() );

          // mainFont = mFont;
          // messageFont = eFont;

    }


    
      // menu border, labels
    protected ColorUIResource getPrimary1()
    {
        return primary1;
    }


    protected ColorUIResource getPrimary2()
    {
        return primary2;
    }

    protected ColorUIResource getPrimary3()
    {
        return primary3;
    }
    

    protected ColorUIResource getSecondary1() { return secondary1; }
    protected ColorUIResource getSecondary2() { return secondary2; }
    protected ColorUIResource getSecondary3() { return secondary3; }


    public String getName()
    {
        return "Contrast Columba Theme";
    }


	public void addCustomEntriesToTable(UIDefaults table)
	{
		super.addCustomEntriesToTable(table);
		
		
		ColorUIResource menuItemPressedBackground = 
	    new ColorUIResource(table.getColor("controlHighlight"));
        ColorUIResource menuItemPressedForeground =
	    new ColorUIResource(table.getColor("controlText"));
	    
		Border raisedBevelBorder = 
	    new ContrastColumbaBorders.BevelBorder(true, MetalLookAndFeel.getControlHighlight(),
				       MetalLookAndFeel.getControlDarkShadow(),
				       Color.black);

        Border marginBorder = new BasicBorders.MarginBorder();
        
		Border menuBarBorder =
			new ContrastColumbaBorders.MenuBarBorder(
				MetalLookAndFeel.getControlDarkShadow(),
				MetalLookAndFeel.getControlHighlight(),
				Color.black);

		Border menuMarginBorder =
			new BorderUIResource.CompoundBorderUIResource(raisedBevelBorder, marginBorder);


		table.put("Separator.background", MetalLookAndFeel.getControlDarkShadow() );
        table.put("Separator.foreground", MetalLookAndFeel.getControlHighlight() );
           
           
         
         
         
		table.put(
			"ScrollBarUI",
			"org.columba.core.gui.themes.contrastcolumba.ContrastColumbaScrollBarUI");
		table.put(
			"SplitPaneUI",
			"org.columba.core.gui.themes.contrastcolumba.ContrastColumbaSplitPaneUI");
		table.put(
			"TreeUI",
			"org.columba.core.gui.themes.contrastcolumba.ContrastColumbaTreeUI");
		table.put(
			"ButtonUI",
			"org.columba.core.gui.themes.contrastcolumba.ContrastColumbaButtonUI");
		table.put(
			"MenuBarUI",
			"org.columba.core.gui.themes.contrastcolumba.ContrastColumbaMenuBarUI");
		table.put(
			"MenuItemUI",
			"org.columba.core.gui.themes.contrastcolumba.ContrastColumbaMenuItemUI");

		table.put("Label.foreground", table.get("textText"));

		table.put("ScrollBar.width", new Integer(20));
		table.put("ScrollBar.allowsAbsolutePositioning", Boolean.FALSE);
		table.put("SplitPane.dividerSize", new Integer(5));
		table.put("SplitPane.border", null);

		//table.put( "Button.margin", new InsetsUIResource(2, 14, 2, 14) );

		
		table.put("ScrollPane.border", new ContrastColumbaScrollPaneBorder());
		table.put("Table.scrollPaneBorder", new ContrastColumbaScrollPaneBorder());

		table.put("Button.border", new ContrastColumbaButtonBorder());

		table.put("TextField.border", new ContrastColumbaTextFieldBorder());

		
		//table.put( "PopupMenu.border", new ContrastColumbaPopupMenuBorder() );
		table.put("MenuBar.border", menuBarBorder );
		table.put("MenuBar.selectionForeground", menuItemPressedForeground);
        table.put("MenuBar.selectionBackground", menuItemPressedBackground );
            
		table.put("Menu.border", menuBarBorder );
		table.put("Menu.selectionForeground", menuItemPressedForeground);
        table.put("Menu.selectionBackground", menuItemPressedBackground );
        
		table.put("MenuItem.border", menuBarBorder );
		table.put("MenuItem.selectionForeground", menuItemPressedForeground);
        table.put("MenuItem.selectionBackground", menuItemPressedBackground );
		
		
		table.put("ToolBar.border", new ContrastColumbaToolBarBorder());

		table.put("List.font", getUserTextFont());

		table.put("TitledBorder.font", getUserTextFont());

		table.put("TitledBorder.titleColor", Color.black);

		table.put("StatusBar.border", new ContrastColumbaStatusBarBorder());

		table.put("ToolBarButton.border", new ContrastColumbaToolBarButtonBorder());

		//table.put("TableHeader.cellBorder", new ContrastColumbaButtonBorder());

		table.put("HeaderView.border", new ContrastColumbaMessageHeaderBorder());

		//table.put("MenuItem.border", new ContrastColumbaMenuBarBorder());

		//table.put( "CheckBoxMenuItem.border", new ContrastColumbaMenuItemBorder() );
		//table.put( "RadioButtonMenuItem.border", new ContrastColumbaMenuItemBorder() );

		//table.put("FolderInfoPanel.border", new ContrastColumbaFolderInfoPanelBorder());

		// Icons

		

	}

}







