/*
 * RootElement.java - For compatibility with Swing document API
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2001 Slava Pestov
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.gjt.sp.jedit.buffer;

//{{{ Imports
import javax.swing.text.*;
import org.gjt.sp.jedit.Buffer;
//}}}

/**
 * A class internal to jEdit's document model. You should not use it
 * directly.
 *
 * @author Slava Pestov
 * @version $Id: RootElement.java,v 1.1 2001/10/20 09:32:23 spestov Exp $
 * @since jEdit 4.0pre1
 */
public class RootElement implements Element
{
	//{{{ RootElement constructor
	public RootElement(Buffer buffer)
	{
		this.buffer = buffer;
	} //}}}

	//{{{ getDocument() method
	public Document getDocument()
	{
		return null;
	} //}}}

	//{{{ getParentElement() method
	public Element getParentElement()
	{
		return null;
	} //}}}

	//{{{ getName() method
	public String getName()
	{
		return null;
	} //}}}

	//{{{ getAttributes() method
	public AttributeSet getAttributes()
	{
		return null;
	} //}}}

	//{{{ getStartOffset() method
	public int getStartOffset()
	{
		return 0;
	} //}}}

	//{{{ getEndOffset() method
	public int getEndOffset()
	{
		return buffer.getLength() + 1;
	} //}}}

	//{{{ getElementIndex() method
	public int getElementIndex(int offset)
	{
		return buffer.getLineOfOffset(offset);
	} //}}}

	//{{{ getElementCount() method
	public int getElementCount()
	{
		return buffer.getLineCount();
	} //}}}

	//{{{ getElement() method
	public Element getElement(int line)
	{
		return new LineElement(buffer,line);
	} //}}}

	//{{{ isLeaf() method
	public boolean isLeaf()
	{
		return false;
	} //}}}

	//{{{ Private members
	private Buffer buffer;
	//}}}
} //}}}
