/*
 * Buffer.java - jEdit buffer
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 1998, 1999, 2000, 2001 Slava Pestov
 * Portions copyright (C) 1999, 2000 mike dillon
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

//{{{ Imports
import gnu.regexp.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.*;
import java.io.File;
import java.util.*;
import org.gjt.sp.jedit.browser.VFSBrowser;
import org.gjt.sp.jedit.buffer.*;
import org.gjt.sp.jedit.io.*;
import org.gjt.sp.jedit.msg.*;
import org.gjt.sp.jedit.search.RESearchMatcher;
import org.gjt.sp.jedit.syntax.*;
import org.gjt.sp.jedit.textarea.*;
import org.gjt.sp.util.*;
//}}}

/**
 * An in-memory copy of an open file.
 * Note that only very few methods in this class are thread safe; namely,
 * those that deal with obtaining buffer contents (<code>getText()</code>,
 * <code>getLineStartOffset()</code>, and so on).
 *
 * @author Slava Pestov
 * @version $Id: Buffer.java,v 1.45 2001/12/11 06:32:54 spestov Exp $
 */
public class Buffer implements EBComponent
{

	//{{{ Some constants
	/**
	 * Line separator property.
	 */
	public static final String LINESEP = "lineSeparator";

	/**
	 * Backed up property.
	 * @since jEdit 3.2pre2
	 */
	public static final String BACKED_UP = "Buffer__backedUp";

	/**
	 * Caret info properties.
	 * @since jEdit 3.2pre1
	 */
	public static final String CARET = "Buffer__caret";
	public static final String SELECTION = "Buffer__selection";

	/**
	 * This should be a physical line number, so that the scroll
	 * position is preserved correctly across reloads (which will
	 * affect virtual line numbers, due to fold being reset)
	 */
	public static final String SCROLL_VERT = "Buffer__scrollVert";
	public static final String SCROLL_HORIZ = "Buffer__scrollHoriz";

	/**
	 * Character encoding used when loading and saving.
	 * @since jEdit 3.2pre4
	 */
	public static final String ENCODING = "encoding";

	/**
	 * This property is set to 'true' if the file has a trailing newline.
	 * @since jEdit 4.0pre1
	 */
	public static final String TRAILING_EOL = "trailingEOL";
	//}}}

	//{{{ Input/output methods

	//{{{ showInsertFileDialog() method
	/**
	 * Displays the 'insert file' dialog box and inserts the selected file
	 * into the buffer.
	 * @param view The view
	 * @since jEdit 2.7pre2
	 */
	public void showInsertFileDialog(View view)
	{
		String[] files = GUIUtilities.showVFSFileDialog(view,null,
			VFSBrowser.OPEN_DIALOG,false);

		if(files != null)
			insertFile(view,files[0]);
	} //}}}

	//{{{ print() method
	/**
	 * Prints the buffer.
	 * @param view The view
	 * @since jEdit 2.7pre2
	 */
	public void print(View view)
	{
		PrintJob job = view.getToolkit().getPrintJob(view,name,null);
		if(job == null)
			return;

		view.showWaitCursor();

		int topMargin;
		int leftMargin;
		int bottomMargin;
		int rightMargin;
		int ppi = job.getPageResolution();

		try
		{
			topMargin = (int)(Float.valueOf(jEdit.getProperty(
				"print.margin.top")).floatValue() * ppi);
		}
		catch(NumberFormatException nf)
		{
			topMargin = ppi / 2;
		}
		try
		{
			leftMargin = (int)(Float.valueOf(jEdit.getProperty(
				"print.margin.left")).floatValue() * ppi);
		}
		catch(NumberFormatException nf)
		{
			leftMargin = ppi / 2;
		}
		try
		{
			bottomMargin = (int)(Float.valueOf(jEdit.getProperty(
				"print.margin.bottom")).floatValue() * ppi);
		}
		catch(NumberFormatException nf)
		{
			bottomMargin = topMargin;
		}
		try
		{
			rightMargin = (int)(Float.valueOf(jEdit.getProperty(
				"print.margin.right")).floatValue() * ppi);
		}
		catch(NumberFormatException nf)
		{
			rightMargin = leftMargin;
		}

		boolean printHeader = jEdit.getBooleanProperty("print.header");
		boolean printFooter = jEdit.getBooleanProperty("print.footer");
		boolean printLineNumbers = jEdit.getBooleanProperty("print.lineNumbers");
		boolean syntax = jEdit.getBooleanProperty("print.syntax");

		String header = path;
		String footer = new Date().toString();

		TabExpander expander = null;

		Graphics gfx = null;

		Font font = jEdit.getFontProperty("print.font");

		SyntaxStyle[] styles = GUIUtilities.loadStyles(
			jEdit.getProperty("print.font"),
			jEdit.getIntegerProperty("print.fontsize",10));

		boolean style = jEdit.getBooleanProperty("print.style");
		boolean color = jEdit.getBooleanProperty("print.color");

		FontMetrics fm = null;
		Dimension pageDimension = job.getPageDimension();
		int pageWidth = pageDimension.width;
		int pageHeight = pageDimension.height;
		int y = 0;
		int tabSize = 0;
		int lineHeight = 0;
		int page = 0;

		int lineNumberDigits = (int)Math.ceil(Math.log(
			getLineCount()) / Math.log(10));

		int lineNumberWidth = 0;

		TextRenderer renderer = new TextRenderer();

		renderer.configure(false,false);

		for(int i = 0; i < getLineCount(); i++)
		{
			if(gfx == null)
			{
				page++;

				gfx = job.getGraphics();
				renderer.setupGraphics(gfx);

				gfx.setFont(font);
				fm = gfx.getFontMetrics();

				if(printLineNumbers)
				{
					lineNumberWidth = fm.charWidth('0')
						* lineNumberDigits;
				}
				else
					lineNumberWidth = 0;

				lineHeight = fm.getHeight();
				tabSize = getTabSize() * fm.charWidth(' ');
				expander = new PrintTabExpander(leftMargin
					+ lineNumberWidth,tabSize);

				y = topMargin + lineHeight - fm.getDescent()
					- fm.getLeading();

				if(printHeader)
				{
					gfx.setColor(Color.lightGray);
					gfx.fillRect(leftMargin,topMargin,pageWidth
						- leftMargin - rightMargin,lineHeight);
					gfx.setColor(Color.black);
					gfx.drawString(header,leftMargin,y);
					y += lineHeight;
				}
			}

			y += lineHeight;

			gfx.setColor(Color.black);
			gfx.setFont(font);

			int x = leftMargin;
			if(printLineNumbers)
			{
				String lineNumber = String.valueOf(i + 1);
				gfx.drawString(lineNumber,(leftMargin + lineNumberWidth)
					- fm.stringWidth(lineNumber),y);
				x += lineNumberWidth + fm.charWidth('0');
			}

			paintSyntaxLine(i,gfx,x,y,expander,style,color,
				font,Color.black,Color.white,styles,
				renderer);

			int bottomOfPage = pageHeight - bottomMargin - lineHeight;
			if(printFooter)
				bottomOfPage -= lineHeight * 2;

			if(y >= bottomOfPage || i == getLineCount() - 1)
			{
				if(printFooter)
				{
					y = pageHeight - bottomMargin;

					gfx.setColor(Color.lightGray);
					gfx.setFont(font);
					gfx.fillRect(leftMargin,y - lineHeight,pageWidth
						- leftMargin - rightMargin,lineHeight);
					gfx.setColor(Color.black);
					y -= (lineHeight - fm.getAscent());
					gfx.drawString(footer,leftMargin,y);

					Integer[] args = { new Integer(page) };
					String pageStr = jEdit.getProperty("print.page",args);
					int width = fm.stringWidth(pageStr);
					gfx.drawString(pageStr,pageWidth - rightMargin
						- width,y);
				}

				gfx.dispose();
				gfx = null;
			}
		}

		job.end();

		view.hideWaitCursor();
	} //}}}

	//{{{ reload() method
	/**
	 * Reloads the buffer from disk, asking for confirmation if the buffer
	 * is dirty.
	 * @param view The view
	 * @since jEdit 2.7pre2
	 */
	public void reload(View view)
	{
		if(getFlag(DIRTY))
		{
			String[] args = { name };
			int result = GUIUtilities.confirm(view,"changedreload",
				args,JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE);
			if(result != JOptionPane.YES_OPTION)
				return;
		}

		view.getEditPane().saveCaretInfo();
		load(view,true);
	} //}}}

	//{{{ load() method
	/**
	 * Loads the buffer from disk, even if it is loaded already.
	 * @param view The view
	 * @param reload If true, user will not be asked to recover autosave
	 * file, if any
	 *
	 * @since 2.5pre1
	 */
	public boolean load(final View view, final boolean reload)
	{
		if(isPerformingIO())
		{
			GUIUtilities.error(view,"buffer-multiple-io",null);
			return false;
		}

		setFlag(LOADING,true);

		// view text areas temporarily blank out while a buffer is
		// being loaded, to indicate to the user that there is no
		// data available yet.
		if(!getFlag(TEMPORARY))
			EditBus.send(new BufferUpdate(this,view,BufferUpdate.LOAD_STARTED));

		final boolean loadAutosave;

		if(reload || !getFlag(NEW_FILE))
		{
			if(file != null)
				modTime = file.lastModified();

			// Only on initial load
			if(!reload && autosaveFile != null && autosaveFile.exists())
				loadAutosave = recoverAutosave(view);
			else
			{
				if(autosaveFile != null)
					autosaveFile.delete();
				loadAutosave = false;
			}

			if(!loadAutosave)
			{
				// this returns false if initial sanity
				// checks (if the file is a directory, etc)
				// fail
				if(!vfs.load(view,this,path))
				{
					setFlag(LOADING,false);
					return false;
				}
			}
		}
		else
			loadAutosave = false;

		//{{{ Do some stuff once loading is finished
		Runnable runnable = new Runnable()
		{
			public void run()
			{
				String newPath = getStringProperty(
					BufferIORequest.NEW_PATH);
				Segment seg = (Segment)getProperty(
					BufferIORequest.LOAD_DATA);
				IntegerArray endOffsets = (IntegerArray)
					getProperty(BufferIORequest.END_OFFSETS);

				// For `reload' command
				remove(0,getLength());

				if(seg != null && endOffsets != null)
				{
					// This is faster than Buffer.insert()
					try
					{
						writeLock();

						contentMgr.insert(0,seg.toString());

						int lineCount = endOffsets.getSize();
						if(lineCount != 0)
						{
							parseBufferLocalProperties(
								contentMgr.getText(0,
									endOffsets.get(Math.min(
									lineCount - 1,
									10))));
						}

						setModeForFirstLine(contentMgr.getText(0,
							endOffsets.get(0)));

						contentInserted(0,seg.count,
							endOffsets);
					}
					finally
					{
						writeUnlock();
					}
				}
				else
				{
					// have to set mode according to path name only
					setMode();
				}

				unsetProperty(BufferIORequest.LOAD_DATA);
				unsetProperty(BufferIORequest.END_OFFSETS);
				unsetProperty(BufferIORequest.NEW_PATH);

				undoMgr.clear();
				undoMgr.setLimit(jEdit.getIntegerProperty(
					"buffer.undoCount",100));

				setFlag(LOADING,false);

				// if reloading a file, clear dirty flag
				if(reload)
					setDirty(false);

				if(!loadAutosave && newPath != null && !path.equals(newPath))
					setPath(newPath);

				// if loadAutosave is false, we loaded an
				// autosave file, so we set 'dirty' to true

				// note that we don't use setDirty(),
				// because a) that would send an unnecessary
				// message, b) it would also set the
				// AUTOSAVE_DIRTY flag, which will make
				// the autosave thread write out a
				// redundant autosave file
				if(loadAutosave)
					setFlag(DIRTY,true);

				if(jEdit.getBooleanProperty("parseFully"))
				{
					for(int i = 0; i < offsetMgr.getLineCount(); i++)
						markTokens(i);
				}

				// send some EditBus messages
				if(!getFlag(TEMPORARY))
				{
					EditBus.send(new BufferUpdate(Buffer.this,
						view,BufferUpdate.LOADED));
					EditBus.send(new BufferUpdate(Buffer.this,
						view,BufferUpdate.MARKERS_CHANGED));
				}
			}
		}; //}}}

		if(getFlag(TEMPORARY))
			runnable.run();
		else
			VFSManager.runInAWTThread(runnable);

		return true;
	} //}}}

	//{{{ insertFile() method
	/**
	 * Loads a file from disk, and inserts it into this buffer.
	 * @param view The view
	 *
	 * @since 4.0pre1
	 */
	public boolean insertFile(final View view, String path)
	{
		if(isPerformingIO())
		{
			GUIUtilities.error(view,"buffer-multiple-io",null);
			return false;
		}

		path = MiscUtilities.constructPath(this.path,path);

		Buffer buffer = jEdit.getBuffer(path);
		if(buffer != null)
		{
			view.getTextArea().setSelectedText(
				buffer.getText(0,buffer.getLength()));
			return true;
		}

		VFS vfs = VFSManager.getVFSForPath(path);

		setFlag(IO,true);

		// this returns false if initial sanity
		// checks (if the file is a directory, etc)
		// fail
		if(!vfs.insert(view,this,path))
		{
			setFlag(IO,false);
			return false;
		}

		// Do some stuff once loading is finished
		VFSManager.runInAWTThread(new Runnable()
		{
			public void run()
			{
				setFlag(IO,false);

				StringBuffer sbuf = (StringBuffer)getProperty(
					BufferIORequest.LOAD_DATA);
				if(sbuf != null)
				{
					unsetProperty(BufferIORequest.LOAD_DATA);

					view.getTextArea().setSelectedText(sbuf.toString());
				}
			}
		});

		return true;
	} //}}}

	//{{{ autosave() method
	/**
	 * Autosaves this buffer.
	 */
	public void autosave()
	{
		if(autosaveFile == null || !getFlag(AUTOSAVE_DIRTY)
			|| !getFlag(DIRTY)
			|| getFlag(LOADING)
			|| getFlag(IO))
			return;

		setFlag(AUTOSAVE_DIRTY,false);

		VFSManager.runInWorkThread(new BufferIORequest(
			BufferIORequest.AUTOSAVE,null,this,null,
			VFSManager.getFileVFS(),autosaveFile.getPath()));
	} //}}}

	//{{{ saveAs() method
	/**
	 * Prompts the user for a file to save this buffer to.
	 * @param view The view
	 * @param rename True if the buffer's path should be changed, false
	 * if only a copy should be saved to the specified filename
	 * @since jEdit 2.6pre5
	 */
	public boolean saveAs(View view, boolean rename)
	{
		String[] files = GUIUtilities.showVFSFileDialog(view,path,
			VFSBrowser.SAVE_DIALOG,false);

		// files[] should have length 1, since the dialog type is
		// SAVE_DIALOG
		if(files == null)
			return false;

		return save(view,files[0],rename);
	} //}}}

	//{{{ save() method
	/**
	 * Saves this buffer to the specified path name, or the current path
	 * name if it's null.
	 * @param view The view
	 * @param path The path name to save the buffer to, or null to use
	 * the existing path
	 */
	public boolean save(View view, String path)
	{
		return save(view,path,true);
	} //}}}

	//{{{ save() method
	/**
	 * Saves this buffer to the specified path name, or the current path
	 * name if it's null.
	 * @param view The view
	 * @param path The path name to save the buffer to, or null to use
	 * the existing path
	 * @param rename True if the buffer's path should be changed, false
	 * if only a copy should be saved to the specified filename
	 * @since jEdit 2.6pre5
	 */
	public boolean save(final View view, String path, final boolean rename)
	{
		if(isPerformingIO())
		{
			GUIUtilities.error(view,"buffer-multiple-io",null);
			return false;
		}

		if(path == null && getFlag(NEW_FILE))
			return saveAs(view,rename);

		if(path == null && file != null)
		{
			long newModTime = file.lastModified();

			if(newModTime != modTime)
			{
				Object[] args = { this.path };
				int result = GUIUtilities.confirm(view,
					"filechanged-save",args,
					JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE);
				if(result != JOptionPane.YES_OPTION)
					return false;
			}
		}

		setFlag(IO,true);
		EditBus.send(new BufferUpdate(this,view,BufferUpdate.SAVING));

		if(path == null)
			path = this.path;

		// can't call setPath() here because we don't want a failed
		// 'save as' to change the buffer's path, so obtain the VFS
		// instance 'manually'
		VFS vfs = VFSManager.getVFSForPath(path);

		if(!vfs.save(view,this,path))
		{
			setFlag(IO,false);
			return false;
		}

		final String oldPath = this.path;
		if(rename)
			setPath(path);

		// Once save is complete, do a few other things
		VFSManager.runInAWTThread(new Runnable()
		{
			public void run()
			{
				// Saving a NEW_FILE will create a file on
				// disk, thus file system browsers must reload
				if(getFlag(NEW_FILE) || !getPath().equals(oldPath))
					VFSManager.sendVFSUpdate(getVFS(),getPath(),true);

				setFlag(IO,false);

				if(rename)
				{
					// we do a write lock so that the
					// autosave, which grabs a read lock,
					// is not executed between the
					// deletion of the autosave file
					// and clearing of the dirty flag
					try
					{
						writeLock();

						if(autosaveFile != null)
							autosaveFile.delete();

						setFlag(AUTOSAVE_DIRTY,false);
						setFlag(READ_ONLY,false);
						setFlag(NEW_FILE,false);
						setFlag(UNTITLED,false);
						setFlag(DIRTY,false);

						// this ensures that undo can clear
						// the dirty flag properly when all
						// edits up to a save are undone
						undoMgr.bufferSaved();
					}
					finally
					{
						writeUnlock();
					}

					parseBufferLocalProperties();

					if(!getPath().equals(oldPath))
					{
						jEdit.updatePosition(Buffer.this);
						setMode();
					}
					else
						propertiesChanged();

					if(file != null)
						modTime = file.lastModified();

					EditBus.send(new BufferUpdate(Buffer.this,
						view,BufferUpdate.DIRTY_CHANGED));
				}
			}
		});

		return true;
	} //}}}

	//{{{ checkModTime() method
	/**
	 * Check if the buffer has changed on disk.
	 */
	public void checkModTime(View view)
	{
		// don't do these checks while a save is in progress,
		// because for a moment newModTime will be greater than
		// oldModTime, due to the multithreading
		if(file == null || getFlag(NEW_FILE) || getFlag(IO))
			return;

		boolean newReadOnly = (file.exists() && !file.canWrite());
		if(newReadOnly != getFlag(READ_ONLY))
		{
			setFlag(READ_ONLY,newReadOnly);
			EditBus.send(new BufferUpdate(this,
				view,BufferUpdate.DIRTY_CHANGED));
		}

		if(!jEdit.getBooleanProperty("view.checkModStatus"))
			return;

		long oldModTime = modTime;
		long newModTime = file.lastModified();

		if(newModTime != oldModTime)
		{
			modTime = newModTime;

			if(!file.exists())
			{
				setFlag(NEW_FILE,true);
				EditBus.send(new BufferUpdate(this,
					view,BufferUpdate.DIRTY_CHANGED));
				Object[] args = { path };
				GUIUtilities.message(view,"filedeleted",args);
				return;
			}

			String prop = (isDirty() ? "filechanged-dirty"
				: "filechanged-focus");

			Object[] args = { path };
			int result = GUIUtilities.confirm(view,
				prop,args,JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE);
			if(result == JOptionPane.YES_OPTION)
			{
				view.getEditPane().saveCaretInfo();
				load(view,true);
			}
		}
	} //}}}

	//}}}

	//{{{ Getters/setter methods for various things

	//{{{ getLastModified() method
	/**
	 * Returns the last time jEdit modified the file on disk.
	 */
	public long getLastModified()
	{
		return modTime;
	} //}}}

	//{{{ setLastModified() method
	/**
	 * Sets the last time jEdit modified the file on disk.
	 * @param modTime The new modification time
	 */
	public void setLastModified(long modTime)
	{
		this.modTime = modTime;
	} //}}}

	//{{{ getVFS() method
	/**
	 * Returns the virtual filesystem responsible for loading and
	 * saving this buffer.
	 */
	public VFS getVFS()
	{
		return vfs;
	} //}}}

	//{{{ getFile() method
	/**
	 * Returns the file for this buffer. This may be null if the buffer
	 * is non-local.
	 */
	public final File getFile()
	{
		return file;
	} //}}}

	//{{{ getAutosaveFile() method
	/**
	 * Returns the autosave file for this buffer. This may be null if
	 * the file is non-local.
	 */
	public final File getAutosaveFile()
	{
		return autosaveFile;
	} //}}}

	//{{{ getName() method
	/**
	 * Returns the name of this buffer.
	 */
	public final String getName()
	{
		return name;
	} //}}}

	//{{{ getPath() method
	/**
	 * Returns the path name of this buffer.
	 */
	public final String getPath()
	{
		return path;
	} //}}}

	//{{{ isClosed() method
	/**
	 * Returns true if this buffer has been closed with
	 * <code>jEdit.closeBuffer()</code>.
	 */
	public final boolean isClosed()
	{
		return getFlag(CLOSED);
	} //}}}

	//{{{ isLoaded() method
	/**
	 * Returns true if the buffer is loaded.
	 */
	public final boolean isLoaded()
	{
		return !getFlag(LOADING);
	} //}}}

	//{{{ isPerformingIO() method
	/**
	 * Returns true if the buffer is currently performing I/O.
	 * @since jEdit 2.7pre1
	 */
	public final boolean isPerformingIO()
	{
		return getFlag(LOADING) || getFlag(IO);
	} //}}}

	//{{{ isNewFile() method
	/**
	 * Returns true if this file doesn't exist on disk.
	 */
	public final boolean isNewFile()
	{
		return getFlag(NEW_FILE);
	} //}}}

	//{{{ setNewFile() method
	/**
	 * Sets the new file flag.
	 * @param newFile The new file flag
	 */
	public final void setNewFile(boolean newFile)
	{
		setFlag(NEW_FILE,newFile);
		if(!newFile)
			setFlag(UNTITLED,false);
	} //}}}

	//{{{ isUntitled() method
	/**
	 * Returns true if this file is 'untitled'.
	 */
	public final boolean isUntitled()
	{
		return getFlag(UNTITLED);
	} //}}}

	//{{{ isDirty() method
	/**
	 * Returns true if this file has changed since last save, false
	 * otherwise.
	 */
	public final boolean isDirty()
	{
		return getFlag(DIRTY);
	} //}}}

	//{{{ isReadOnly() method
	/**
	 * Returns true if this file is read only, false otherwise.
	 */
	public final boolean isReadOnly()
	{
		return getFlag(READ_ONLY);
	} //}}}

	//{{{ isEditable() method
	/**
	 * Returns true if this file is editable, false otherwise.
	 * @since jEdit 2.7pre1
	 */
	public final boolean isEditable()
	{
		return !(getFlag(READ_ONLY) || getFlag(IO) || getFlag(LOADING));
	} //}}}

	//{{{ isReadOnly() method
	/**
	 * Sets the read only flag.
	 * @param readOnly The read only flag
	 */
	public final void setReadOnly(boolean readOnly)
	{
		setFlag(READ_ONLY,readOnly);
	} //}}}

	//{{{ setDirty() method
	/**
	 * Sets the `dirty' (changed since last save) flag of this buffer.
	 */
	public void setDirty(boolean d)
	{
		boolean old_d = getFlag(DIRTY);

		if(d)
		{
			if(getFlag(LOADING) || getFlag(READ_ONLY))
				return;
			if(getFlag(DIRTY) && getFlag(AUTOSAVE_DIRTY))
				return;
			setFlag(DIRTY,true);
			setFlag(AUTOSAVE_DIRTY,true);
		}
		else
		{
			setFlag(DIRTY,false);
			setFlag(AUTOSAVE_DIRTY,false);

			// this ensures that undo can clear the dirty flag properly
			// when all edits up to a save are undone
			undoMgr.bufferSaved();
		}

		if(d != old_d)
		{
			EditBus.send(new BufferUpdate(this,null,
				BufferUpdate.DIRTY_CHANGED));
		}
	} //}}}

	//{{{ isTemporary() method
	/**
	 * Returns if this is a temporary buffer.
	 * @see jEdit#openTemporary(View,String,String,boolean,boolean)
	 * @see jEdit#commitTemporary(Buffer)
	 * @since jEdit 2.2pre7
	 */
	public boolean isTemporary()
	{
		return getFlag(TEMPORARY);
	} //}}}

	//{{{ getIcon() method
	/**
	 * Returns this buffer's icon.
	 * @since jEdit 2.6pre6
	 */
	public Icon getIcon()
	{
		if(getFlag(DIRTY))
			return GUIUtilities.DIRTY_BUFFER_ICON;
		else if(getFlag(READ_ONLY))
			return GUIUtilities.READ_ONLY_BUFFER_ICON;
		else if(getFlag(NEW_FILE))
			return GUIUtilities.NEW_BUFFER_ICON;
		else
			return GUIUtilities.NORMAL_BUFFER_ICON;
	} //}}}

	//}}}

	//{{{ Thread safety

	//{{{ readLock() method
	/**
	 * The buffer is guaranteed not to change between calls to
	 * <code>readLock()</code> and <code>readUnlock()</code>.
	 */
	public final void readLock()
	{
		lock.readLock();
	} //}}}

	//{{{ readUnlock() method
	/**
	 * The buffer is guaranteed not to change between calls to
	 * <code>readLock()</code> and <code>readUnlock()</code>.
	 */
	public final void readUnlock()
	{
		lock.readUnlock();
	} //}}}

	//{{{ writeLock() method
	/**
	 * The buffer cintents are guaranteed not to be read or written
	 * by other threads between calls to <code>writeLock()</code>
	 * and <code>writeUnlock()</code>.
	 */
	public final void writeLock()
	{
		lock.writeLock();
	} //}}}

	//{{{ writeUnlock() method
	/**
	 * The buffer cintents are guaranteed not to be read or written
	 * by other threads between calls to <code>writeLock()</code>
	 * and <code>writeUnlock()</code>.
	 */
	public final void writeUnlock()
	{
		lock.writeUnlock();
	} //}}}

	//}}}

	//{{{ Text reading methods

	//{{{ getLength() method
	/**
	 * Returns the number of characters in the buffer.
	 */
	public int getLength()
	{
		// no need to lock since this just returns a value and that's it
		return contentMgr.getLength();
	} //}}}

	//{{{ getLineCount() method
	/**
	 * Returns the number of physical lines in the buffer.
	 * This method is thread-safe.
	 * @since jEdit 3.1pre1
	 */
	public int getLineCount()
	{
		// no need to lock since this just returns a value and that's it
		return offsetMgr.getLineCount();
	} //}}}

	//{{{ Debugging
	public void testOffsetManager()
	{
		java.util.Random random = new java.util.Random();
		for(int i = 0; i < 10000; i++)
		{
			int next = Math.abs(random.nextInt()) % getLength();
			int line = getLineOfOffset(next);
			int start = getLineStartOffset(line);
			int end = getLineEndOffset(line);
			if(next < start || next >= end)
			{
				System.err.println(next + ":" + line);
				break;
			}
		}

		for(int i = 0; i < getLineCount(); i++)
		{
			int start = getLineStartOffset(i);
			int end = getLineEndOffset(i);
			if(start >= end)
				System.err.println(i + ":" + start + ":" + end);
		}
	}

	public void testPositions(int positions, int inserts)
	{
		java.util.Vector v = new Vector();
		java.util.Random random = new java.util.Random();
		for(int i = 0; i < positions; i++)
		{
			v.addElement(createPosition(Math.abs(random.nextInt()) % getLength()));
		}

		long start = System.currentTimeMillis();

		for(int i = 0; i < inserts; i++)
		{
			int pos = Math.abs(random.nextInt()) % getLength();
			insert(pos,"a");
		}

		System.err.println(System.currentTimeMillis() - start);
	} //}}}

	//{{{ getLineOfOffset() method
	/**
	 * Returns the line containing the specified offset.
	 * This method is thread-safe.
	 * @param offset The offset
	 * @since jEdit 4.0pre1
	 */
	public final int getLineOfOffset(int offset)
	{
		try
		{
			readLock();

			if(offset < 0 || offset > getLength())
				throw new ArrayIndexOutOfBoundsException(offset);

			return offsetMgr.getLineOfOffset(offset);
		}
		finally
		{
			readUnlock();
		}
	} //}}}

	//{{{ getLineStartOffset() method
	/**
	 * Returns the start offset of the specified line.
	 * This method is thread-safe.
	 * @param line The line
	 * @return The start offset of the specified line
	 * @since jEdit 4.0pre1
	 */
	public int getLineStartOffset(int line)
	{
		try
		{
			readLock();

			if(line < 0 || line >= offsetMgr.getLineCount())
				throw new ArrayIndexOutOfBoundsException(line);
			else if(line == 0)
				return 0;

			return offsetMgr.getLineEndOffset(line - 1);
		}
		finally
		{
			readUnlock();
		}
	} //}}}

	//{{{ getLineEndOffset() method
	/**
	 * Returns the end offset of the specified line.
	 * This method is thread-safe.
	 * @param line The line
	 * @return The end offset of the specified line
	 * invalid.
	 * @since jEdit 4.0pre1
	 */
	public int getLineEndOffset(int line)
	{
		try
		{
			readLock();

			if(line < 0 || line >= offsetMgr.getLineCount())
				throw new ArrayIndexOutOfBoundsException(line);

			return offsetMgr.getLineEndOffset(line);
		}
		finally
		{
			readUnlock();
		}
	} //}}}

	//{{{ getLineLength() method
	/**
	 * Returns the length of the specified line.
	 * This method is thread-safe.
	 * @param line The line
	 * @since jEdit 4.0pre1
	 */
	public int getLineLength(int line)
	{
		try
		{
			readLock();

			return getLineEndOffset(line)
				- getLineStartOffset(line) - 1;
		}
		finally
		{
			readUnlock();
		}
	} //}}}

	//{{{ getLineText() method
	/**
	 * Returns the text on the specified line.
	 * This method is thread-safe.
	 * @param lineIndex The line
	 * @return The text, or null if the line is invalid
	 * @since jEdit 4.0pre1
	 */
	public String getLineText(int lineIndex)
	{
		try
		{
			readLock();

			return getText(getLineStartOffset(lineIndex),
				getLineLength(lineIndex));
		}
		finally
		{
			readUnlock();
		}
	} //}}}

	//{{{ getLineText() method
	/**
	 * Copies the text on the specified line into a segment.
	 * This method is thread-safe.
	 * @param lineIndex The line
	 * @since jEdit 4.0pre1
	 */
	public void getLineText(int lineIndex, Segment segment)
	{
		try
		{
			readLock();

			getText(getLineStartOffset(lineIndex),
				getLineLength(lineIndex),segment);
		}
		finally
		{
			readUnlock();
		}
	} //}}}

	//{{{ getText() method
	/**
	 * Returns the specified text range.
	 * @param start The start offset
	 * @param length The number of characters to get
	 */
	public String getText(int start, int length)
	{
		try
		{
			readLock();

			if(start < 0 || length < 0
				|| start + length > contentMgr.getLength())
				throw new ArrayIndexOutOfBoundsException(start + ":" + length);

			return contentMgr.getText(start,length);
		}
		finally
		{
			readUnlock();
		}
	} //}}}

	//{{{ getText() method
	/**
	 * Returns the specified text range.
	 * @param start The start offset
	 * @param length The number of characters to get
	 * @param seg The segment to copy the text to
	 */
	public void getText(int start, int length, Segment seg)
	{
		try
		{
			readLock();

			if(start < 0 || length < 0
				|| start + length > contentMgr.getLength())
				throw new ArrayIndexOutOfBoundsException(start + ":" + length);

			contentMgr.getText(start,length,seg);
		}
		finally
		{
			readUnlock();
		}
	} //}}}

	//}}}

	//{{{ Text writing methods

	//{{{ insert() method
	/**
	 * Inserts a string into the buffer.
	 * @param offset The offset
	 * @param str The string
	 * @since jEdit 4.0pre1
	 */
	public void insert(int offset, String str)
	{
		if(str == null || str.length() == 0)
			return;

		if(isReadOnly())
			throw new RuntimeException("buffer read-only");

		try
		{
			writeLock();

			if(offset < 0 || offset > contentMgr.getLength())
				throw new ArrayIndexOutOfBoundsException(offset);

			contentMgr.insert(offset,str);

			integerArray.clear();

			for(int i = 0; i < str.length(); i++)
			{
				if(str.charAt(i) == '\n')
					integerArray.add(i);
			}

			if(!getFlag(UNDO_IN_PROGRESS))
			{
				undoMgr.contentInserted(offset,str.length(),str,
					!getFlag(DIRTY));
			}

			contentInserted(offset,str.length(),integerArray);
		}
		finally
		{
			writeUnlock();
		}
	} //}}}

	//{{{ insert() method
	/**
	 * Inserts a string into the buffer.
	 * @param offset The offset
	 * @param seg The segment
	 * @since jEdit 4.0pre1
	 */
	public void insert(int offset, Segment seg)
	{
		if(seg.count == 0)
			return;

		if(isReadOnly())
			throw new RuntimeException("buffer read-only");

		try
		{
			writeLock();

			if(offset < 0 || offset > contentMgr.getLength())
				throw new ArrayIndexOutOfBoundsException(offset);

			contentMgr.insert(offset,seg);

			integerArray.clear();

			for(int i = 0; i < seg.count; i++)
			{
				if(seg.array[seg.offset + i] == '\n')
					integerArray.add(i);
			}

			if(!getFlag(UNDO_IN_PROGRESS))
			{
				undoMgr.contentInserted(offset,seg.count,
					seg.toString(),!getFlag(DIRTY));
			}

			contentInserted(offset,seg.count,integerArray);
		}
		finally
		{
			writeUnlock();
		}
	} //}}}

	//{{{ remove() method
	/**
	 * Removes the specified rang efrom the buffer.
	 * @param offset The start offset
	 * @param length The number of characters to remove
	 */
	public void remove(int offset, int length)
	{
		if(length == 0)
			return;

		if(isReadOnly())
			throw new RuntimeException("buffer read-only");

		try
		{
			writeLock();

			if(offset < 0 || length < 0
				|| offset + length > contentMgr.getLength())
				throw new ArrayIndexOutOfBoundsException(offset + ":" + length);

			int startLine = offsetMgr.getLineOfOffset(offset);

			contentMgr.getText(offset,length,seg);
			int numLines = 0;
			for(int i = 0; i < seg.count; i++)
			{
				if(seg.array[seg.offset + i] == '\n')
					numLines++;
			}

			if(!getFlag(UNDO_IN_PROGRESS))
			{
				undoMgr.contentRemoved(offset,length,
					seg.toString(),!getFlag(DIRTY));
			}

			contentMgr.remove(offset,length);

			if(lastTokenizedLine >= startLine)
				lastTokenizedLine = -1;

			offsetMgr.contentRemoved(startLine,offset,numLines,length);

			if(numLines > 0)
			{
				for(int i = 0; i < inUseFVMs.length; i++)
				{
					if(inUseFVMs[i] != null)
						inUseFVMs[i]._invalidate(startLine);
				}
			}

			fireContentRemoved(startLine,offset,numLines,length);

			setDirty(true);
		}
		finally
		{
			writeUnlock();
		}
	} //}}}

	//{{{ removeTrailingWhiteSpace() method
	/**
	 * Removes trailing whitespace from all lines in the specified list.
	 * @param list The line numbers
	 * @since jEdit 3.2pre1
	 */
	public void removeTrailingWhiteSpace(int[] lines)
	{
		try
		{
			beginCompoundEdit();

			for(int i = 0; i < lines.length; i++)
			{
				int pos, lineStart, lineEnd, tail;

				getLineText(lines[i],seg);

				// blank line
				if (seg.count == 0) continue;

				lineStart = seg.offset;
				lineEnd = seg.offset + seg.count - 1;

				for (pos = lineEnd; pos >= lineStart; pos--)
				{
					if (!Character.isWhitespace(seg.array[pos]))
						break;
				}

				tail = lineEnd - pos;

				// no whitespace
				if (tail == 0) continue;

				remove(getLineEndOffset(lines[i]) - 1 - tail,tail);
			}
		}
		finally
		{
			endCompoundEdit();
		}
	} //}}}

	//{{{ shiftIndentLeft() method
	/**
	 * Shifts the indent of each line in the specified list to the left.
	 * @param lines The line numbers
	 * @since jEdit 3.2pre1
	 */
	public void shiftIndentLeft(int[] lines)
	{
		int tabSize = getTabSize();
		int indentSize = getIndentSize();
		boolean noTabs = getBooleanProperty("noTabs");

		try
		{
			beginCompoundEdit();

			for(int i = 0; i < lines.length; i++)
			{
				int lineStart = getLineStartOffset(lines[i]);
				String line = getLineText(lines[i]);
				int whiteSpace = MiscUtilities
					.getLeadingWhiteSpace(line);
				if(whiteSpace == 0)
					continue;
				int whiteSpaceWidth = Math.max(0,MiscUtilities
					.getLeadingWhiteSpaceWidth(line,tabSize)
					- indentSize);

				remove(lineStart,whiteSpace);
				insert(lineStart,MiscUtilities
					.createWhiteSpace(whiteSpaceWidth,
					(noTabs ? 0 : tabSize)));
			}

		}
		finally
		{
			endCompoundEdit();
		}
	} //}}}

	//{{{ shiftIndentRight() method
	/**
	 * Shifts the indent of each line in the specified list to the right.
	 * @param lines The line numbers
	 * @since jEdit 3.2pre1
	 */
	public void shiftIndentRight(int[] lines)
	{
		try
		{
			beginCompoundEdit();

			int tabSize = getTabSize();
			int indentSize = getIndentSize();
			boolean noTabs = getBooleanProperty("noTabs");
			for(int i = 0; i < lines.length; i++)
			{
				int lineStart = getLineStartOffset(lines[i]);
				String line = getLineText(lines[i]);
				int whiteSpace = MiscUtilities
					.getLeadingWhiteSpace(line);

				// silly usability hack
				//if(lines.length != 1 && whiteSpace == 0)
				//	continue;

				int whiteSpaceWidth = MiscUtilities
					.getLeadingWhiteSpaceWidth(
					line,tabSize) + indentSize;
				remove(lineStart,whiteSpace);
				insert(lineStart,MiscUtilities
					.createWhiteSpace(whiteSpaceWidth,
					(noTabs ? 0 : tabSize)));
			}
		}
		finally
		{
			endCompoundEdit();
		}
	} //}}}

	//}}}

	//{{{ Undo

	//{{{ undo() method
	/**
	 * Undoes the most recent edit.
	 *
	 * @since jEdit 4.0pre1
	 */
	public void undo(JEditTextArea textArea)
	{
		if(undoMgr == null)
			return;

		if(!isEditable())
		{
			textArea.getToolkit().beep();
			return;
		}

		try
		{
			writeLock();

			setFlag(UNDO_IN_PROGRESS,true);
			if(!undoMgr.undo(textArea))
				textArea.getToolkit().beep();
		}
		finally
		{
			setFlag(UNDO_IN_PROGRESS,false);

			writeUnlock();
		}
	} //}}}

	//{{{ redo() method
	/**
	 * Redoes the most recently undone edit. Returns true if the redo was
	 * successful.
	 *
	 * @since jEdit 2.7pre2
	 */
	public void redo(JEditTextArea textArea)
	{
		if(undoMgr == null)
			return;

		if(!isEditable())
		{
			Toolkit.getDefaultToolkit().beep();
			return;
		}

		try
		{
			writeLock();

			setFlag(UNDO_IN_PROGRESS,true);
			if(!undoMgr.redo(textArea))
				textArea.getToolkit().beep();
		}
		finally
		{
			setFlag(UNDO_IN_PROGRESS,false);

			writeUnlock();
		}
	} //}}}

	//{{{ beginCompoundEdit() method
	/**
	 * Starts a compound edit. All edits from now on until
	 * <code>endCompoundEdit()</code> are called will be merged
	 * into one. This can be used to make a complex operation
	 * undoable in one step. Nested calls to
	 * <code>beginCompoundEdit()</code> behave as expected,
	 * requiring the same number of <code>endCompoundEdit()</code>
	 * calls to end the edit.
	 * @see #endCompoundEdit()
	 * @see #undo()
	 */
	public void beginCompoundEdit()
	{
		if(getFlag(TEMPORARY))
			return;

		try
		{
			writeLock();

			undoMgr.beginCompoundEdit();
		}
		finally
		{
			writeUnlock();
		}
	} //}}}

	//{{{ endCompoundEdit() method
	/**
	 * Ends a compound edit. All edits performed since
	 * <code>beginCompoundEdit()</code> was called can now
	 * be undone in one step by calling <code>undo()</code>.
	 * @see #beginCompoundEdit()
	 * @see #undo()
	 */
	public void endCompoundEdit()
	{
		if(getFlag(TEMPORARY))
			return;

		try
		{
			writeLock();

			undoMgr.endCompoundEdit();
		}
		finally
		{
			writeUnlock();
		}
	}//}}}

	//{{{ insideCompoundEdit() method
	/**
	 * Returns if a compound edit is currently active.
	 * @since jEdit 3.1pre1
	 */
	public boolean insideCompoundEdit()
	{
		return undoMgr.insideCompoundEdit();
	} //}}}

	//}}}

	//{{{ Buffer events

	//{{{ addBufferChangeListener() method
	/**
	 * Adds a buffer change listener.
	 * @param listener The listener
	 * @since jEdit 4.0pre1
	 */
	public void addBufferChangeListener(BufferChangeListener l)
	{
		bufferListeners.addElement(l);
	} //}}}

	//{{{ removeBufferChangeListener() method
	/**
	 * Removes a buffer change listener.
	 * @param listener The listener
	 * @since jEdit 4.0pre1
	 */
	public void removeBufferChangeListener(BufferChangeListener l)
	{
		bufferListeners.removeElement(l);
	} //}}}

	//}}}

	//{{{ Property methods

	//{{{ propertiesChanged() method
	/**
	 * Reloads settings from the properties. This should be called
	 * after the <code>syntax</code> or <code>folding</code>
	 * buffer-local properties are changed.
	 */
	public void propertiesChanged()
	{
		setTokenMarker(mode.getTokenMarker());

		String folding = getStringProperty("folding");
		if("explicit".equals(folding))
			setFoldHandler(new ExplicitFoldHandler());
		else if("indent".equals(folding))
			setFoldHandler(new IndentFoldHandler());
		else
			setFoldHandler(new DummyFoldHandler());
	} //}}}

	//{{{ getTabSize() method
	/**
	 * Returns the tab size used in this buffer. This is equivalent
	 * to calling getProperty("tabSize").
	 */
	public int getTabSize()
	{
		return getIntegerProperty("tabSize",8);
	} //}}}

	//{{{ getIndentSize() method
	/**
	 * Returns the indent size used in this buffer. This is equivalent
	 * to calling getProperty("indentSize").
	 * @since jEdit 2.7pre1
	 */
	public final int getIndentSize()
	{
		return getIntegerProperty("indentSize",8);
	} //}}}

	//{{{ getProperty() method
	/**
	 * Returns the value of a buffer-local property.
	 * @param name The property name. For backwards compatibility, this
	 * is an <code>Object</code>, not a <code>String</code>.
	 */
	public Object getProperty(Object name)
	{
		// First try the buffer-local properties
		Object o = properties.get(name);
		if(o != null)
			return o;

		// For backwards compatibility
		if(!(name instanceof String))
			return null;

		// Now try mode.<mode>.<property>
		if(mode != null)
			return mode.getProperty((String)name);
		else
		{
			// Now try buffer.<property>
			String value = jEdit.getProperty("buffer." + name);
			if(value == null)
				return null;

			// Try returning it as an integer first
			try
			{
				return new Integer(value);
			}
			catch(NumberFormatException nf)
			{
				return value;
			}
		}
	} //}}}

	//{{{ setProperty() method
	/**
	 * Sets the value of a buffer-local property.
	 * @param name The property name
	 * @param value The property value
	 * @since jEdit 4.0pre1
	 */
	public void setProperty(String name, Object value)
	{
		putProperty(name,value);
	} //}}}

	//{{{ unsetProperty() method
	/**
	 * Clears the value of a buffer-local property.
	 * @param name The property name
	 * @since jEdit 4.0pre1
	 */
	public void unsetProperty(String name)
	{
		properties.remove(name);
	} //}}}

	//{{{ getStringProperty() method
	/**
	 * Returns the value of a string property.
	 * @param name The property name
	 * @since jEdit 4.0pre1
	 */
	public String getStringProperty(String name)
	{
		Object obj = getProperty(name);
		if(obj != null)
			return obj.toString();
		else
			return null;
	} //}}}

	//{{{ setStringProperty() method
	/**
	 * Sets a string property.
	 * @param name The property name
	 * @param value The value
	 * @since jEdit 4.0pre1
	 */
	public void setStringProperty(String name, String value)
	{
		setProperty(name,value);
	} //}}}

	//{{{ getBooleanProperty() method
	/**
	 * Returns the value of a boolean property.
	 * @param name The property name
	 * @since jEdit 4.0pre1
	 */
	public boolean getBooleanProperty(String name)
	{
		Object obj = getProperty(name);
		if(obj instanceof Boolean)
			return ((Boolean)obj).booleanValue();
		else if("true".equals(obj) || "on".equals(obj) || "yes".equals(obj))
			return true;
		else
			return false;
	} //}}}

	//{{{ setBooleanProperty() method
	/**
	 * Sets a boolean property.
	 * @param name The property name
	 * @param value The value
	 * @since jEdit 4.0pre1
	 */
	public void setBooleanProperty(String name, boolean value)
	{
		setProperty(name,value ? Boolean.TRUE : Boolean.FALSE);
	} //}}}

	//{{{ getIntegerProperty() method
	/**
	 * Returns the value of an integer property.
	 * @param name The property name
	 * @since jEdit 4.0pre1
	 */
	public int getIntegerProperty(String name, int defaultValue)
	{
		Object obj = getProperty(name);
		if(obj instanceof Number)
			return ((Number)obj).intValue();
		else
		{
			try
			{
				int value = Integer.parseInt(getStringProperty(name));
				properties.put(name,new Integer(value));
				return value;
			}
			catch(Exception e)
			{
				return defaultValue;
			}
		}
	} //}}}

	//{{{ setIntegerProperty() method
	/**
	 * Sets an integer property.
	 * @param name The property name
	 * @param value The value
	 * @since jEdit 4.0pre1
	 */
	public void setIntegerProperty(String name, int value)
	{
		setProperty(name,new Integer(value));
	} //}}}

	//{{{ getKeywordMapAtOffset() method
	/**
	 * Returns the syntax highlighting keyword map in effect at the
	 * specified offset. Used by the <b>Complete Word</b> command to
	 * complete keywords.
	 * @param offset The offset
	 * @since jEdit 4.0pre3
	 */
	public KeywordMap getKeywordMapAtOffset(int offset)
	{
		return getRuleSetAtOffset(offset).getKeywords();
	} //}}}

	//{{{ getContextSensitiveProperty() method
	/**
	 * Some settings, like comment start and end strings, can
	 * vary between different parts of a buffer (HTML text and inline
	 * JavaScript, for example).
	 * @param offset The offset
	 * @param name The property name
	 * @since jEdit 4.0pre3
	 */
	public String getContextSensitiveProperty(int offset, String name)
	{
		ParserRuleSet rules = getRuleSetAtOffset(offset);

		Object value = null;

		Hashtable rulesetProps = rules.getProperties();
		if(rulesetProps != null)
			value = rulesetProps.get(name);

		if(value == null)
		{
			value = rules.getMode().getProperty(name);

			if(value == null)
				value = mode.getProperty(name);
		}

		if(value == null)
			return null;
		else
			return String.valueOf(value);
	} //}}}

	//}}}

	//{{{ Edit modes, syntax highlighting, auto indent

	//{{{ getMode() method
	/**
	 * Returns this buffer's edit mode.
	 */
	public final Mode getMode()
	{
		return mode;
	} //}}}

	//{{{ setMode() method
	/**
	 * Sets this buffer's edit mode. Note that calling this before a buffer
	 * is loaded will have no effect; in that case, set the "mode" property
	 * to the name of the mode. A bit inelegant, I know...
	 * @param mode The mode
	 */
	public void setMode(Mode mode)
	{
		/* This protects against stupid people (like me)
		 * doing stuff like buffer.setMode(jEdit.getMode(...)); */
		if(mode == null)
			throw new NullPointerException("Mode must be non-null");

		// still need to set up new fold handler, etc even if mode not
		// changed.
		//if(this.mode == mode)
		//	return;

		//{{{ Reset cached properties
		if(getProperty("tabSize")
			.equals(mode.getProperty("tabSize")))
			unsetProperty("tabSize");

		if(getProperty("indentSize")
			.equals(mode.getProperty("indentSize")))
			unsetProperty("indentSize");

		if(getProperty("maxLineLen")
			.equals(mode.getProperty("maxLineLen")))
			unsetProperty("maxLineLen");
		//}}}

		Mode oldMode = this.mode;

		this.mode = mode;

		//{{{ Cache these for improved performance
		putProperty("tabSize",getProperty("tabSize"));
		putProperty("indentSize",getProperty("indentSize"));
		putProperty("maxLineLen",getProperty("maxLineLen"));
		//}}}

		propertiesChanged(); // sets up token marker

		// don't fire it for initial mode set
		if(oldMode != null && !getFlag(TEMPORARY))
		{
			EditBus.send(new BufferUpdate(this,null,
				BufferUpdate.MODE_CHANGED));
		}
	} //}}}

	//{{{ setMode() method
	/**
	 * Sets this buffer's edit mode by calling the accept() method
	 * of each registered edit mode.
	 */
	public void setMode()
	{
		setModeForFirstLine(getLineText(0));
	} //}}}

	//{{{ indentLine() method
	/**
	 * If auto indent is enabled, this method is called when the `Tab'
	 * or `Enter' key is pressed to perform mode-specific indentation
	 * and return true, or return false if a normal tab is to be inserted.
	 * @param line The line number to indent
	 * @param canIncreaseIndent If false, nothing will be done if the
	 * calculated indent is greater than the current
	 * @param canDecreaseIndent If false, nothing will be done if the
	 * calculated indent is less than the current
	 * @return true if the tab key event should be swallowed (ignored)
	 * false if a real tab should be inserted
	 */
	public boolean indentLine(int lineIndex, boolean canIncreaseIndent,
		boolean canDecreaseIndent)
	{
		if(lineIndex == 0)
			return false;

		// Get properties
		String openBrackets = (String)getProperty("indentOpenBrackets");
		String closeBrackets = (String)getProperty("indentCloseBrackets");
		String _indentPrevLine = (String)getProperty("indentPrevLine");
		boolean doubleBracketIndent = getBooleanProperty("doubleBracketIndent");
		RE indentPrevLineRE = null;
		if(openBrackets == null)
			openBrackets = "";
		if(closeBrackets == null)
			closeBrackets = "";
		if(_indentPrevLine != null)
		{
			try
			{
				indentPrevLineRE = new RE(_indentPrevLine,
					RE.REG_ICASE,RESearchMatcher.RE_SYNTAX_JEDIT);
			}
			catch(REException re)
			{
				Log.log(Log.ERROR,this,"Invalid 'indentPrevLine'"
					+ " regexp: " + _indentPrevLine);
				Log.log(Log.ERROR,this,re);
			}
		}

		int tabSize = getTabSize();
		int indentSize = getIndentSize();
		boolean noTabs = getBooleanProperty("noTabs");

		Element map = getDefaultRootElement();

		String prevLine = null;
		String line = null;

		Element lineElement = map.getElement(lineIndex);
		int start = lineElement.getStartOffset();

		// Get line text
		line = getText(start,lineElement.getEndOffset() - start - 1);

		for(int i = lineIndex - 1; i >= 0; i--)
		{
			lineElement = map.getElement(i);
			int lineStart = lineElement.getStartOffset();
			int len = lineElement.getEndOffset() - lineStart - 1;
			if(len != 0)
			{
				prevLine = getText(lineStart,len);
				break;
			}
		}

		if(prevLine == null)
			return false;

		/*
		 * If 'prevLineIndent' matches a line --> +1
		 */
		boolean prevLineMatches = (indentPrevLineRE == null ? false
			: indentPrevLineRE.isMatch(prevLine));

		/*
		 * On the previous line,
		 * if(bob) { --> +1
		 * if(bob) { } --> 0
		 * } else if(bob) { --> +1
		 */
		boolean prevLineStart = true; // False after initial indent
		int prevLineIndent = 0; // Indent width (tab expanded)
		int prevLineBrackets = 0; // Additional bracket indent
		for(int i = 0; i < prevLine.length(); i++)
		{
			char c = prevLine.charAt(i);
			switch(c)
			{
			case ' ':
				if(prevLineStart)
					prevLineIndent++;
				break;
			case '\t':
				if(prevLineStart)
				{
					prevLineIndent += (tabSize
						- (prevLineIndent
						% tabSize));
				}
				break;
			default:
				prevLineStart = false;
				if(closeBrackets.indexOf(c) != -1)
					prevLineBrackets = Math.max(
						prevLineBrackets-1,0);
				else if(openBrackets.indexOf(c) != -1)
				{
					/*
					 * If supressBracketAfterIndent is true
					 * and we have something that looks like:
					 * if(bob)
					 * {
					 * then the 'if' will not shift the indent,
					 * because of the {.
					 *
					 * If supressBracketAfterIndent is false,
					 * the above would be indented like:
					 * if(bob)
					 *         {
					 */
					if(!doubleBracketIndent)
						prevLineMatches = false;
					prevLineBrackets++;
				}
				break;
			}
		}

		// This is a hack so that auto indent does not go haywire
		// with explicit folding. Proper fix will be done later,
		// when the auto indent is rewritten.
		if(prevLineBrackets == 3)
			prevLineBrackets = 0;

		/*
		 * On the current line,
		 * } --> -1
		 * } else if(bob) { --> -1
		 * if(bob) { } --> 0
		 */
		boolean lineStart = true; // False after initial indent
		int lineIndent = 0; // Indent width (tab expanded)
		int lineWidth = 0; // White space count
		int lineBrackets = 0; // Additional bracket indent
		int closeBracketIndex = -1; // For lining up closing
			// and opening brackets
		for(int i = 0; i < line.length(); i++)
		{
			char c = line.charAt(i);
			switch(c)
			{
			case ' ':
				if(lineStart)
				{
					lineIndent++;
					lineWidth++;
				}
				break;
			case '\t':
				if(lineStart)
				{
					lineIndent += (tabSize
						- (lineIndent
						% tabSize));
					lineWidth++;
				}
				break;
			default:
				lineStart = false;
				if(closeBrackets.indexOf(c) != -1)
				{
					if(lineBrackets == 0)
						closeBracketIndex = i;
					else
						lineBrackets--;
				}
				else if(openBrackets.indexOf(c) != -1)
				{
					if(!doubleBracketIndent)
						prevLineMatches = false;
					lineBrackets++;
				}

				break;
			}
		}

		// This is a hack so that auto indent does not go haywire
		// with explicit folding. Proper fix will be done later,
		// when the auto indent is rewritten.
		if(lineBrackets == 3)
		{
			closeBracketIndex = 0;
			lineBrackets = 0;
		}

		if(closeBracketIndex != -1)
		{
			int offset = TextUtilities.findMatchingBracket(
				this,lineIndex,closeBracketIndex);
			if(offset != -1)
			{
				lineElement = map.getElement(map.getElementIndex(
					offset));
				int startOffset = lineElement.getStartOffset();
				String closeLine = getText(startOffset,
					lineElement.getEndOffset() - startOffset - 1);
				prevLineIndent = MiscUtilities
					.getLeadingWhiteSpaceWidth(
					closeLine,tabSize);
			}
			else
				return false;
		}
		else
		{
			prevLineIndent += (prevLineBrackets * indentSize);
		}

		if(prevLineMatches)
			prevLineIndent += indentSize;

		if(!canDecreaseIndent && prevLineIndent <= lineIndent)
			return false;

		if(!canIncreaseIndent && prevLineIndent >= lineIndent)
			return false;

		// Do it
		try
		{
			beginCompoundEdit();

			remove(start,lineWidth);
			insert(start,MiscUtilities.createWhiteSpace(
				prevLineIndent,(noTabs ? 0 : tabSize)));
		}
		finally
		{
			endCompoundEdit();
		}

		return true;
	} //}}}

	//{{{ indentLines() method
	/**
	 * Indents all specified lines.
	 * @param start The first line to indent
	 * @param end The last line to indent
	 * @since jEdit 3.1pre3
	 */
	public void indentLines(int start, int end)
	{
		try
		{
			beginCompoundEdit();
			for(int i = start; i <= end; i++)
				indentLine(i,true,true);
		}
		finally
		{
			endCompoundEdit();
		}
	} //}}}

	//{{{ indentLines() method
	/**
	 * Indents all specified lines.
	 * @param lines The line numbers
	 * @since jEdit 3.2pre1
	 */
	public void indentLines(int[] lines)
	{
		try
		{
			beginCompoundEdit();
			for(int i = 0; i < lines.length; i++)
				indentLine(lines[i],true,true);
		}
		finally
		{
			endCompoundEdit();
		}
	} //}}}

	//{{{ paintSyntaxLine() method
	/**
	 * Paints the specified line onto the graphics context.
	 * @since jEdit 3.2pre6
	 */
	public int paintSyntaxLine(int lineIndex, Graphics gfx, int _x, int _y,
		TabExpander expander, boolean style, boolean color,
		Font defaultFont, Color foreground, Color background,
		SyntaxStyle[] styles, TextRenderer renderer)
	{
		float x = (float)_x;
		float y = (float)_y;

		if(lastTokenizedLine == lineIndex)
			getLineText(lineIndex,seg);
		else
			markTokens(lineIndex);

		Token tokens = tokenList.firstToken;

		// the above should leave the text in the 'seg' segment
		char[] text = seg.array;

		int off = seg.offset;

		for(;;)
		{
			byte id = tokens.id;
			if(id == Token.END)
				break;

			Color tokenForeground;
			Color tokenBackground = null;
			if(id == Token.NULL)
			{
				gfx.setFont(defaultFont);
				tokenForeground = foreground;
			}
			else
			{
				if(style)
					gfx.setFont(styles[id].getFont());
				else
					gfx.setFont(defaultFont);

				if(color)
				{
					tokenBackground = styles[id].getBackgroundColor();
					tokenForeground = styles[id].getForegroundColor();
					if(tokenForeground == null)
						tokenForeground = foreground;
				}
				else
					tokenForeground = foreground;
			}

			int len = tokens.length;
			/*if(off + len > seg.offset + seg.count)
			{
				System.err.println((off - seg.offset)
					+ "+" + len + ":" + seg.count);
			}*/
			x = renderer.drawChars(text,off,len,gfx,x,y,expander,
				tokenForeground,tokenBackground,background);

			off += len;

			tokens = tokens.next;
		}

		return (int)x;
	} //}}}

	//{{{ markTokens() method
	/**
	 * Returns the syntax tokens for the specified line.
	 * @param lineIndex The line number
	 * @since jEdit 4.0pre1
	 */
	public TokenList markTokens(int lineIndex)
	{
		try
		{
			writeLock();

			if(lineIndex < 0 || lineIndex >= offsetMgr.getLineCount())
				throw new ArrayIndexOutOfBoundsException(lineIndex);

			/* If cached tokens are valid, return 'em */
			if(lastTokenizedLine == lineIndex)
				return tokenList;

			/*
			 * Else, go up to 100 lines back, looking for a line with
			 * a valid line context.
			 */
			int start = Math.max(0,lineIndex - 100) - 1;
			int end = Math.max(0,lineIndex - 100);

			for(int i = lineIndex - 1; i > end; i--)
			{
				if(offsetMgr.isLineContextValid(i))
				{
					start = i;
					break;
				}
			}

			for(int i = start + 1; i <= lineIndex; i++)
			{
				getLineText(i,seg);

				TokenMarker.LineContext prevContext = (i == 0 ? null
					: offsetMgr.getLineContext(i - 1));

				/* Prepare tokenization */
				tokenList.lastToken = null;

				TokenMarker.LineContext context = offsetMgr.getLineContext(i);
				ParserRule oldRule;
				ParserRuleSet oldRules;
				if(context == null)
				{
					oldRule = null;
					oldRules = null;
				}
				else
				{
					oldRule = context.inRule;
					oldRules = context.rules;
				}

				context = tokenMarker.markTokens(prevContext,tokenList,seg);
				offsetMgr.setLineContext(i,context);

				// Could incorrectly be set to 'false' with
				// recursive delegates, where the chaining might
				// have changed but not the rule set in question (?)
				if(oldRule != context.inRule)
					nextLineRequested = true;
				else if(oldRules != context.rules)
					nextLineRequested = true;
				else if(i != lastTokenizedLine)
					nextLineRequested = false;
			}

			lastTokenizedLine = lineIndex;

			int lineCount = offsetMgr.getLineCount();
			if(nextLineRequested && lineCount - lineIndex > 1)
			{
				offsetMgr.linesChanged(lineIndex + 1,lineCount - lineIndex - 1);
			}

			return tokenList;
		}
		finally
		{
			writeUnlock();
		}
	} //}}}

	//{{{ isNextLineRequested() method
	/**
	 * Returns true if the next line should be repainted. This
	 * will return true after a line has been tokenized that starts
	 * a multiline token that continues onto the next line.
	 */
	public boolean isNextLineRequested()
	{
		return nextLineRequested;
	} //}}}

	//{{{ getTokenMarker() method
	/**
	 * This method is only public so that the <code>OffsetManager</code>
	 * class can use it.
	 * @since jEdit 4.0pre1
	 */
	public TokenMarker getTokenMarker()
	{
		return tokenMarker;
	} //}}}

	//}}}

	//{{{ Deprecated methods

	//{{{ addDocumentListener() method
	/**
	 * @deprecated Write a <code>BufferChangeListener</code> instead
	 */
	public void addDocumentListener(DocumentListener l)
	{
		Log.log(Log.WARNING,this,"Document listeners not supported: "
			+ l.getClass().getName());
	} //}}}

	//{{{ removeDocumentListener() method
	/**
	 * @deprecated Write a <code>BufferChangeListener</code> instead
	 */
	public void removeDocumentListener(DocumentListener l)
	{
		Log.log(Log.WARNING,this,"Document listeners not supported: "
			+ l.getClass().getName());
	} //}}}

	//{{{ putProperty() method
	/**
	 * @deprecated Call <code>setProperty()</code> instead.
	 */
	public void putProperty(Object name, Object value)
	{
		if(value == null)
			properties.remove(name);
		else
			properties.put(name,value);
	} //}}}

	//{{{ putBooleanProperty() method
	/**
	 * @deprecated Call <code>setBooleanProperty()</code> instead
	 */
	public void putBooleanProperty(String name, boolean value)
	{
		setBooleanProperty(name,value);
	} //}}}

	//{{{ isSaving() method
	/**
	 * @deprecated Call isPerformingIO() instead
	 */
	public final boolean isSaving()
	{
		return getFlag(IO);
	} //}}}

	//{{{ tokenizeLines() method
	/**
	 * @deprecated Don't call this method.
	 */
	public void tokenizeLines() {} //}}}

	//{{{ tokenizeLines() method
	/**
	 * @deprecated
	 */
	public void tokenizeLines(int start, int len)
	{
	} //}}}

	//{{{ isLineVisible() method
	/**
	 * @deprecated Fold visibility is now stored on a per-text area
	 * basis. Call <code>textArea.getFoldVisibilityManager()</code>
	 * to get a visibility manager, and call the
	 * <code>isLineVisible()</code> method on that object instead.
	 */
	public boolean isLineVisible(int line)
	{
		return true;
	} //}}}

	//{{{ getVirtualLineCount() method
	/**
	 * @deprecated
	 */
	public int getVirtualLineCount()
	{
		return offsetMgr.getLineCount();
	} //}}}

	//{{{ getPrevVisibleLine() method
	/**
	 * @deprecated Fold visibility is now stored on a per-text area
	 * basis. Call <code>textArea.getFoldVisibilityManager()</code>
	 * to get a visibility manager, and call the
	 * <code>getPrevVisibleLine()</code> method on that object instead.
	 */
	public int getPrevVisibleLine(int lineNo)
	{
		return lineNo - 1;
	} //}}}

	//{{{ getNextVisibleLine() method
	/**
	 * @deprecated Fold visibility is now stored on a per-text area
	 * basis. Call <code>textArea.getFoldVisibilityManager()</code>
	 * to get a visibility manager, and call the
	 * <code>getNextVisibleLine()</code> method on that object instead.
	 */
	public int getNextVisibleLine(int lineNo)
	{
		if(lineNo >= offsetMgr.getLineCount() - 1)
			return -1;
		else
			return lineNo + 1;
	} //}}}

	//{{{ virtualToPhysical() method
	/**
	 * @deprecated Fold visibility is now stored on a per-text area
	 * basis. Call <code>textArea.getFoldVisibilityManager()</code>
	 * to get a visibility manager, and call the
	 * <code>virtualToPhysical()</code> method on that object instead.
	 */
	public int virtualToPhysical(int lineNo)
	{
		return lineNo;
	} //}}}

	//{{{ physicalToVirtual() method
	/**
	 * @deprecated Fold visibility is now stored on a per-text area
	 * basis. Call <code>textArea.getFoldVisibilityManager()</code>
	 * to get a visibility manager, and call the
	 * <code>physicalToVirtual()</code> method on that object instead.
	 */
	public int physicalToVirtual(int lineNo)
	{
		return lineNo;
	} //}}}

	//{{{ getRootElements() method
	/**
	 * @deprecated
	 */
	public Element[] getRootElements()
	{
		return new Element[] { getDefaultRootElement() };
	} //}}}

	//{{{ getParagraphElement() method
	/**
	 * @deprecated
	 */
	public Element getParagraphElement(int offset)
	{
		return new LineElement(this,getLineOfOffset(offset));
	} //}}}

	//{{{ getDefaultRootElement() method
	/**
	 * @deprecated Use <code>getLineOfOffset()</code>,
	 * <code>getLineStartOffset()</code>, and
	 * <code>getLineEndOffset()</code> instead.
	 */
	public Element getDefaultRootElement()
	{
		return new RootElement(this);
	} //}}}

	//{{{ insertString() method
	/**
	 * @deprecated Call <code>insert()</code> instead.
	 */
	public void insertString(int offset, String str, AttributeSet attr)
	{
		insert(offset,str);
	} //}}}

	//}}}

	//{{{ Folding methods

	//{{{ isFoldStart() method
	/**
	 * Returns if the specified line begins a fold.
	 * @since jEdit 3.1pre1
	 */
	public boolean isFoldStart(int line)
	{
		return (line != getLineCount() - 1
			&& getFoldLevel(line) < getFoldLevel(line + 1));
	} //}}}

	//{{{ getFoldLevel() method
	/**
	 * Returns the fold level of the specified line.
	 * @param line A physical line index
	 * @since jEdit 3.1pre1
	 */
	public int getFoldLevel(int line)
	{
		try
		{
			writeLock();

			if(line < 0 || line >= offsetMgr.getLineCount())
				throw new ArrayIndexOutOfBoundsException(line);

			if(offsetMgr.isFoldLevelValid(line))
			{
				//System.err.println("level valid: " + line + ":"
				//	+ offsetMgr.getFoldLevel(line));
				return offsetMgr.getFoldLevel(line);
			}
			else
			{
				int start = 0;
				for(int i = line - 1; i >= 0; i--)
				{
					if(offsetMgr.isFoldLevelValid(i))
					{
						start = i + 1;
						break;
					}
				}

				int newFoldLevel = 0;
				boolean changed = false;

				for(int i = start; i <= line; i++)
				{
					newFoldLevel = foldHandler.getFoldLevel(this,i,seg);
					offsetMgr.setFoldLevel(i,newFoldLevel);
					changed = true;
				}

				if(changed && !getFlag(INSIDE_INSERT))
					fireFoldLevelChanged(start,line);

				return newFoldLevel;
			}
		}
		finally
		{
			writeUnlock();
		}
	} //}}}

	//{{{ getFoldAtLine() method
	/**
	 * Returns an array. The first element is the start line, the
	 * second element is the end line, of the fold containing the
	 * specified line number.
	 * @param line The line number
	 * @since jEdit 4.0pre3
	 */
	public int[] getFoldAtLine(int line)
	{
		int start, end;

		if(isFoldStart(line))
		{
			start = line;
			int foldLevel = getFoldLevel(line);

			line++;

			while(getFoldLevel(line) > foldLevel)
			{
				if(line == getLineCount() - 1)
					break;
				else
					line++;
			}
			end = line;
		}
		else
		{
			start = line;
			int foldLevel = getFoldLevel(line);
			while(getFoldLevel(start) >= foldLevel)
			{
				if(start == 0)
					break;
				else
					start--;
			}

			end = line;
			while(getFoldLevel(end) >= foldLevel)
			{
				if(end == getLineCount() - 1)
					break;
				else
					end++;
			}

			while(getLineLength(end) == 0 && end > start)
				end--;
		}

		return new int[] { start, end };
	} //}}}

	//{{{ _getFoldVisibilityManager() method
	/**
	 * Plugins and macros should call
	 * <code>textArea.getFoldVisibilityManager()</code>
	 * instead of this method.
	 * @param textArea The text area
	 * @since jEdit 4.0pre1
	 */
	public FoldVisibilityManager _getFoldVisibilityManager(
		JEditTextArea textArea)
	{
		FoldVisibilityManager mgr = new FoldVisibilityManager(this,textArea);

		// find it a bit that it can set in line's 'visible' flag sets
		for(int i = 0; i < inUseFVMs.length; i++)
		{
			if(inUseFVMs[i] == null)
			{
				inUseFVMs[i] = mgr;
				mgr._grab(i);
				return mgr;
			}
		}

		//XXX
		throw new InternalError("Too many text areas editing this buffer");
	} //}}}

	//{{{ _releaseFoldVisibilityManager() method
	/**
	 * Plugins and macros should not call this method.
	 * @param mgr The fold visibility manager
	 * @since jEdit 4.0pre1
	 */
	public void _releaseFoldVisibilityManager(FoldVisibilityManager mgr)
	{
		inUseFVMs[mgr._getIndex()] = null;
		mgr._release();
	} //}}}

	//{{{ _isLineVisible() method
	/**
	 * Plugins and macros should call
	 * <code>textArea.getFoldVisibilityManager().isLineVisible()</code>
	 * instead of this method.
	 * @since jEdit 4.0pre1
	 */
	public final boolean _isLineVisible(int line, int index)
	{
		if(line < 0 || line >= offsetMgr.getLineCount())
			throw new ArrayIndexOutOfBoundsException(line);

		return offsetMgr.isLineVisible(line,index);
	} //}}}

	//{{{ _setLineVisible() method
	/**
	 * Plugins and macros should not call this method.
	 * @since jEdit 4.0pre1
	 */
	public final void _setLineVisible(int line, int index, boolean visible)
	{
		if(line < 0 || line >= offsetMgr.getLineCount())
			throw new ArrayIndexOutOfBoundsException(line);

		offsetMgr.setLineVisible(line,index,visible);
	} //}}}

	//{{{ _getVirtualLineCount() method
	/**
	 * Plugins and macros should call
	 * <code>textArea.getVirtualLineCount()</code>
	 * instead of this method.
	 * @since jEdit 4.0pre1
	 */
	public final int _getVirtualLineCount(int index)
	{
		return offsetMgr.getVirtualLineCount(index);
	} //}}}

	//{{{ _setVirtualLineCount() method
	/**
	 * Plugins and macros should not call this method.
	 * @since jEdit 4.0pre1
	 */
	public final void _setVirtualLineCount(int index, int lineCount)
	{
		offsetMgr.setVirtualLineCount(index,lineCount);
	} //}}}

	//}}}

	//{{{ Position methods

	//{{{ createPosition() method
	/**
	 * Creates a floating position.
	 * @param offset The offset
	 */
	public Position createPosition(int offset)
	{
		try
		{
			readLock();

			if(offset < 0 || offset > contentMgr.getLength())
				throw new ArrayIndexOutOfBoundsException(offset);

			return offsetMgr.createPosition(offset);
		}
		finally
		{
			readUnlock();
		}
	} //}}}

	//}}}

	//{{{ Marker methods

	//{{{ getMarkers() method
	/**
	 * Returns a vector of markers.
	 * @since jEdit 3.2pre1
	 */
	public final Vector getMarkers()
	{
		return markers;
	} //}}}

	//{{{ addOrRemoveMarker() method
	/**
	 * If a marker is set on the line of the position, it is removed. Otherwise
	 * a new marker with the specified shortcut is added.
	 * @param pos The position of the marker
	 * @param shortcut The shortcut ('\0' if none)
	 * @since jEdit 3.2pre5
	 */
	public void addOrRemoveMarker(char shortcut, int pos)
	{
		int line = getLineOfOffset(pos);
		if(getMarkerAtLine(line) != null)
			removeMarker(line);
		else
			addMarker(shortcut,pos);
	} //}}}

	//{{{ addMarker() method
	/**
	 * Adds a marker to this buffer.
	 * @param pos The position of the marker
	 * @param shortcut The shortcut ('\0' if none)
	 * @since jEdit 3.2pre1
	 */
	public void addMarker(char shortcut, int pos)
	{
		if(!getFlag(READ_ONLY) && jEdit.getBooleanProperty("persistentMarkers"))
			setDirty(true);

		Marker markerN = new Marker(this,shortcut,pos);
		boolean added = false;

		int line = getLineOfOffset(pos);

		// don't sort markers while buffer is being loaded
		if(!getFlag(LOADING))
		{
			markerN.createPosition();

			for(int i = 0; i < markers.size(); i++)
			{
				Marker marker = (Marker)markers.elementAt(i);
				if(shortcut != '\0' && marker.getShortcut() == shortcut)
					marker.setShortcut('\0');

				if(getLineOfOffset(marker.getPosition()) == line)
				{
					markers.removeElementAt(i);
					i--;
				}
			}

			for(int i = 0; i < markers.size(); i++)
			{
				Marker marker = (Marker)markers.elementAt(i);
				if(marker.getPosition() > pos)
				{
					markers.insertElementAt(markerN,i);
					added = true;
					break;
				}
			}
		}

		if(!added)
			markers.addElement(markerN);

		if(!getFlag(LOADING) && !getFlag(TEMPORARY))
		{
			EditBus.send(new BufferUpdate(this,null,
				BufferUpdate.MARKERS_CHANGED));
		}
	} //}}}

	//{{{ getMarkerAtLine() method
	/**
	 * Returns the first marker at the specified line.
	 * @param line The line number
	 * @since jEdit 3.2pre2
	 */
	public Marker getMarkerAtLine(int line)
	{
		for(int i = 0; i < markers.size(); i++)
		{
			Marker marker = (Marker)markers.elementAt(i);
			if(getLineOfOffset(marker.getPosition()) == line)
				return marker;
		}

		return null;
	} //}}}

	//{{{ removeMarker() method
	/**
	 * Removes all markers at the specified line.
	 * @param line The line number
	 * @since jEdit 3.2pre2
	 */
	public void removeMarker(int line)
	{
		for(int i = 0; i < markers.size(); i++)
		{
			Marker marker = (Marker)markers.elementAt(i);
			if(getLineOfOffset(marker.getPosition()) == line)
			{
				if(!getFlag(READ_ONLY) && jEdit.getBooleanProperty("persistentMarkers"))
					setDirty(true);

				marker.removePosition();
				markers.removeElementAt(i);
				i--;
			}
		}

		EditBus.send(new BufferUpdate(this,null,
			BufferUpdate.MARKERS_CHANGED));
	} //}}}

	//{{{ removeAllMarkers() method
	/**
	 * Removes all defined markers.
	 * @since jEdit 2.6pre1
	 */
	public void removeAllMarkers()
	{
		if(!getFlag(READ_ONLY) && jEdit.getBooleanProperty("persistentMarkers"))
			setDirty(true);

		for(int i = 0; i < markers.size(); i++)
			((Marker)markers.elementAt(i)).removePosition();

		markers.removeAllElements();

		EditBus.send(new BufferUpdate(this,null,
			BufferUpdate.MARKERS_CHANGED));
	} //}}}

	//{{{ getMarker() method
	/**
	 * Returns the marker with the specified shortcut.
	 * @param shortcut The shortcut
	 * @since jEdit 3.2pre2
	 */
	public Marker getMarker(char shortcut)
	{
		Enumeration enum = markers.elements();
		while(enum.hasMoreElements())
		{
			Marker marker = (Marker)enum.nextElement();
			if(marker.getShortcut() == shortcut)
				return marker;
		}
		return null;
	} //}}}

	//}}}

	//{{{ Miscellaneous methods

	//{{{ getNext() method
	/**
	 * Returns the next buffer in the list.
	 */
	public final Buffer getNext()
	{
		return next;
	} //}}}

	//{{{ getPrev() method
	/**
	 * Returns the previous buffer in the list.
	 */
	public final Buffer getPrev()
	{
		return prev;
	} //}}}

	//{{{ getIndex() method
	/**
	 * Returns the position of this buffer in the buffer list.
	 */
	public final int getIndex()
	{
		int count = 0;
		Buffer buffer = prev;
		for(;;)
		{
			if(buffer == null)
				break;
			count++;
			buffer = buffer.prev;
		}
		return count;
	} //}}}

	//{{{ toString() method
	/**
	 * Returns a string representation of this buffer.
	 * This simply returns the path name.
	 */
	public String toString()
	{
		return name + " (" + MiscUtilities.getParentOfPath(path) + ")";
	} //}}}

	//{{{ handleMessage() method
	public void handleMessage(EBMessage msg)
	{
		if(msg instanceof PropertiesChanged)
			propertiesChanged();
		else if(msg instanceof EditPaneUpdate)
		{
			EditPaneUpdate emsg = (EditPaneUpdate)msg;
			if(emsg.getWhat() == EditPaneUpdate.CREATED)
			{
				// see getFoldVisibilityManager()
			}
			else if(emsg.getWhat() == EditPaneUpdate.DESTROYED)
			{
				JEditTextArea textArea = emsg.getEditPane()
					.getTextArea();
				FoldVisibilityManager mgr = textArea
					.getFoldVisibilityManager();

				for(int i = 0; i < inUseFVMs.length; i++)
				{
					if(mgr == inUseFVMs[i])
					{
						mgr._release();
						inUseFVMs[i] = null;
						break;
					}
				}
			}
		}
	} //}}}

	//}}}

	//{{{ Package-private members
	Buffer prev;
	Buffer next;

	//{{{ Buffer constructor
	Buffer(View view, String path, boolean newFile, boolean temp,
		Hashtable props)
	{
		properties = ((Hashtable)props.clone());

		// fill in defaults for these from system properties.
		if(properties.get(ENCODING) == null)
			properties.put(ENCODING,System.getProperty("file.encoding"));
		if(properties.get(LINESEP) == null)
			properties.put(LINESEP,System.getProperty("line.separator"));

		lock = new ReadWriteLock();
		contentMgr = new ContentManager();
		offsetMgr = new OffsetManager(this);
		integerArray = new IntegerArray();
		undoMgr = new UndoManager(this);
		bufferListeners = new Vector();

		seg = new Segment();
		lastTokenizedLine = -1;
		tokenList = new TokenList();

		inUseFVMs = new FoldVisibilityManager[8];

		setFlag(TEMPORARY,temp);

		markers = new Vector();

		// this must be called before any EditBus messages are sent
		setPath(path);

		Mode defaultMode = jEdit.getMode(jEdit.getProperty("buffer.defaultMode"));
		if(defaultMode == null)
			defaultMode = jEdit.getMode("text");
		setMode(defaultMode);

		/*Magic: UNTITLED is only set if newFile param to
		 * constructor is set, NEW_FILE is also set if file
		 * doesn't exist on disk.
		 *
		 * This is so that we can tell apart files created
		 * with jEdit.newFile(), and those that just don't
		 * exist on disk.
		 *
		 * Why do we need to tell the difference between the
		 * two? jEdit.addBufferToList() checks if the only
		 * opened buffer is an untitled buffer, and if so,
		 * replaces it with the buffer to add. We don't want
		 * this behavior to occur with files that don't
		 * exist on disk; only untitled ones.
		 */
		setFlag(UNTITLED,newFile);
		setFlag(NEW_FILE,newFile);

		if(!temp)
			EditBus.addToBus(Buffer.this);
	} //}}}

	//{{{ commitTemporary() method
	void commitTemporary()
	{
		setFlag(TEMPORARY,false);
		EditBus.addToBus(this);
	} //}}}

	//{{{ close() method
	void close()
	{
		setFlag(CLOSED,true);

		if(autosaveFile != null)
			autosaveFile.delete();

		EditBus.removeFromBus(this);
	} //}}}

	//}}}

	//{{{ Private members

	//{{{ Flags

	//{{{ setFlag() method
	private void setFlag(int flag, boolean value)
	{
		if(value)
			flags |= (1 << flag);
		else
			flags &= ~(1 << flag);
	} //}}}

	//{{{ getFlag() method
	private boolean getFlag(int flag)
	{
		int mask = (1 << flag);
		return (flags & mask) == mask;
	} //}}}

	//{{{ Flag values
	private static final int CLOSED = 0;
	private static final int LOADING = 1;
	private static final int IO = 2;
	private static final int NEW_FILE = 3;
	private static final int UNTITLED = 4;
	private static final int AUTOSAVE_DIRTY = 5;
	private static final int DIRTY = 6;
	private static final int READ_ONLY = 7;
	private static final int UNDO_IN_PROGRESS = 8;
	private static final int TEMPORARY = 9;
	private static final int INSIDE_INSERT = 10;
	//}}}

	private int flags;

	//}}}

	//{{{ Instance variables
	private VFS vfs;
	private String path;
	private String name;
	private File file;
	private File autosaveFile;
	private long modTime;
	private Mode mode;
	private Hashtable properties;

	private ReadWriteLock lock;
	private ContentManager contentMgr;
	private OffsetManager offsetMgr;
	private IntegerArray integerArray;
	private UndoManager undoMgr;
	private Vector bufferListeners;

	private Vector markers;

	// Syntax highlighting
	private TokenMarker tokenMarker;
	private Segment seg;
	private int lastTokenizedLine;
	private boolean nextLineRequested;
	private TokenList tokenList;

	// Folding
	private FoldHandler foldHandler;
	private FoldVisibilityManager[] inUseFVMs;

	//}}}

	//{{{ setPath() method
	private void setPath(String path)
	{
		this.path = path;
		name = MiscUtilities.getFileName(path);

		vfs = VFSManager.getVFSForPath(path);
		if((vfs.getCapabilities() & VFS.WRITE_CAP) == 0)
			setReadOnly(true);

		if(vfs instanceof FileVFS)
		{
			file = new File(path);

			// if we don't do this, the autosave file won't be
			// deleted after a save as
			if(autosaveFile != null)
				autosaveFile.delete();
			autosaveFile = new File(file.getParent(),'#' + name + '#');
		}
	} //}}}

	//{{{ recoverAutosave() method
	private boolean recoverAutosave(final View view)
	{
		if(!autosaveFile.canRead())
			return false;

		// this method might get called at startup
		GUIUtilities.hideSplashScreen();

		final Object[] args = { autosaveFile.getPath() };
		int result = GUIUtilities.confirm(view,"autosave-found",args,
			JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);

		if(result == JOptionPane.YES_OPTION)
		{
			vfs.load(view,this,autosaveFile.getPath());

			// show this message when all I/O requests are
			// complete
			VFSManager.runInAWTThread(new Runnable()
			{
				public void run()
				{
					GUIUtilities.message(view,"autosave-loaded",args);
				}
			});

			return true;
		}
		else
			return false;
	} //}}}

	//{{{ parseBufferLocalProperties() method
	private void parseBufferLocalProperties()
	{
		int lastLine = Math.min(10,getLineCount() - 1);
		parseBufferLocalProperties(getText(0,getLineEndOffset(lastLine) - 1));
	} //}}}

	//{{{ parseBufferLocalProperties() method
	private void parseBufferLocalProperties(String prop)
	{
		//{{{ Reset cached properties
		if(getProperty("tabSize")
			.equals(mode.getProperty("tabSize")))
			unsetProperty("tabSize");

		if(getProperty("indentSize")
			.equals(mode.getProperty("indentSize")))
			unsetProperty("indentSize");

		if(getProperty("maxLineLen")
			.equals(mode.getProperty("maxLineLen")))
			unsetProperty("maxLineLen");
		//}}}

		StringBuffer buf = new StringBuffer();
		String name = null;
		boolean escape = false;
		for(int i = 0; i < prop.length(); i++)
		{
			char c = prop.charAt(i);
			switch(c)
			{
			case ':':
				if(escape)
				{
					escape = false;
					buf.append(':');
					break;
				}
				if(name != null)
				{
					String value = buf.toString();
					try
					{
						setProperty(name,new Integer(value));
					}
					catch(NumberFormatException nf)
					{
						setProperty(name,value);
					}
					name = null;
				}
				buf.setLength(0);
				break;
			case '=':
				if(escape)
				{
					escape = false;
					buf.append('=');
					break;
				}
				name = buf.toString();
				buf.setLength(0);
				break;
			case '\\':
				if(escape)
					buf.append('\\');
				escape = !escape;
				break;
			case 'n':
				if(escape)
				{	buf.append('\n');
					escape = false;
					break;
				}
			case 't':
				if(escape)
				{
					buf.append('\t');
					escape = false;
					break;
				}
			default:
				buf.append(c);
				break;
			}
		}

		//XXX: Why the fuck is this here???

		// Create marker positions
		for(int i = 0; i < markers.size(); i++)
		{
			((Marker)markers.elementAt(i))
				.createPosition();
		}
	} //}}}

	//{{{ setModeForFirstLine() method
	private void setModeForFirstLine(String firstLine)
	{
		String userMode = getStringProperty("mode");
		if(userMode != null)
		{
			Mode m = jEdit.getMode(userMode);
			if(m != null)
			{
				setMode(m);
				return;
			}
		}

		String nogzName = name.substring(0,name.length() -
			(name.endsWith(".gz") ? 3 : 0));
		Mode[] modes = jEdit.getModes();

		for(int i = 0; i < modes.length; i++)
		{
			if(modes[i].accept(nogzName,firstLine))
			{
				setMode(modes[i]);
				return;
			}
		}

		Mode defaultMode = jEdit.getMode(jEdit.getProperty("buffer.defaultMode"));
		if(defaultMode == null)
			defaultMode = jEdit.getMode("text");
		setMode(defaultMode);
	} //}}}

	//{{{ setTokenMarker() method
	private void setTokenMarker(TokenMarker tokenMarker)
	{
		TokenMarker oldTokenMarker = this.tokenMarker;

		this.tokenMarker = tokenMarker;

		// don't do this on initial token marker
		if(oldTokenMarker != null)
			offsetMgr.linesChanged(0,offsetMgr.getLineCount());

		lastTokenizedLine = -1;
	} //}}}

	//{{{ setFoldHandler() method
	private void setFoldHandler(FoldHandler foldHandler)
	{
		FoldHandler oldFoldHandler = this.foldHandler;

		//XXX: Will break with possible future plugin fold handlers
		if(oldFoldHandler != null
			&& oldFoldHandler.getClass() == foldHandler.getClass())
			return;

		this.foldHandler = foldHandler;

		// don't do this on initial fold handler creation
		if(oldFoldHandler != null)
		{
			offsetMgr.linesChanged(0,offsetMgr.getLineCount());
			EditBus.send(new BufferUpdate(this,null,
				BufferUpdate.FOLD_HANDLER_CHANGED));
		}
	} //}}}

	//{{{ contentInserted() method
	private void contentInserted(int offset, int length, IntegerArray endOffsets)
	{
		try
		{
			setFlag(INSIDE_INSERT,true);

			int startLine = offsetMgr.getLineOfOffset(offset);
			int numLines = endOffsets.getSize();

			offsetMgr.contentInserted(startLine,offset,numLines,length,
				endOffsets);

			if(numLines > 0)
			{
				// notify fold visibility managers
				for(int i = 0; i < inUseFVMs.length; i++)
				{
					if(inUseFVMs[i] != null)
						inUseFVMs[i]._invalidate(startLine);
				}
			}

			if(lastTokenizedLine >= startLine)
				lastTokenizedLine = -1;

			fireContentInserted(startLine,offset,numLines,length);

			setDirty(true);
		}
		finally
		{
			setFlag(INSIDE_INSERT,false);
		}
	} //}}}

	//{{{ getRuleSetAtOffset() method
	private ParserRuleSet getRuleSetAtOffset(int offset)
	{
		int line = getLineOfOffset(offset);
		offset -= getLineStartOffset(line);
		if(offset != 0)
			offset--;

		TokenList tokens = markTokens(line);
		Token token = TextUtilities.getTokenAtOffset(tokens,offset);
		return token.rules;
	} //}}}

	//{{{ Event firing methods

	//{{{ fireFoldLevelChanged() method
	private void fireFoldLevelChanged(int line, int level)
	{
		for(int i = 0; i < bufferListeners.size(); i++)
		{
			try
			{
				((BufferChangeListener)bufferListeners.elementAt(i))
					.foldLevelChanged(this,line,level);
			}
			catch(Throwable t)
			{
				Log.log(Log.ERROR,this,"Exception while sending buffer event:");
				Log.log(Log.ERROR,this,t);
			}
		}
	} //}}}

	//{{{ fireContentInserted() method
	private void fireContentInserted(int startLine, int offset,
		int numLines, int length)
	{
		for(int i = 0; i < bufferListeners.size(); i++)
		{
			try
			{
				((BufferChangeListener)bufferListeners.elementAt(i))
					.contentInserted(this,startLine,offset,
					numLines,length);
			}
			catch(Throwable t)
			{
				Log.log(Log.ERROR,this,"Exception while sending buffer event:");
				Log.log(Log.ERROR,this,t);
			}
		}
	} //}}}

	//{{{ fireContentRemoved() method
	private void fireContentRemoved(int startLine, int offset,
		int numLines, int length)
	{
		for(int i = 0; i < bufferListeners.size(); i++)
		{
			try
			{
				((BufferChangeListener)bufferListeners.elementAt(i))
					.contentRemoved(this,startLine,offset,
					numLines,length);
			}
			catch(Throwable t)
			{
				Log.log(Log.ERROR,this,"Exception while sending buffer event:");
				Log.log(Log.ERROR,this,t);
			}
		}
	} //}}}

	//}}}

	//}}}

	//{{{ PrintTabExpander class
	static class PrintTabExpander implements TabExpander
	{
		private int leftMargin;
		private int tabSize;

		//{{{ PrintTabExpander constructor
		public PrintTabExpander(int leftMargin, int tabSize)
		{
			this.leftMargin = leftMargin;
			this.tabSize = tabSize;
		} //}}}

		//{{{ nextTabStop() method
		public float nextTabStop(float x, int tabOffset)
		{
			int ntabs = ((int)x - leftMargin) / tabSize;
			return (ntabs + 1) * tabSize + leftMargin;
		} //}}}
	} //}}}

	//{{{ TokenList class
	/**
	 * Encapsulates a token list.
	 * @since jEdit 4.0pre1
	 */
	public static class TokenList
	{
		//{{{ getFirstToken() method
		/**
		 * Returns the first syntax token.
		 * @since jEdit 4.0pre1
		 */
		public Token getFirstToken()
		{
			return firstToken;
		} //}}}

		//{{{ getLastToken() method
		/**
		 * Returns the last syntax token.
		 * @since jEdit 4.0pre1
		 */
		public Token getLastToken()
		{
			return lastToken;
		} //}}}

		//{{{ addToken() method
		/**
		 * Do not call this method. The only reason it is public
		 * is so that classes in the 'syntax' package can call it.
		 */
		public void addToken(int length, byte id, ParserRuleSet rules)
		{
			if(length == 0 && id != Token.END)
				return;

			if(firstToken == null)
			{
				firstToken = new Token(length,id,rules);
				lastToken = firstToken;
			}
			else if(lastToken == null)
			{
				lastToken = firstToken;
				firstToken.length = length;
				firstToken.id = id;
				firstToken.rules = rules;
			}
			else if(lastToken.id == id && lastToken.rules == rules)
			{
				lastToken.length += length;
			}
			else if(lastToken.next == null)
			{
				lastToken.next = new Token(length,id,rules);
				lastToken.next.prev = lastToken;
				lastToken = lastToken.next;
			}
			else
			{
				lastToken = lastToken.next;
				lastToken.length = length;
				lastToken.id = id;
				lastToken.rules = rules;
			}
		} //}}}

		private Token firstToken;
		private Token lastToken;
	} //}}}
}
