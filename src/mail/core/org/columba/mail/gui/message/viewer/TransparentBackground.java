package org.columba.mail.gui.message.viewer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;

import javax.swing.JComponent;
import javax.swing.JDialog;

public class TransparentBackground extends JComponent {

	private JDialog dialog;

	private Image background;

	public TransparentBackground(JDialog dialog) {
		this.dialog = dialog;
		setOpaque(true);
		
		//updateBackground();
	}

	private void updateBackground() {
		try {
			Robot robot = new Robot();
			Toolkit tk = Toolkit.getDefaultToolkit();
			Dimension dim = tk.getScreenSize();
			background = robot.createScreenCapture(new Rectangle(0, 0,
					(int) dim.getWidth(), (int) dim.getHeight()));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void paintComponent(Graphics g) {
		Point pos = this.getLocationOnScreen();
		Point offset = new Point(-pos.x, -pos.y);
		
//		g.drawImage(background, offset.x, offset.y, null);
		
		g.setColor(Color.LIGHT_GRAY);

		int x = 0;
		int y = 0;
		int width = getWidth();
		int height = getHeight();

		g.drawRoundRect(x, y, width - 1, height - 1, 5, 5);
	}
}
