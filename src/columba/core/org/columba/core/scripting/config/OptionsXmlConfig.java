/*
  The contents of this file are subject to the Mozilla Public License Version 1.1
  (the "License"); you may not use this file except in compliance with the 
  License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
  
  Software distributed under the License is distributed on an "AS IS" basis,
  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License 
  for the specific language governing rights and
  limitations under the License.

  The Original Code is "The Columba Project"
  
  The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
  Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
  
  All Rights Reserved.
*/
package org.columba.core.scripting.config;

import java.io.File;

import org.columba.core.config.DefaultXmlConfig;
import org.columba.core.xml.XmlElement;

public class OptionsXmlConfig extends DefaultXmlConfig {

	private static final String OPTIONS_KEY = "options", OPTIONS_PATH = "/"
			+ OPTIONS_KEY;

	private Options options;

	public OptionsXmlConfig(File file) {
		super(file);
	}

	public Options getOptions() {
		if (options == null)
			options = new Options(getRoot().getElement(OPTIONS_PATH));

		return options;
	}

	public boolean load() {
		boolean superRes = super.load();
		if (getRoot().getElement(OPTIONS_PATH) == null) {
			XmlElement optionsElement = new XmlElement(OPTIONS_KEY);
			getRoot().addElement(optionsElement);
			optionsElement.addElement(new XmlElement(
					Options.POLLING_INTERVAL_KEY));
			optionsElement.addElement(new XmlElement(
					Options.POLLING_ENABLED_KEY));
			getOptions().setDefaultData();
		}

		return superRes;
	}
}
