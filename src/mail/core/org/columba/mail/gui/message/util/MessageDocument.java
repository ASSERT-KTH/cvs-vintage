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

package org.columba.mail.gui.message.util;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

public class MessageDocument extends DefaultStyledDocument
{
	private static final int MAX_ATTR = 5;

	public static final int NONE = 0;
	public static final int BOLD = 1;
	public static final int ITALIC = 2;
	public static final int LINK = 3;
	public static final int INLINE_MESSAGE = 4;

	private Caret caret;
	private String ident;
	private int identLevel;

	private SimpleAttributeSet[] attr = new SimpleAttributeSet[MAX_ATTR];

	public MessageDocument()
	{
		int i;

		identLevel = 0;
		ident = ">";

	}

	public void setFont(Font font)
	{
		String name = font.getName();

		for (int i = 0; i < MAX_ATTR; i++)
		{
			attr[i] = new SimpleAttributeSet();
			StyleConstants.setFontFamily(attr[i], name);
		}
		
		StyleConstants.setBold(attr[1], true);
		StyleConstants.setItalic(attr[2], true);
		StyleConstants.setUnderline(attr[3], true);
		StyleConstants.setForeground(attr[3], Color.blue);
		StyleConstants.setForeground(attr[4], Color.gray);
	}
	public void setIdent(String id)
	{
		ident = id;
	}

	public int incIdentLevel()
	{
		return ++identLevel;
	}

	public int decIdentLevel()
	{
		return --identLevel;
	}

	public void insertString(int offs, String str, AttributeSet a)
	{
		try
		{
			super.insertString(offs, str, a);
		}
		catch (BadLocationException e)
		{
		}
	}

	public void reset()
	{
		identLevel = 0;

		try
		{
			super.remove(0, super.getLength());
		}
		catch (BadLocationException e)
		{
		}
	}

	public void append(String str, int a)
	{
		if ((a < 0) | (a > MAX_ATTR))
			a = 0;

		try
		{
			super.insertString(super.getLength(), str, attr[a]);
		}
		catch (BadLocationException e)
		{
		}

	}

	public void setText(String str, int a)
	{
		identLevel = 0;

		System.out.println("messagedocument->append");
		if ((a < 0) | (a > MAX_ATTR))
			a = 0;

		try
		{
			super.remove(0, super.getLength());
			super.insertString(0, str, attr[a]);
		}
		catch (BadLocationException e)
		{
		}
	}

	public void setCaret(JTextComponent textPane)
	{
		textPane.setCaretPosition(0);
	}

	public String getText()
	{
		String output = null;

		try
		{
			output = super.getText(0, super.getLength());
		}
		catch (BadLocationException e)
		{
		}
		return output;
	}

	public void setStyle(int pos, int length, int i)
	{
		setCharacterAttributes(pos, length, attr[i], true);
	}

	protected boolean isCharacter(char c)
	{
		boolean result = false;

		if (Character.isLetterOrDigit(c) == true)
			result = true;
		else
			result = false;

		if (c == '.')
			result = true;
		if (c == ' ')
			result = false;
		if (c == '-')
			result = true;
		if (c == '_')
			result = true;
		if (c == '@')
			result = true;
		if (c == '?')
			result = true;
		if (c == '=')
			result = true;

		return result;
	}

	public int getLeft(String str, int pos)
	{
		int index = pos;
		char c = str.charAt(index);

		while (isCharacter(c) == true)
		{
			c = str.charAt(index);
			if (index - 1 < 0)
				return 0;
			index--;
		}

		return index + 2;
	}

	public int getRight(String str, int pos)
	{
		int index = pos;
		char c = str.charAt(index);

		while (isCharacter(c) == true)
		{
			c = str.charAt(index);
			if (index + 1 > str.length() - 1)
				return str.length() - 1;
			index++;
		}
		return index - 1;
	}

	public void parse()
	{
		String str = getText();

		parseInlineMessage(str);

		parseAddress(str);

		parseUrl(str);
	}

	public void parseLine(int position)
	{
		/*
		String str = getText();

		int right = gotoLineEnd( str, position );
		*/
		String str = getText();

		parseInlineMessage(str);

		parseAddress(str);

		parseUrl(str);
		
		System.out.println("---parsing document");
    }

	public void parseAddress(String str)
	{
		int pos = 0;
		String newLine;
		char c;
		int index;
		StringBuffer buf = new StringBuffer();
		while (pos < str.length())
		{
			c = str.charAt(pos);

			if (c == '@')
			{
				int left = getLeft(str, pos - 1);
				int right = getRight(str, pos + 1);

				//System.out.println(  left+ " : "+ right );

				if ((left != -1) && (right != -1))
				{

					if ((right - left) > 2)
						setStyle(left, (right - left), 3);
				}

			}
			pos++;
		}
	}

	protected boolean isUrlCharacter(char c)
	{
		boolean result = false;

		if (Character.isLetterOrDigit(c) == true)
			result = true;
		else
			result = false;

		if (c == '.')
			result = true;
		else if (c == ' ')
			result = false;
		else if (c == '-')
			result = true;
		else if (c == '_')
			result = true;
		else if (c == '/')
			result = true;
		else if (c == ':')
			result = true;
		else if (c == '?')
			result = true;
		else if (c == '&')
			result = true;
		else if (c == '=')
			result = true;
		else if (c == '$')
			result = true;
		else if (c == '#')
			result = true;
		else if (c == ':')
			result = true;
		else if (c == '+')
			result = true;
		else if (c == '~')
			result = true;
		else if (c == '!')
			result = true;
		else if (c == '%')
			result = true;

		return result;
	}

	protected boolean findHttp(String str, int pos)
	{
		int index = pos;
		char c = str.charAt(index);

		if (c == 'h')
		{
			index++;
			c = str.charAt(index);
			if (c == 't')
			{
				index++;
				c = str.charAt(index);
				if (c == 't')
				{
					index++;
					c = str.charAt(index);
					if (c == 'p')
					{
						index++;
						c = str.charAt(index);
						if (c == ':')
						{
							index++;
							c = str.charAt(index);
							if (c == '/')
							{
								index++;
								c = str.charAt(index);
								if (c == '/')
								{
									index++;
									c = str.charAt(index);

									return true;
								}
							}
						}
					}
				}
			}

		}

		return false;
	}

	public int getUrlRight(String str, int pos)
	{
		int index = pos;
		char c = str.charAt(index);

		while ( isUrlCharacter(c) == true) 
		{
			if ( index>=str.length() )
			{
				return str.length();
			}
			
			c = str.charAt(index);
			index++;
		}

		return index - 1;
	}

	public void parseUrl(String str)
	{
		int pos = 0;

		char c;

		while (pos < str.length())
		{
			c = str.charAt(pos);

			if (c == 'h')
			{
				boolean result = findHttp(str, pos);

				if (result == true)
				{

					int right = getUrlRight(str, pos);

					setStyle(pos, right - pos, 3);

					pos += right - pos;
				}
				else
				{

				}
			}
			pos++;
		}
	}

	protected int gotoLineEnd(String str, int pos)
	{
		char c = '0';

		if (pos < str.length())
			c = str.charAt(pos);
		else
			return str.length() - 1;

		while ((pos < str.length()) && (c != '\n'))
		{
			if (pos < str.length())
				c = str.charAt(pos);
			//System.out.println("pos: "+ pos +"  char: "+c);
			pos++;
		}

		return pos;
	}
	
	protected int gotoLineStart( String str, int pos )
	{
		return pos;
	}

	protected int markInlineMessage(String str, int pos)
	{
		int startIndex;
		int endIndex;

		// found starting sign
		pos++;
		char c = str.charAt(pos);
		startIndex = pos;

		while (c == '>')
		{
			pos = gotoLineEnd(str, pos);

			if (pos < str.length())
				c = str.charAt(pos);

			//System.out.println("------pos: "+ pos +"  char: "+c);
		}

		// found end sign
		pos--;

		endIndex = pos;

		setStyle(startIndex, endIndex - startIndex, 4);

		return pos;
	}

	public void parseInlineMessage(String str)
	{
		int pos = 0;

		char c = str.charAt(pos);

		if (c == '>')
		{
			pos = markInlineMessage(str, pos - 1);
		}

		while (pos < str.length())
		{
			c = str.charAt(pos);

			//System.out.println("1pos: "+ pos +"  char: "+c);

			if ((c == '\n') && (pos + 1 < str.length()))
			{
				if (str.charAt(pos + 1) == '>')
				{
					pos = markInlineMessage(str, pos);
				}
			}

			pos++;
		}
	}

}