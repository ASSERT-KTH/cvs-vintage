package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jdt.core.*;

import java.io.*;
import java.util.Enumeration;

/**
 * The BufferManager implements an LRU cache of buffers.
 *
 * @see IBufferManager
 */
public class BufferManager implements IBufferManager {

	/**
	 * LRU cache of buffers. The key and value for an entry
	 * in the table is the identical buffer.
	 */
	protected OverflowingLRUCache fOpenBuffers = new BufferCache(20);
	protected static BufferManager fgDefaultBufferManager;
/**
 * Creates a new buffer manager.
 */
public BufferManager() {
}
/**
 * Adds a buffer to the table of open buffers.
 */
protected void addBuffer(IBuffer buffer) {
	fOpenBuffers.put(buffer.getOwner(), buffer);
}
/**
 * Returns the given bytes as a char array.
 */
public static char[] bytesToChar(byte[] bytes) throws JavaModelException {

	return getInputStreamAsCharArray(new ByteArrayInputStream(bytes));

}
/**
 * @see IBufferManager
 */
public IBuffer getBuffer(IOpenable owner) {
	return (IBuffer)fOpenBuffers.get(owner);
}
/**
 * Returns the default buffer manager.
 * TBD: There shouldn't be a global buffer manager.
 * It should be a registered manager with the workspace.
 */
public synchronized static IBufferManager getDefaultBufferManager() {
	if (fgDefaultBufferManager == null) {
		fgDefaultBufferManager = new BufferManager();
	}
	return fgDefaultBufferManager;
}
/**
 * Returns the given input stream's contents as a character array.
 */
protected static char[] getInputStreamAsCharArray(InputStream stream) throws JavaModelException {
	InputStreamReader reader= null;
	reader= new InputStreamReader(stream);
	char[] contents = new char[0];
	char[] grow;
	try {
		int available= stream.available();	
		int charsRead= 0;
		int pos = 0;
		while (available > 0) {
			grow = new char[contents.length + available];
			System.arraycopy(contents, 0, grow, 0, contents.length);
			contents = grow;
			charsRead= reader.read(contents, pos, available);
			available= stream.available();
		} 
		if (charsRead < available && charsRead > 0) {
			grow = new char[contents.length - (available - charsRead)];
			System.arraycopy(contents, 0, grow, 0, grow.length);
			contents= grow;
		}
	} catch (IOException e) {
		throw new JavaModelException(e, IJavaModelStatusConstants.IO_EXCEPTION);
	} finally {
		try {
			reader.close();
		} catch (IOException ioe) {
			throw new JavaModelException(ioe, IJavaModelStatusConstants.IO_EXCEPTION);
		}
	}
	return contents;
}
/**
 * The <code>Enumeration</code> answered is thread safe.
 *
 * @see OverflowLRUCache
 * @see IBufferManager
 */
public Enumeration getOpenBuffers() {
	synchronized (fOpenBuffers) {
		fOpenBuffers.shrink();
		return fOpenBuffers.elements();
	}
}
/**
 * Returns the given file's contents as a byte array.
 */
public static byte[] getResourceContentsAsBytes(IFile file) throws JavaModelException {
	InputStream stream= null;
	try {
		stream = file.getContents(true);
	} catch (CoreException e) {
		throw new JavaModelException(e);
	}
	byte[] contents = new byte[0];
	byte[] grow;
	try {
		int available = stream.available();
		int pos = 0;
		while (available > 0) {
			grow = new byte[contents.length + available];
			System.arraycopy(contents, 0, grow, 0, contents.length);
			contents = grow;
			stream.read(contents, pos, available);
			available = stream.available();
		}
	} catch (IOException e) {
		throw new JavaModelException(e, IJavaModelStatusConstants.IO_EXCEPTION);
	} finally {
		try {
			stream.close();
		} catch (IOException ioe) {
			throw new JavaModelException(ioe, IJavaModelStatusConstants.IO_EXCEPTION);
		}
	}
	return contents;
}
/**
 * Returns the given file's contents as a character array.
 */
public static char[] getResourceContentsAsCharArray(IFile file) throws JavaModelException {
	InputStream stream= null;
	try {
		stream = file.getContents(true);
	} catch (CoreException e) {
		throw new JavaModelException(e);
	}
	return getInputStreamAsCharArray(stream);
}
/**
 * @see IBufferManager
 */
public IBuffer openBuffer(char[] contents, IProgressMonitor progress, IOpenable owner, boolean readOnly) throws IllegalArgumentException {
	if (contents == null || owner == null) {
		throw new IllegalArgumentException();
	}
	Buffer buffer = new Buffer(this, contents, owner, readOnly);
	addBuffer(buffer);
	return buffer;
}
/**
 * @see IBufferManager
 */
public IBuffer openBuffer(IFile file, IProgressMonitor progress, IOpenable owner, boolean readOnly) throws JavaModelException {
	if (file == null || owner == null) {
		throw new IllegalArgumentException();
	}
	char[] contents = getResourceContentsAsCharArray(file);
	Buffer buffer = new Buffer(this, file, contents, owner, readOnly);
	addBuffer(buffer);
	return buffer;
}
/**
 * Removes a buffer from the table of open buffers.
 */
protected void removeBuffer(IBuffer buffer) {
	fOpenBuffers.remove(buffer.getOwner());
}
/**
 * Returns the given String as a byte array. This is centralized here in case
 * we need to do special conversion.
 */
public static byte[] stringToBytes(String s) {

	return s.getBytes();
	
}
}
