package org.columba.core.xml;
import java.io.IOException;

public class XmlTest {
	public XmlTest() {
	}

	
	public static void main(String argv[]) {
		if (argv.length != 1) {
			System.err.println("Usage: cmd filename");
			System.exit(1);
		}

		XmlIO X = new XmlIO();
		X.load(argv[0]);

		XmlElement.printNode(X.getRoot(), "");

		System.out.println("---------------------------------------------");
		XmlElement E = X.getRoot().getElement("options");
		if (E != null) {
			System.out.println("options: '" + E.getData() + "'");
		}
		E = X.getRoot().getElement("/options/gui/window/width");
		if (E != null) {
			System.out.println(
				"options/gui/window/width: '" + E.getData() + "'");
		} else {
			System.out.println(
				"options/gui/window/width: "
					+ "**Not found in this XML document**");
		}

		System.out.println("---------------------------------------------");

		try {
			X.write(System.out);
		} catch (IOException e) {
			System.out.println("Error in write: " + e.toString());
		}
	}
}
