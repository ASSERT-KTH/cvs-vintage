// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

package org.columba.main;

import org.columba.core.util.*;

import java.util.*;

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