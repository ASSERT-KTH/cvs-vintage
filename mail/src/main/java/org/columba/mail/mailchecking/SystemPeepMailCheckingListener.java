package org.columba.mail.mailchecking;

import java.awt.Toolkit;

public class SystemPeepMailCheckingListener implements IMailCheckingListener {

	public SystemPeepMailCheckingListener() {
		super();
	}

	public void newMessageArrived(IMailCheckingEvent event) {
		Toolkit kit = Toolkit.getDefaultToolkit();
		kit.beep(); //system beep
	}

}
