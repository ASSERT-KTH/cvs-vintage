import java.awt.event.ActionEvent;

import org.columba.core.action.FrameAction;
import org.columba.core.gui.frame.AbstractFrameController;
import org.columba.core.gui.selection.SelectionChangedEvent;
import org.columba.core.gui.selection.SelectionListener;
import org.columba.core.main.MainInterface;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.gui.frame.AbstractMailFrameController;
import org.columba.mail.gui.tree.selection.TreeSelectionChangedEvent;



/**
 * @author fdietz
 *
 *
 */
public class AnalyzeFolderAction extends FrameAction implements SelectionListener {

	/**
	 * @param frameController
	 * @param name
	 * @param longDescription
	 * @param actionCommand
	 * @param small_icon
	 * @param big_icon
	 * @param mnemonic
	 * @param keyStroke
	 */
	public AnalyzeFolderAction(AbstractFrameController frameController) {
		super(
			frameController,
			"Analyze Folder",
			"Analyze all messages of selected Folder",
			"ANALYZE_FOLDER",
			null,
			null,
			'0',
			null);
		setEnabled(false);
		(
			(
				AbstractMailFrameController) frameController)
					.registerTreeSelectionListener(
			this);
	}

	/* (non-Javadoc)
			 * @see org.columba.core.gui.util.SelectionListener#selectionChanged(org.columba.core.gui.util.SelectionChangedEvent)
			 */
	public void selectionChanged(SelectionChangedEvent e) {

		if (((TreeSelectionChangedEvent) e).getSelected().length > 0)
			setEnabled(true);
		else
			setEnabled(false);

	}

	/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
	public void actionPerformed(ActionEvent evt) {
		FolderCommandReference[] r =
			((AbstractMailFrameController) getFrameController())
				.getTreeSelection();

		
		MainInterface.processor.addOp(new AnalyzeFolderCommand(r));
	}
}

