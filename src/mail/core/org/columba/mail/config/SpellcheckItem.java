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

package org.columba.mail.config;

import org.columba.core.config.AdapterNode;
import org.columba.core.config.DefaultItem;
import org.columba.mail.spellcheck.ASpellInterface;
import org.w3c.dom.Document;

/**
 * @version 	1.0
 * @author
 */
public class SpellcheckItem extends DefaultItem
{
	private AdapterNode aspellExecutable;
	private AdapterNode limit;

	private AdapterNode rootNode;

	public SpellcheckItem(AdapterNode rootNode, Document doc)
	{
		super(doc);

		this.rootNode = rootNode;

		parse();

		createMissingElements();

	}

	public AdapterNode getRootNode()
	{
		return rootNode;
	}

	protected void parse()
	{
		for (int i = 0; i < getRootNode().getChildCount(); i++)
		{
			AdapterNode child = getRootNode().getChildAt(i);

			if (child.getName().equals(ASpellInterface.ASPELL_EXE_PROP))
			{
				aspellExecutable = child;
			}
			else if (child.getName().equals("limit"))
			{
				limit = child;
			}
		}
	}

	protected void createMissingElements()
	{
		if (aspellExecutable == null)
			aspellExecutable = addKey(rootNode, ASpellInterface.ASPELL_EXE_PROP, "aspell.exe");
		if (limit == null)
			limit = addKey(rootNode, "limit", "5");

	}

	public void setAspellExecutable(String str)
	{
		setTextValue(aspellExecutable, str);
	}

	public String getAspellExecutable()
	{
		return getTextValue(aspellExecutable);
	}

	public String getLimit()
	{
		return getTextValue(limit);
	}

	public void setLimit(String s)
	{
		setTextValue(limit, s);
	}

}