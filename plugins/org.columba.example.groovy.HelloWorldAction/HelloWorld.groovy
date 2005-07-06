
import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.columba.core.plugin.IExtensionInterface;
import org.columba.core.action.AbstractColumbaAction;
import org.columba.core.gui.frame.FrameMediator;

public class HelloWorldAction extends AbstractColumbaAction implements IExtensionInterface{
	public HelloWorldAction(FrameMediator controller) {
		super(controller, "Hello, World!")

		putValue(AbstractColumbaAction.SHORT_DESCRIPTION, "Show me this tooltip, please")
	}
	
	public void actionPerformed(ActionEvent evt) {
		JOptionPane.showMessageDialog(null, "Hello World!")
	}
}