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

package org.columba.core.config;

import java.io.File;

/**
 * @author frd
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class LogProperty {

	public static String createPropertyString(File configDir) {
		String dir = configDir.getPath();
		
		String pathDelimiter = File.separator;
		
		System.out.println("path-delimiter="+pathDelimiter);
		
		if( pathDelimiter.equals( "\\" ) ){
			pathDelimiter = "\\\\";	
			dir = replace( dir, "\\\\", "\\" );			
		}
		

		StringBuffer buf = new StringBuffer();

		buf.append(
			"log4j.logger.org.columba=ALL, destall, desterr, destinfo, destdebug, destcons\n");

		buf.append(
			"log4j.appender.destall=org.apache.log4j.RollingFileAppender\n");
		buf.append("log4j.appender.destall.File="+dir+pathDelimiter+"columba-all.log\n");
		buf.append("log4j.appender.destall.MaxFileSize=100KB\n");
		buf.append("log4j.appender.destall.MaxBackupIndex=5\n");
		buf.append(
			"log4j.appender.destall.layout=org.apache.log4j.PatternLayout\n");
		buf.append(
			"log4j.appender.destall.layout.ConversionPattern=%p %d{dd MMM yyyy HH:mm:s=s} %l - %m%n\n");

		buf.append("log4j.appender.desterr.Threshold=ERROR\n");
		buf.append(
			"log4j.appender.desterr=org.apache.log4j.RollingFileAppender\n");
		buf.append("log4j.appender.desterr.File="+dir+pathDelimiter+"columba-error.log\n");
		buf.append("log4j.appender.desterr.MaxFileSize=100KB\n");
		buf.append("log4j.appender.desterr.MaxBackupIndex=5\n");
		buf.append(
			"log4j.appender.desterr.layout=org.apache.log4j.PatternLayout\n");
		buf.append(
			"log4j.appender.desterr.layout.ConversionPattern=%p %d{dd MMM yyyy HH:mm:s=s} %l - %m%n\n");

		buf.append("log4j.appender.destinfo.Threshold=INFO\n");
		buf.append(
			"log4j.appender.destinfo=org.apache.log4j.RollingFileAppender\n");
		buf.append("log4j.appender.destinfo.File="+dir+pathDelimiter+"columba-info.log\n");
		buf.append("log4j.appender.destinfo.MaxFileSize=100KB\n");
		buf.append("log4j.appender.destinfo.MaxBackupIndex=5\n");
		buf.append(
			"log4j.appender.destinfo.layout=org.apache.log4j.PatternLayout\n");
		buf.append(
			"log4j.appender.destinfo.layout.ConversionPattern=%p %d{dd MMM yyyy HH:mm:s=s} %l - %m%n\n");

		buf.append("log4j.appender.destdebug.Threshold=DEBUG\n");
		buf.append(
			"log4j.appender.destdebug=org.apache.log4j.RollingFileAppender\n");
		buf.append("log4j.appender.destdebug.File="+dir+pathDelimiter+"columba-debug.log\n");
		buf.append("log4j.appender.destdebug.MaxFileSize=100KB\n");
		buf.append("log4j.appender.destdebug.MaxBackupIndex=5\n");
		buf.append(
			"log4j.appender.destdebug.layout=org.apache.log4j.PatternLayout\n");
		buf.append(
			"log4j.appender.destdebug.layout.ConversionPattern=%p %d{dd MMM yyyy HH:mm:s=s} %l - %m%n\n");

		buf.append("log4j.appender.destcons=org.apache.log4j.ConsoleAppender\n");
		buf.append(
			"log4j.appender.destcons.layout=org.apache.log4j.PatternLayout\n");
		buf.append(
			"log4j.appender.destcons.layout.ConversionPattern=%p %d{dd MMM yyyy HH:mm:s=s} %l - %m%n\n");

		return buf.toString();
	}

	protected static String replace(String str, String tok, String rep)
	{
		String retStr = "";
		for (int i = 0, j = 0;(j = str.indexOf(rep, i)) > -1; i = j + rep.length())
		{
			retStr += (str.substring(i, j) + tok);
		}
		return (str.indexOf(rep) == -1)
			? str
			: retStr + str.substring(str.lastIndexOf(rep) + rep.length(), str.length());
	}


}
