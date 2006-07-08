package org.columba.mail.gui.message.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.columba.mail.gui.attachment.IAttachmentContext;
import org.columba.ristretto.message.MimeHeader;

public class AttachmentContext implements IAttachmentContext {

	private File file;

	private MimeHeader header;

	public AttachmentContext(final File file, final MimeHeader header) {
		this.file = file;
		this.header = header;
	}

	public InputStream getContent() throws IOException {
		return new BufferedInputStream(new FileInputStream(file));
	}

	public String getFileName() {
		return header.getFileName();
	}

	public String getContentType() {
		return header.getMimeType().getType();
	}

	public String getContentSubtype() {
		return header.getMimeType().getSubtype();
	}

	public Charset getCharset() {
		return header.getCharset();
	}

	public String getContentParameter(String key) {
		return header.getContentParameter(key);
	}

	public String getContentId() {
		return header.getContentID();
	}

	public String getContentDescription() {
		return header.getContentDescription();
	}

	public String getDispositionParameter(String key) {
		return header.getDispositionParameter(key);
	}

}
