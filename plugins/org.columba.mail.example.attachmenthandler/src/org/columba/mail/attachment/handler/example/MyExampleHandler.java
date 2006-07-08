package org.columba.mail.attachment.handler.example;
import java.awt.Toolkit;
import java.io.IOException;

import org.columba.mail.gui.attachment.IAttachmentContext;
import org.columba.mail.gui.attachment.IAttachmentHandler;

public class MyExampleHandler implements IAttachmentHandler {

	public MyExampleHandler() {
	}

	public void execute(IAttachmentContext context)
			throws IllegalArgumentException {

		System.out.println("filename="+context.getFileName());
		try {
			System.out.println("inputstream length="+context.getContent().available());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// system beep
		Toolkit kit = Toolkit.getDefaultToolkit();
		kit.beep();
	}

}
