/*
 * Created on 11.03.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import org.columba.core.action.FrameAction;
import org.columba.core.gui.frame.AbstractFrameController;
import org.columba.core.gui.selection.SelectionChangedEvent;
import org.columba.core.gui.selection.SelectionListener;
import org.columba.core.main.MainInterface;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.gui.frame.AbstractMailFrameController;
import org.columba.mail.gui.table.selection.TableSelectionChangedEvent;
import org.columba.mail.util.MailResourceLoader;

/**
 * @author frd
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class BounceAction extends FrameAction implements SelectionListener {

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
	public BounceAction(AbstractFrameController frameController) {
		super(
			frameController,
			MailResourceLoader.getString(
				"menu",
				"mainframe",
				"menu_message_bounce"),
			MailResourceLoader.getString(
				"menu",
				"mainframe",
				"menu_message_bounce_tooltip"),
			"BOUNCE",
			null,
			null,
			'B',
		// we must see if the Key not alrady used
		KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK));
		setEnabled(false);
		(
			(
				AbstractMailFrameController) frameController)
					.registerTableSelectionListener(
			this);

	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		FolderCommandReference[] r =
			((AbstractMailFrameController) getFrameController())
				.getTableSelection();
		MainInterface.processor.addOp(new BounceCommand(r));
	}

	/* (non-Javadoc)
		 * @see org.columba.core.gui.util.SelectionListener#selectionChanged(org.columba.core.gui.util.SelectionChangedEvent)
		 */
	public void selectionChanged(SelectionChangedEvent e) {

		if (((TableSelectionChangedEvent) e).getUids().length > 0)
			setEnabled(true);
		else
			setEnabled(false);

	}

}
