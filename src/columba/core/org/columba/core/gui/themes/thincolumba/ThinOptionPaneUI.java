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
package org.columba.core.gui.themes.thincolumba;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicOptionPaneUI;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ThinOptionPaneUI extends BasicOptionPaneUI {

	private static String newline;
	
	public static ComponentUI createUI(JComponent c) {
		return new ThinOptionPaneUI();
	}
	
	protected void addMessageComponents(
		Container container,
		GridBagConstraints cons,
		Object msg,
		int maxll,
		boolean internallyCreated) {
		if (msg == null) {
			return;
		}
		if (msg instanceof Component) {
			// To workaround problem where Gridbad will set child
			// to its minimum size if its preferred size will not fit
			// within allocated cells
			if (msg instanceof JScrollPane || msg instanceof JPanel) {
				cons.fill = GridBagConstraints.BOTH;
				cons.weighty = 1;
			} else {
				cons.fill = GridBagConstraints.HORIZONTAL;
			}
			cons.weightx = 1;

			container.add((Component) msg, cons);
			cons.weightx = 0;
			cons.weighty = 0;
			cons.fill = GridBagConstraints.NONE;
			cons.gridy++;
			if (!internallyCreated) {
				hasCustomComponents = true;
			}

		} else if (msg instanceof Object[]) {
			Object[] msgs = (Object[]) msg;
			for (int i = 0; i < msgs.length; i++) {
				addMessageComponents(container, cons, msgs[i], maxll, false);
			}

		} else if (msg instanceof Icon) {
			JLabel label = new JLabel((Icon) msg, SwingConstants.CENTER);
			configureMessageLabel(label);
			addMessageComponents(container, cons, label, maxll, true);

		} else {
			String s = msg.toString();
			int len = s.length();
			if (len <= 0) {
				return;
			}
			int nl = -1;
			int nll = 0;

			if ((nl = s.indexOf(newline)) >= 0) {
				nll = newline.length();
			} else if ((nl = s.indexOf("\r\n")) >= 0) {
				nll = 2;
			} else if ((nl = s.indexOf('\n')) >= 0) {
				nll = 1;
			}
			if (nl >= 0) {
				// break up newlines
				if (nl == 0) {
					addMessageComponents(container, cons, new Component() {
						public Dimension getPreferredSize() {
							Font f = getFont();

							if (f != null) {
								return new Dimension(1, f.getSize() + 2);
							}
							return new Dimension(0, 0);
						}
					}, maxll, true);
				} else {
					addMessageComponents(
						container,
						cons,
						s.substring(0, nl),
						maxll,
						false);
				}
				addMessageComponents(
					container,
					cons,
					s.substring(nl + nll),
					maxll,
					false);

			} else if (len > maxll) {
				Container c = Box.createVerticalBox();
				burstStringInto(c, s, maxll);
				addMessageComponents(container, cons, c, maxll, true);

			} else {
				JLabel label;
				label = new JLabel(s, JLabel.LEADING);
				configureMessageLabel(label);
				addMessageComponents(container, cons, label, maxll, true);
			}
		}
	}
	
	 private void configureMessageLabel(JLabel label) {
        label.setForeground(UIManager.getColor(
                            "OptionPane.messageForeground"));
        Font messageFont = UIManager.getFont("OptionPane.messageFont");
        if (messageFont != null) {
            label.setFont(messageFont);
        }
    }
    
   
}
