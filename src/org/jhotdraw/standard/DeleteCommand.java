/*
 * @(#)DeleteCommand.java
 *
 * Project:		JHotdraw - a GUI framework for technical drawings
 *				http://www.jhotdraw.org
 *				http://jhotdraw.sourceforge.net
 * Copyright:	� by the original author(s) and all contributors
 * License:		Lesser GNU Public License (LGPL)
 *				http://www.opensource.org/licenses/lgpl-license.html
 */

package CH.ifa.draw.standard;

import CH.ifa.draw.framework.*;
import CH.ifa.draw.util.*;

/**
 * Command to delete the selection.
 *
 * @version <$CURRENT_VERSION$>
 */
public class DeleteCommand extends FigureTransferCommand {

   /**
    * Constructs a delete command.
    * @param name the command name
    * @param view the target view
    */
    public DeleteCommand(String name, DrawingView view) {
        super(name, view);
    }

    public void execute() {
    	setUndoActivity(createUndoActivity());
    	getUndoActivity().setAffectedFigures(view().selectionElements());
        deleteFigures(getUndoActivity().getAffectedFigures());
        view().checkDamage();
    }

    public boolean isExecutable() {
        return view().selectionCount() > 0;
    }

	/**
	 * Factory method for undo activity
	 */
	protected Undoable createUndoActivity() {
		return new DeleteCommand.UndoActivity(this);
	}

	public static class UndoActivity extends UndoableAdapter {
		private FigureTransferCommand myCommand;
		
		public UndoActivity(FigureTransferCommand newCommand) {
			super(newCommand.view());
			myCommand = newCommand;
	        setUndoable(true);
	        setRedoable(true);
		}
		
		public boolean undo() {
			if (super.undo() && getAffectedFigures().hasMoreElements()) {
				getDrawingView().clearSelection();
		        setAffectedFigures(myCommand.insertFigures(getAffectedFigures(), 0, 0));
	
			    return true;
			}
			
	        return false;
		}
	
		public boolean redo() {
			// do not call execute directly as the selection might has changed
			if (isRedoable()) {
	        	myCommand.deleteFigures(getAffectedFigures());
				getDrawingView().clearSelection();

				return true;
			}
			
			return false;
		}
	}
}
