//The contents of this file are subject to the Mozilla Public License Version 1.1
//(the "License"); you may not use this file except in compliance with the 
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License 
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.

package org.columba.mail.gui.composer.util;

import javax.swing.text.DefaultStyledDocument;
import javax.swing.undo.UndoManager;

public class UndoDocument extends DefaultStyledDocument
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



