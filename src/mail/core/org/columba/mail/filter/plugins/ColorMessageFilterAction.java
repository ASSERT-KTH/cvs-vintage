package org.columba.mail.filter.plugins;

import java.awt.Color;

import org.columba.core.command.Command;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.filter.FilterAction;
import org.columba.mail.folder.Folder;
import org.columba.mail.folder.command.ColorMessageCommand;

/**
 * This Filter Action colors a message with a specified color. The action
 * retrieves the integer attribute "<b>rgb</b>" from the filter action as
 * the color to color the message with. In order to keep the memory consumtion
 * up, it creates the <code>Color</code> object by using the <code>ColorFactory</code>.
 * The message is colored by setting the "<b>columba.color</b>" header.
 * 
 * @author redsolo
 */
public class ColorMessageFilterAction extends AbstractFilterAction {

	/** {@inheritDoc} */
	public Command getCommand(
		FilterAction filterAction,
		Folder srcFolder,
		Object[] uids)
		throws Exception {

		int rgb = filterAction.getInteger("rgb", Color.black.getRGB());
		
		// create reference
		FolderCommandReference[] r = new FolderCommandReference[1];
		r[0] = new FolderCommandReference(srcFolder, uids);
		r[0].setColorValue(rgb);

		// create command
		return new ColorMessageCommand(r);

	}
}