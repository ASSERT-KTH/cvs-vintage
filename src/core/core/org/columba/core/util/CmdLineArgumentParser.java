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

package org.columba.core.util;

import java.util.*;

/**
 * Thanks go to Steve Purcell who wrote the basic parser
 * and added it to the sourceforge codesnippet library
 * 
 */
public class CmdLineArgumentParser
{
	private String[] remainingArgs = null;
	private Hashtable options = new Hashtable();
	private Hashtable values = new Hashtable();

	/**
	 * Thrown when the parsed command-line contains an option that is not
	 * recognised. <code>getMessage()</code> returns
	 * an error string suitable for reporting the error to the user (in
	 * English).
	 */
	public static class UnknownOptionException extends Exception
	{
		UnknownOptionException(String optionName)
		{
			super("unknown option '" + optionName + "'");
			this.optionName = optionName;
		}
		public String getOptionName()
		{
			return this.optionName;
		}
		private String optionName = null;
	}

	/**
	 * Thrown when an illegal or missing value is given by the user for
	 * an option that takes a value. <code>getMessage()</code> returns
	 * an error string suitable for reporting the error to the user (in
	 * English).
	 */
	public static class IllegalOptionValueException extends Exception
	{
		IllegalOptionValueException(Option opt, String value)
		{
			super(
				"illegal value '"
					+ value
					+ "' for option -"
					+ opt.shortForm()
					+ "/--"
					+ opt.longForm());
			this.option = opt;
			this.value = value;
		}
		public Option getOption()
		{
			return this.option;
		}
		public String getValue()
		{
			return this.value;
		}
		private Option option;
		private String value;
	}

	/**
	 * Representation of a command-line option
	 */
	public static abstract class Option
	{

		protected Option(char shortForm, String longForm, boolean wantsValue)
		{
			if (longForm == null)
				throw new IllegalArgumentException("null arg forms not allowed");
			this.shortForm = new String(new char[] { shortForm });
			this.longForm = longForm;
			this.wantsValue = wantsValue;
		}

		public String shortForm()
		{
			return this.shortForm;
		}

		public String longForm()
		{
			return this.longForm;
		}

		/**
		 * Tells whether or not this option wants a value
		 */
		public boolean wantsValue()
		{
			return this.wantsValue;
		}

		public final Object getValue(String arg) throws IllegalOptionValueException
		{
			if (this.wantsValue)
			{
				if (arg == null)
				{
					throw new IllegalOptionValueException(this, "");
				}
				return this.parseValue(arg);
			}
			else
			{
				return Boolean.TRUE;
			}
		}

		/**
		 * Override to extract and convert an option value passed on the
		 * command-line
		 */
		protected Object parseValue(String arg) throws IllegalOptionValueException
		{
			return null;
		}

		private String shortForm = null;
		private String longForm = null;
		private boolean wantsValue = false;

		public static class BooleanOption extends Option
		{
			public BooleanOption(char shortForm, String longForm)
			{
				super(shortForm, longForm, false);
			}
		}

		public static class IntegerOption extends Option
		{
			public IntegerOption(char shortForm, String longForm)
			{
				super(shortForm, longForm, true);
			}
			protected Object parseValue(String arg) throws IllegalOptionValueException
			{
				try
				{
					return new Integer(arg);
				}
				catch (NumberFormatException e)
				{
					throw new IllegalOptionValueException(this, arg);
				}
			}
		}

		public static class StringOption extends Option
		{
			public StringOption(char shortForm, String longForm)
			{
				super(shortForm, longForm, true);
			}
			protected Object parseValue(String arg)
			{
				return arg;
			}
		}
	}

	public final void addOption(Option opt)
	{
		this.options.put("-" + opt.shortForm(), opt);
		this.options.put("--" + opt.longForm(), opt);
	}

	public final Object getOptionValue(Option o)
	{
		return values.get(o.longForm());
	}

	public final String[] getRemainingArgs()
	{
		return this.remainingArgs;
	}

	public void parse(String[] argv)
		throws IllegalOptionValueException, UnknownOptionException
	{
		Vector otherArgs = new Vector();
		int position = 0;
		while (position < argv.length)
		{
			String curArg = argv[position];
			if (curArg.startsWith("-"))
			{
				if (curArg.equals("--"))
				{ // end of options
					position += 1;
					break;
				}
				String valueArg = null;
				if (curArg.startsWith("--"))
				{ // handle --arg=value
					int equalsPos = curArg.indexOf("=");
					if (equalsPos != -1)
					{
						valueArg = curArg.substring(equalsPos + 1);
						curArg = curArg.substring(0, equalsPos);
					}
				}
				Option opt = (Option) this.options.get(curArg);
				if (opt == null)
				{
					throw new UnknownOptionException(curArg);
				}
				Object value = null;
				if (opt.wantsValue())
				{
					if (valueArg == null)
					{
						position += 1;
						valueArg = null;
						if (position < argv.length)
						{
							valueArg = argv[position];
						}
					}
					value = opt.getValue(valueArg);
				}
				else
				{
					value = opt.getValue(null);
				}
				this.values.put(opt.longForm(), value);
				position += 1;
			}
			else
			{
				break;
			}
		}
		for (; position < argv.length; ++position)
		{
			otherArgs.addElement(argv[position]);
		}

		this.remainingArgs = new String[otherArgs.size()];
		int i = 0;
		for (Enumeration e = otherArgs.elements(); e.hasMoreElements(); ++i)
		{
			this.remainingArgs[i] = (String) e.nextElement();
		}
	}


}