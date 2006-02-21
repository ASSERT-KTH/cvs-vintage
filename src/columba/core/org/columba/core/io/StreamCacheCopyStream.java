package org.columba.core.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class StreamCacheCopyStream extends PassiveCopyStream {

	StreamCache cache;

	Object key;

	File fileOut;

	/**
	 * @param in
	 * @param key
	 * @param out
	 * @param cache
	 * @throws FileNotFoundException
	 */
	public StreamCacheCopyStream(InputStream in, Object key, File out,
			StreamCache cache) throws FileNotFoundException {
		super(in, new FileOutputStream(out));

		this.key = key;
		this.cache = cache;
		this.fileOut = out;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws IOException {
		super.close();

		cache.add(key, fileOut);
	}

}
