package org.columba.mail.message;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public abstract class AbstractMessage {

	protected String source=null;
	protected MimePartTree mimePartCollection;
	
	
	protected MimePart bodyPart;
	
	public AbstractMessage()
	{}
	
	public abstract HeaderInterface getHeader();
	public abstract void setHeader( HeaderInterface h );
	
	
	public String getSource()
	{
		return source;
	}
	
	public void setSource( String s )
	{
		source = s;
	}
	
	public void setBodyPart(MimePart body) {
		bodyPart = body;
	}

	public MimePart getBodyPart() {
		return bodyPart;
	}
	
	public int getSize() {
		Integer sizeInt = (Integer) getHeader().get("columba.size");

		return sizeInt.intValue();
	}

	public Flags getFlags() {
		return ((ColumbaHeader)getHeader()).getFlags();
	}

	public void setUID(Object o) {
		if (o != null)
			getHeader().set("columba.uid", o);
		else
			getHeader().set("columba.uid", new String(""));

		//uid = o;
	}

	public Object getUID() {
		return getHeader().get("columba.uid");
	}

	public MimePart getMimePart(int number) {
		return mimePartCollection.get(number);

	}

	public int getMimePartCount() {
		if( mimePartCollection != null )
		return mimePartCollection.count();
		else return 0;
	}

	public MimePartTree getMimePartTree() {
		return mimePartCollection;
	}

	public void setMimePartTree(MimePartTree ac) {
		mimePartCollection = ac;
	}
	
	public void freeMemory()
	{
		source=null;
	}
}
