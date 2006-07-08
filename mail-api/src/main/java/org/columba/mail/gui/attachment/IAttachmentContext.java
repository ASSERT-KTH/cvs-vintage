package org.columba.mail.gui.attachment;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Provides context information for attachment.
 * 
 * @author frd
 */
public interface IAttachmentContext {

	/**
	 * Return attachment content.
	 * 
	 * @return	attachment content
	 * @throws  in case of io error
	 */
	public InputStream getContent() throws IOException;

	public String getFileName();

	public String getContentType();

	public String getContentSubtype();

	public Charset getCharset();

	public String getContentParameter(String key);

	public String getContentId();

	public String getContentDescription();

	public String getDispositionParameter(String key);

}
