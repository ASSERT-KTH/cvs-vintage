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


package org.columba.mail.gui.composer.util;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.undo.*;
import java.awt.*;

import org.columba.mail.gui.message.util.MessageDocument;

public class UndoDocument extends MessageDocument
{
    private UndoManager undoManager;

    public UndoDocument()
        {
            super();

            undoManager = new UndoManager();
            this.addUndoableEditListener( undoManager );
        }

    public void Undo()
        {
            if( undoManager.canUndo() )
                undoManager.undo();
        }

    public void Redo()
        {
            if( undoManager.canRedo() )
                undoManager.redo();
        }
}



