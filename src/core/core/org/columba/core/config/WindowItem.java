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

package org.columba.core.config;

import java.util.Vector;
import java.awt.*;

import org.columba.mail.folder.*;

import org.w3c.dom.*;

import org.columba.core.config.*;
import org.columba.core.gui.themes.*;

public class WindowItem extends DefaultItem
{
	private AdapterNode xposition, yposition, width, height, maximized;
	private AdapterNode showToolbar,
		showFilterToolbar,
		showFolderInfo,
		mainsplitpane,
		rightsplitpane,
		selectedheader,
		headerascending,
		showAddressbook;

	private AdapterNode toolbarshowtext, toolbartextposition, toolbarshowicon;

	private AdapterNode advancedviewer, htmlviewer;

	private AdapterNode rootNode;

	public WindowItem(Document doc, AdapterNode root)
	{
		super(doc);
		this.rootNode = root;

		parse();

		createMissingElements();

	}

	protected void parse()
	{

		for (int i = 0; i < rootNode.getChildCount(); i++)
		{
			AdapterNode child = rootNode.getChildAt(i);
            String childName = child.getName();

			if (childName.equals("xposition"))
			{
				xposition = child;
			}
			else if (childName.equals("yposition"))
			{
				yposition = child;
			}
			else if (childName.equals("width"))
			{
				width = child;
			}
			else if (childName.equals("height"))
			{
				height = child;
			}
			else if (childName.equals("showfiltertoolbar"))
			{
				showFilterToolbar = child;
			}
			else if (childName.equals("showfolderinfo"))
			{
				showFolderInfo = child;
			}
			else if (childName.equals("showtoolbar"))
			{
				showToolbar = child;
			}
			else if (childName.equals("selectedheader"))
			{
				selectedheader = child;
			}
			else if (childName.equals("headerascending"))
			{
				headerascending = child;
			}
			else if (childName.equals("htmlviewer"))
			{
				htmlviewer = child;
			}
			else if (childName.equals("advancedviewer"))
			{
				advancedviewer = child;
			}
			else if (childName.equals("mainsplitpane"))
			{
				mainsplitpane = child;
			}
			else if (childName.equals("rightsplitpane"))
			{
				rightsplitpane = child;
			}
			else if (childName.equals("showaddressbook"))
			{
				showAddressbook = child;
			}
			else if (childName.equals("toolbarshowtext"))
			{
				toolbarshowtext = child;
			}
			else if (childName.equals("toolbarshowicon"))
			{
				toolbarshowicon = child;
			}
			else if (childName.equals("toolbartextposition"))
			{
				toolbartextposition = child;
			}
			else if (childName.equals("maximized"))
			{
				maximized = child;
			}

		}
	}

	protected void createMissingElements()
	{

		if (xposition == null)
			xposition = addKey(rootNode, "xposition", "0");
		if (yposition == null)
			yposition = addKey(rootNode, "yposition", "0");
		if (width == null)
			width = addKey(rootNode, "width", "640");
		if (height == null)
			height = addKey(rootNode, "height", "480");
		if (advancedviewer == null)
			advancedviewer = addKey(rootNode, "advancedviewer", "true");
		if (htmlviewer == null)
			htmlviewer = addKey(rootNode, "htmlviewer", "true");
		if (mainsplitpane == null)
			mainsplitpane = addKey(rootNode, "mainsplitpane", "100");
		if (rightsplitpane == null)
			rightsplitpane = addKey(rootNode, "rightsplitpane", "100");
		if (selectedheader == null)
			selectedheader = addKey(rootNode, "selectedheader", "date");
		if (headerascending == null)
			headerascending = addKey(rootNode, "headerascending", "true");
		if (showFilterToolbar == null)
			showFilterToolbar = addKey(rootNode, "showfiltertoolbar", "true");
		if (showFolderInfo == null)
			showFolderInfo = addKey(rootNode, "showfolderinfo", "true");
		if (showToolbar == null)
			showToolbar = addKey(rootNode, "showtoolbar", "true");
		if (showAddressbook == null)
			showAddressbook = addKey(rootNode, "showaddressbook", "false");

		if (toolbarshowtext == null)
			toolbarshowtext = addKey(rootNode, "toolbarshowtext", "true");
		if (toolbarshowicon == null)
			toolbarshowicon = addKey(rootNode, "toolbarshowicon", "true");
		if (toolbartextposition == null)
			toolbartextposition = addKey(rootNode, "toolbartextposition", "false");
		if ( maximized == null ) maximized = addKey(rootNode,"maximized","false");
	}

	/******************************************** set ***************************************/

	public void setMaximized(boolean b)
	{
		Boolean bool = new Boolean(b);

		setTextValue(maximized, bool.toString());
	}
	
	public void setHeaderAscending(boolean b)
	{
		Boolean bool = new Boolean(b);

		setTextValue(headerascending, bool.toString());
	}

	public void setSelectedHeader(String s)
	{
		setTextValue(selectedheader, s);
	}

	public void setXPosition(int i)
	{
		Integer h = new Integer(i);

		setTextValue(xposition, h.toString());
	}

	public void setYPosition(int i)
	{
		Integer h = new Integer(i);

		setTextValue(yposition, h.toString());
	}

	public void setWidth(int i)
	{
		Integer h = new Integer(i);

		setTextValue(width, h.toString());
	}

	public void setHeight(int i)
	{
		Integer h = new Integer(i);

		setTextValue(height, h.toString());
	}

	public void setShowToolbar(String str)
	{
		setTextValue(showToolbar, str);
	}

	public void setShowFilterToolbar(String str)
	{
		setTextValue(showFilterToolbar, str);
	}

	public void setShowFolderInfo(String str)
	{
		setTextValue(showFolderInfo, str);
	}

	public void setShowAddressbook(String str)
	{
		setTextValue(showAddressbook, str);
	}

	public void setMainSplitPane(int i)
	{
		Integer h = new Integer(i);

		setTextValue(mainsplitpane, h.toString());
	}

	public void setRightSplitPane(int i)
	{
		Integer h = new Integer(i);

		setTextValue(rightsplitpane, h.toString());
	}

	public void setAdvancedViewer(boolean b)
	{
		Boolean bool = new Boolean(b);

		setTextValue(advancedviewer, bool.toString());
	}

	public void setHtmlViewer(boolean b)
	{
		Boolean bool = new Boolean(b);

		setTextValue(htmlviewer, bool.toString());
	}

	/**************************************************** get *********************************/

	public boolean isMaximized()
	{
		String s = getTextValue(maximized);

		Boolean bool = new Boolean(s);

		return bool.booleanValue();
	}
	
	public boolean getHeaderAscending()
	{
		String s = getTextValue(headerascending);

		Boolean bool = new Boolean(s);

		return bool.booleanValue();
	}

	public boolean getAdvancedViewer()
	{
		String s = getTextValue(advancedviewer);

		Boolean bool = new Boolean(s);

		return bool.booleanValue();
	}

	public boolean getHtmlViewer()
	{
		String s = getTextValue(htmlviewer);

		Boolean bool = new Boolean(s);

		return bool.booleanValue();
	}

	public String getSelectedHeader()
	{
		return getTextValue(selectedheader);
	}

	public int getXPosition()
	{
		Integer i = new Integer(getTextValue(xposition));

		return i.intValue();
	}

	public int getYPosition()
	{
		Integer i = new Integer(getTextValue(yposition));

		return i.intValue();
	}

	public int getWidth()
	{
		Integer i = new Integer(getTextValue(width));

		return i.intValue();
	}

	public int getHeight()
	{
		Integer i = new Integer(getTextValue(height));

		return i.intValue();
	}

	public int getMainSplitPane()
	{
		Integer i = new Integer(getTextValue(mainsplitpane));

		return i.intValue();
	}

	public int getRightSplitPane()
	{
		Integer i = new Integer(getTextValue(rightsplitpane));

		return i.intValue();
	}

	public String getShowToolbar()
	{
		return getTextValue(showToolbar);
	}

	public String getShowAddressbook()
	{
		return getTextValue(showAddressbook);
	}

	public boolean isShowAddressbook()
	{
		return getShowAddressbook().equals("true");
	}

	public boolean isShowToolbar()
	{
		return getShowToolbar().equals("true");
	}

	public boolean isToolbarShowText()
	{
		return getTextValue(toolbarshowtext).equals("true");
	}

	public boolean isToolbarShowIcon()
	{
		return getTextValue(toolbarshowicon).equals("true");
	}

	public boolean isToolbarTextPosition()
	{
		return getTextValue(toolbartextposition).equals("true");
	}

	public String getShowFilterToolbar()
	{
		return getTextValue(showFilterToolbar);
	}

	public boolean isShowFilterToolbar()
	{
		return getShowFilterToolbar().equals("true");
	}

	public String getShowFolderInfo()
	{
		return getTextValue(showFolderInfo);
	}

	public boolean isShowFolderInfo()
	{
		return getShowFolderInfo().equals("true");
	}

	public Point getPoint()
	{
		Point point = new Point();

		point.x = getXPosition();
		point.y = getYPosition();

		return point;
	}

	public int getToolbarState()
	{
		boolean icon = isToolbarShowIcon();
		boolean text = isToolbarShowText();
		boolean pos = isToolbarTextPosition();

		if ( ( icon == true ) && ( text == true ) && ( pos == true ) )
			return 2;
		else if ( ( icon == true ) && ( text == true ) && ( pos == false ) )
			return 3;
		else if ( ( icon == true ) && ( text == false ) )
			return 0;
		else if ( ( icon == false) && ( text == true ) )
			return 1;

		else return 2;
	}

	public void setToolbarState( int i )
	{
		if ( i==0 )
		{
			setTextValue( toolbarshowicon, "true" );
			setTextValue( toolbarshowtext, "false" );
		}
		else if ( i==1 )
		{
			setTextValue( toolbarshowicon, "false" );
			setTextValue( toolbarshowtext, "true" );
		}
		else if ( i==2 )
		{
			setTextValue( toolbarshowicon, "true" );
			setTextValue( toolbarshowtext, "true" );
			setTextValue( toolbartextposition, "true" );
		}
		else if ( i==3 )
		{
			setTextValue( toolbarshowicon, "true" );
			setTextValue( toolbarshowtext, "true" );
			setTextValue( toolbartextposition, "false" );
		}
	}

	public Dimension getDimension()
	{
		Dimension dim = new Dimension();

		dim.width = getWidth();
		dim.height = getHeight();

		return dim;
	}

}