/*
 * Debug.java - Various debugging flags
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2003 Slava Pestov
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.gjt.sp.jedit;

/**
 * This class contains various debugging methods and flags.
 * @since jEdit 4.2pre1
 * @author Slava Pestov
 * @version $Id: Debug.java,v 1.1 2003/03/31 01:44:32 spestov Exp $
 */
public class Debug
{
	public static boolean CHUNK_CACHE_DEBUG = false;
	public static boolean CHUNK_PAINT_DEBUG = false;
	public static boolean EB_TIMER = false;
	public static boolean KEY_DELAY_TIMER = false;
	public static boolean OFFSET_DEBUG = false;
	public static boolean PAINT_TIMER = false;
	public static boolean TOKEN_MARKER_DEBUG = false;
}
