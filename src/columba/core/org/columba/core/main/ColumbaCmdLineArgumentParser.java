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
package org.columba.core.main;

import org.columba.core.util.CmdLineArgumentParser;

public class ColumbaCmdLineArgumentParser extends CmdLineArgumentParser
{
	public static final Option DEBUG =
		new CmdLineArgumentParser.Option.BooleanOption('d', "debug");

	public static final Option COMPOSER =
		new CmdLineArgumentParser.Option.BooleanOption('c', "composer");

	public static final Option RCPT =
		new CmdLineArgumentParser.Option.StringOption('r', "rcpt");

	public static final Option MESSAGE =
		new CmdLineArgumentParser.Option.StringOption('b', "body");

	public static final Option PATH =
		new CmdLineArgumentParser.Option.StringOption('p', "path");

	public static final Option MAILURL =
		new CmdLineArgumentParser.Option.StringOption('u', "mailurl");

	public static final Option SUBJECT =
		new CmdLineArgumentParser.Option.StringOption('s', "subject");

	public static final Option CC =
		new CmdLineArgumentParser.Option.StringOption('x', "cc");

	public static final Option BCC =
		new CmdLineArgumentParser.Option.StringOption('x', "bcc");


	public ColumbaCmdLineArgumentParser()
	{
		super();

		addOption(DEBUG);
		addOption(COMPOSER);
		addOption(RCPT);
		addOption(MESSAGE);
		addOption(PATH);
		addOption(MAILURL);
	}
	
	public static void printUsage()
	{
		 System.err.println("usage: java -jar columba.jar [{-d,--debug}] [{-c,--composer}]"+
                           "[{-r,--rcpt} recipient] [{-b,--body} message body] [{-p,--path} config path]");
	}
}