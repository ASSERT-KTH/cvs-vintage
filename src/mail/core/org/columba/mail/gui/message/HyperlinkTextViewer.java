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

package org.columba.mail.gui.message;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;

import org.columba.core.config.Config;
import org.columba.mail.gui.message.util.MessageDocument;
import org.columba.mail.gui.util.URLController;

public class HyperlinkTextViewer
	extends JTextPane
	implements DocumentViewer, MouseMotionListener, MouseListener
{
	boolean active = false;

	private MessageDocument document;

	private JMenuItem menuItem;
	private JPopupMenu popup;

	private String address;
	private String link;

	public HyperlinkTextViewer()
	{
		super();
		document = new MessageDocument();
		Font font =
			new Font(
				Config.getOptionsConfig().getThemeItem().getTextFontName(),
				Font.PLAIN,
				Config.getOptionsConfig().getThemeItem().getTextFontSize());
		document.setFont(font);
		setStyledDocument(document);
		setMargin(new Insets(5, 5, 5, 5));
		setEditable(false);
		addMouseListener(new PopupListener());
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	public String getSelectedText()
	{
		return super.getSelectedText();
	}

	public String getAddress()
	{
		return address;
	}

	public String getLink()
	{
		return link;
	}

	public void setHeader( Component header )
	{
		insertComponent( header );
	}
	
	public void setDoc(String s)
	{
		//System.out.println("hyperlinktextviewer->setText");

		document.reset();

		if (s == null) return;

		if (s.length() > 0)
		{
			document.append(s, 0);
			document.parse();
			setCaretPosition(0);
		}
	}

	public String getDoc()
	{
		return document.getText();
	}

	public void setActive(boolean b)
	{
		active = true;
	}

	public boolean isActive()
	{
		return active;
	}

	public void clearDoc()
	{
		document.reset();
	}

	public boolean isLink(int pos)
	{
		Element element = document.getCharacterElement(pos);
		AttributeSet set = element.getAttributes();
		Color color = StyleConstants.getForeground(set);
		return (color != null && color == Color.blue);
	}

	protected String getURI(String str, int pos)
	{
		int index1 = pos;
		char c = str.charAt(index1);
		StringBuffer left = new StringBuffer();
		while (isLink(index1))
		{
			if (index1 < 0)
				break;
			c = str.charAt(index1);
			left.insert(0, c);
			index1--;
		}
		System.out.println("left: " + left);

		int index2 = pos + 1;
		c = str.charAt(index2);
		StringBuffer right = new StringBuffer();
		while (isLink(index2))
		{
			if (index2 >= str.length())
				break;
			c = str.charAt(index2);
			right.append(c);
			index2++;
		}
		System.out.println("right: " + right);

		String result = left.toString() + right.toString();
		System.out.println("str: " + result);

		return result;
	}

	public void mouseMoved(MouseEvent e)
	{

		java.awt.Point point = e.getPoint();

		int pos = viewToModel(point);

		Element element = document.getCharacterElement(pos);
		AttributeSet set = element.getAttributes();

		Color color = StyleConstants.getForeground(set);

		if (color != null)
		{
			if (color == Color.blue)
			{
				if (getCursor().getType() != Cursor.HAND_CURSOR)
					setCursor(new Cursor(Cursor.HAND_CURSOR));
			}
			else
			{
				if (getCursor().getType() != Cursor.DEFAULT_CURSOR)
					setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		}
		else
		{
			if (getCursor().getType() != Cursor.DEFAULT_CURSOR)
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}

	}

	public void mouseDragged(MouseEvent e)
	{
	}

	public void mouseClicked(MouseEvent e)
	{

		java.awt.Point point = e.getPoint();

		int pos = viewToModel(point);

		if (isLink(pos))
		{
			String str = document.getText();

			String result = getURI(str, pos);

			if (result.indexOf("@") != -1)
			{
				// e-mail address

				address = result;

				URLController controller = new URLController();
				controller.setAddress(address);
				controller.createContactMenu(address).show(e.getComponent(), point.x, point.y);

			}
			else
			{

				link = result;

				System.out.println(" no popup trigger");

				URLController controller = new URLController();
				try{
					controller.open(new URL(link));
				}catch(MalformedURLException mue){}
			}
		}

	}

	public void mouseEntered(MouseEvent e)
	{
	}

	public void mouseExited(MouseEvent e)
	{
	}

	public void mousePressed(MouseEvent e)
	{
	}

	public void mouseReleased(MouseEvent e)
	{
	}

	class PopupListener extends MouseAdapter
	{
		public void mousePressed(MouseEvent e)
		{
			maybeShowPopup(e);
		}

		public void mouseReleased(MouseEvent e)
		{
			maybeShowPopup(e);
		}

		private void maybeShowPopup(MouseEvent e)
		{
			if (e.isPopupTrigger())
			{
				java.awt.Point point = e.getPoint();

				int pos = viewToModel(point);

				if (isLink(pos))
				{
					String str = document.getText();

					String result = getURI(str, pos);

					if (result.indexOf("@") != -1)
					{
						// e-mail address

						address = result;

						URLController controller = new URLController();
						controller.setAddress(address);
						controller.createContactMenu(address).show(e.getComponent(), point.x, point.y);
					}
					else
					{
						link = result;

						URLController controller = new URLController();
						try{
							controller.setLink(new URL(link));
							controller.createLinkMenu().show(e.getComponent(), e.getX(), e.getY());
						}catch(MalformedURLException mue){}
					}
				}
			}
		}
	}

}
