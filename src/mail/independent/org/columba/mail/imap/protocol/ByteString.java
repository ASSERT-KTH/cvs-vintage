package org.columba.mail.imap.protocol;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ByteString {

	byte[] bytes;

	public ByteString(String str) {
		this.bytes = convertToBytes(str);
	}
	/**
	 * Constructor for ByteString.
	 */
	public ByteString(byte[] bytes) {
		this.bytes = bytes;
	}

	public byte[] getBytes() {
		return bytes;
	}

	public byte[] convertToBytes(String s) {
		char[] chars = s.toCharArray();
		int size = chars.length;
		byte[] bytes = new byte[size];

		for (int i = 0; i < size;)
			bytes[i] = (byte) chars[i++];
		return bytes;
	}

}
