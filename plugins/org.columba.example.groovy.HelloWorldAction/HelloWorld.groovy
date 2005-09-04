
import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.columba.api.plugin.IExtensionInterface;
import org.columba.core.gui.action.AbstractColumbaAction;
import org.columba.api.gui.frame.IFrameMediator;

public class HelloWorldAction extends AbstractColumbaAction implements IExtensionInterface{
	public HelloWorldAction(IFrameMediator controller) {
		super(controller, "Hello, World!")

		putValue(AbstractColumbaAction.SHORT_DESCRIPTION, "Show me this tooltip, please")
	}
	
	public void actionPerformed(ActionEvent evt) {
		JOptionPane.showMessageDialog(null, "Hello World!")
	}
}