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

package org.columba.mail.gui.composer;

import org.columba.mail.config.MailConfig;
import org.columba.mail.config.SpellcheckItem;
import org.columba.mail.spellcheck.ASpellInterface;

public class ComposerSpellCheck
{
    private ComposerInterface composerInterface=null;
    private SpellcheckItem spellCheckConfig=null;

	public ComposerSpellCheck(ComposerInterface iface)
    {
		composerInterface = iface;
        spellCheckConfig = MailConfig
					.getComposerOptionsConfig()
					.getSpellcheckItem();
        System.out.println("Filename is " + spellCheckConfig.get("executable"));
        ASpellInterface.setAspellExeFilename(spellCheckConfig.get("executable"));
        
	}

	public String checkText(String text)
    {
        String checked =  ASpellInterface.checkBuffer(text);

        if (checked == null)
        {
            // Display error ?
            // As it is inmutable
            return text;
        }
        else
        {
            return checked;
        }
	}
}
