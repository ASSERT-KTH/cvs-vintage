/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.timer;

import java.io.EOFException;
import java.io.File;
//AS import java.io.FileDescriptor;
//AS import java.io.FileInputStream;
import java.io.FileNotFoundException;
//AS import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
//AS import java.io.SyncFailedException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import javax.ejb.Timer;
import javax.ejb.TimerService;

import org.jboss.mx.util.SerializationHelper;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.server.ServerConfigLocator;

/**
 * This class provide a persistence support for timers on a file
 * bases.
 *
 * @jmx:mbean name="jboss:service=TimePersistenceManager,type=file"
 *            extends="org.jboss.system.ServiceMBean"
 *
 * @author <a href="mailto:andreas.schaefer@madplanet.com">Andreas Schaefer</a>
 **/
public class FilePersistenceManager
   extends ServiceMBeanSupport
   implements FilePersistenceManagerMBean
{
   private String mFileDirectory = "data";
   private File mDirectory;
   private HashMap mContainers = new HashMap();
   private HashMap mFiles = new HashMap();
   
   private int mFileCounter = 0;
   
   /**
    * Adds a new timer to the persistence store
    *
    * @param pContainerId Id of the container the timer belongs to
    * @param pId Timer Id
    * @param pKey Key of the EJB instance the Timer belongs to
    * @param pStartDate Date when the timer send an event initially
    * @param pInterval Interval of the next event in milliseconds or -1 if a
    *                  single time timer
    * @param pInfo User Info assigned with the Timer
    *
    * @throws IOException when making the timer persistent fails
    *
    * @jmx.managed-operation
    **/
   public void add(
      String pContainerId,
      Integer pId,
      Object pKey,
      Date pStartDate,
      long pInterval,
      Serializable pInfo
   )
      throws IOException
   {
      TimerItem lItem = new TimerItem(
         new ContainerTimerRepresentative( pId, pKey, pStartDate, pInterval, pInfo )
      );
      TreeMap lTimers = (TreeMap) mContainers.get( pContainerId );
      Object lTemp = mFiles.get( pContainerId );
      if( lTimers == null ) {
         lTimers = new TreeMap();
         mContainers.put( pContainerId, lTimers );
      }
      ObjectOutputStream lOutput = null;
      if( lTemp == null || lTemp instanceof Integer ) {
         int lFileNumber = 0;
         if( lTemp instanceof Integer ) {
            lFileNumber = ( (Integer) lTemp ).intValue();
         } else {
            lFileNumber = mFileCounter++;
         }
         try {
            File lNewFile = new File( mDirectory, "timers." + ( lFileNumber ) + ".save" );
            if( lNewFile.exists() ) {
               // Rename file to restart fresh and avoid carring around corrupt files
               boolean lRenamed = lNewFile.renameTo(
                  new File(
                     lNewFile.getParent(),
                     lNewFile.getName() + ".bak"
                  )
               );
               if( !lRenamed ) {
                  // Marked as error because this could lead to a problem
                  log.error( "-------------------> could not rename file: " + lNewFile );
               }
            }
            log.info( "Create Output file: " + mFileCounter + ", for container: " + pContainerId );
            lOutput = new MyObjectOutputStream(
               new MyOutputStream(
                  new RandomAccessFile( lNewFile, "rw" )
               )
            );
            lOutput.writeObject( pContainerId );
            mFiles.put( pContainerId, lOutput );
         }
         catch( FileNotFoundException fnfe ) {
            // Ignore safely because directory was tested when service started
         }
         catch( IOException ioe ) {
            // Ignore safely because directory was tested when service started
         }
         mFiles.put( pContainerId, lOutput );
      } else {
         lOutput = (ObjectOutputStream) lTemp;
      }
      lTimers.put( lItem.getId(), lItem );
      log.debug( "Write Timer Item to output file: " + lItem );
      lItem.writeItem( lOutput );
      // Flush to write everything into the file
      lOutput.flush();
   }
   
   /**
    * Removes an inactive timer from the persistence store
    *
    * @param pContainerId Id of the container the timer belongs to
    * @param pId Timer Id
    *
    * @throws IOException when making the timer persistent fails
    *
    * @jmx.managed-operation
    **/
   public void remove( String pContainerId, Integer pId )
      throws IOException
   {
      TimerItem lItem = new TimerItem( pId );
      TreeMap lTimers = (TreeMap) mContainers.get( pContainerId );
      ObjectOutputStream lOutput = (ObjectOutputStream) mFiles.get( pContainerId );
      log.debug( "remove(), container: " + pContainerId + ", id: " + pId
         + ", timer list: " + lTimers
         + ", output: " + lOutput
      );
      if( lTimers != null && lOutput != null ) {
          lTimers.remove( lItem.getId() );
          log.info( "Write remove timer to file: " + lItem );
          lItem.writeItem( lOutput );
          lOutput.flush();
      }
   }
   
   /**
    * Returns a list of Timer Representatives to be recreated by the called
    *
    * @param pContainerId Id of the container of which its timers has
    *                     to be restored and returned. If this Id is
    *                     null then all timers are restored and returned
    *
    * @return List of Timer Representatives to be restored or null if Container Id is null
    *
    * @jmx.managed-operation
    **/
   public List restore( String pContainerId, ContainerTimerService pTimerService ) {
      if( pContainerId != null ) {
         TreeMap lTimerMap = (TreeMap) mContainers.get( pContainerId );
         if( lTimerMap == null ) {
            // No Timers found -> nothing to restore
            log.debug( "No saved timers available for: " + pContainerId );
            return new ArrayList();
         }
         Collection lValues = (Collection) lTimerMap.values();
         TimerItem[] lTimers = (TimerItem[]) lValues.toArray( new TimerItem[] {} );
         // Restore timers
         List lReturn = new ArrayList( lTimers.length );
         for( int i = 0; i < lTimers.length; i++ ) {
            TimerItem lItem = lTimers[ i ];
            log.debug( "Restore timer: " + lItem );
            lReturn.add(
               lItem.getTimerRep()
            );
         }
         return lReturn;
      }
      return null;
   }
   
   protected void createService()
      throws Exception
   {
      mDirectory = null;
      // First check if the given Data Directory is a valid URL pointing to
      // a readable and writable directory
      try {
         URL lFileURL = new URL( mFileDirectory );
         File lFile = new File( lFileURL.getFile() );
         if( lFile.isDirectory() && lFile.canRead() && lFile.canWrite() ) {
            mDirectory = lFile;
            if( log.isDebugEnabled() ) {
               log.debug( "Using data directory: " + mDirectory );
            }
         }
      }
      catch( Exception e ) {
         // Ignore message and try it as relative path
      }
      // If directory is not defined use the a default directory and create it if
      // necessary
      if( mDirectory == null ) {
         // Get the system home directory
         File lSystemHome = ServerConfigLocator.locate().getServerHomeDir();
         
         mDirectory = new File( lSystemHome, mFileDirectory );
         log.info( "Using data directory: " + mDirectory );
         if( log.isDebugEnabled() ) {
            log.debug( "Using data directory: " + mDirectory );
         }
         
         mDirectory.mkdirs();
         if( !mDirectory.isDirectory() ) {
            throw new RuntimeException( "The data directory is not valid: " + mDirectory );
         }
      }
   }
   
   protected void startService()
      throws Exception
   {
      // Open the files and load the content and keep it
      // available until the timers are restored
      File[] lContainerFiles = mDirectory.listFiles();
      for( int i = 0; i < lContainerFiles.length; i++ ) {
         String lFileName = lContainerFiles[ i ].getName();
         if( lFileName.startsWith( "timers." ) && lFileName.endsWith( ".save" ) ) {
            TreeMap lTimers = new TreeMap();
            int lFileCounter = new Integer( lFileName.substring( 7, lFileName.length() - 5 ) ).intValue();
            mFileCounter = lFileCounter > mFileCounter ? lFileCounter : mFileCounter;
            log.info( "Open backup file: " + lContainerFiles[ i ] );
            RandomAccessFile lFileAccess = new RandomAccessFile( lContainerFiles[ i ], "rw" );
            ObjectInputStream lInput = new MyObjectInputStream(
               new MyInputStream( lFileAccess )
            );
            // First read container id
            String lContainerId = (String) lInput.readObject();
            log.info( "Load saved timers for container: " + lContainerId );
            TimerItem lItem = TimerItem.readItem( lInput );
            while( lItem != null ) {
                log.info( "Got saved timer item: " + lItem );
                if( lItem.getType() == TimerItem.REMOVE ) {
                    lTimers.remove( lItem.getId() );
                } else {
                    lTimers.put( lItem.getId(), lItem );
                }
                lItem = TimerItem.readItem( lInput );
            }
            lInput.close();
            mContainers.put( lContainerId, lTimers );
            mFiles.put( lContainerId, new Integer( lFileCounter ) );
         }
      }
   }
   
   public static class TimerItem {
      
      public static final int ADD = 0;
      public static final int REMOVE = 1;
      
      private int mType;
      private Integer mId;
      private ContainerTimerRepresentative mTimerRep;
      
      public TimerItem( ContainerTimerRepresentative pTimerRep ) {
         mType = ADD;
         mId = pTimerRep.getId();
         mTimerRep = pTimerRep;
      }
      
      public TimerItem( Integer pId ) {
         mId = pId;
         mType = REMOVE;
      }
      
      public int getType() {
         return mType;
      }
      
      public Integer getId() {
         return mId;
      }
      
      public ContainerTimerRepresentative getTimerRep() {
         return mTimerRep;
      }
      
      
      public String toString() {
         return "FilePersistenceManager.TimerItem [ "
            + ", type: " + ( mType == ADD ? "ADD" : "REMOVE" )
            + ", timer rep: " + mTimerRep
            + " ]";
      }
      
      public static TimerItem readItem( ObjectInputStream pInput )
         throws IOException
      {
         try {
            int lType = pInput.readInt();
            Integer lId = (Integer) pInput.readObject();
            if( lType == ADD ) {
               byte[] lKey = (byte[]) pInput.readObject();
               Date lDate = new Date( pInput.readLong() );
               long lInterval = pInput.readLong();
               byte[] lInfo = (byte[]) pInput.readObject();
               return new TimerItem(
                  new ContainerTimerRepresentative(
                     lId, lKey, lDate, lInterval, lInfo
                  )
               );
            } else {
               return new TimerItem( lId );
            }
         }
         catch( ClassNotFoundException cnfe ) {
            // Ignore safely because there are no unknow classes
            return null;
         }
         catch( EOFException ee ) {
            // End of file means no more saved timers -> return null
            return null;
         }
      }
      
      public void writeItem( ObjectOutputStream pOutput )
         throws IOException
      {
         pOutput.writeInt( mType );
         pOutput.writeObject( mId );
         if( mType == ADD ) {
             byte[] lKeyBytes = SerializationHelper.serialize( mTimerRep.getKey() );
             pOutput.writeObject( lKeyBytes );
             pOutput.writeLong( mTimerRep.getStartDate().getTime() );
             pOutput.writeLong( mTimerRep.getInterval() );
             byte[] lInfoBytes = SerializationHelper.serialize( mTimerRep.getInfo() );
             pOutput.writeObject( lInfoBytes);
         }
      }
   }
   
   /**
    * @created    August 16, 2001
    */
   class MyOutputStream extends OutputStream {
      private RandomAccessFile mFile;
      
      public MyOutputStream( RandomAccessFile pFile ) {
         mFile = pFile;
      }
      
      public void flush() {
         try {
            mFile.getFD().sync();
         }
         catch( IOException ioe ) {
            // Ignore it
         }
      }
      
      public void close()
         throws IOException {
         flush();
      }

      public void write( int b )
         throws IOException
      {
         mFile.write( ( byte )b );
      }

      public void write( byte bytes[], int off, int len )
         throws IOException
      {
         mFile.write( bytes, off, len );
      }
   }

   /**
    * @created    August 16, 2001
    */
   class MyObjectOutputStream extends ObjectOutputStream {
      MyObjectOutputStream( OutputStream os )
         throws IOException
      {
         super( os );
      }
      
      protected void writeStreamHeader() {
      }
   }
   
   /**
    * @created    August 16, 2001
    */
   class MyObjectInputStream extends ObjectInputStream {
      MyObjectInputStream( InputStream is )
         throws IOException
      {
         super( is );
      }
      
      protected void readStreamHeader() {
      }
   }
   
   /**
    * @created    August 16, 2001
    */
   class MyInputStream extends InputStream {
      private RandomAccessFile mFile;
      
      public MyInputStream( RandomAccessFile pFile ) {
         mFile = pFile;
      }
      
      public void close()
         throws IOException
      {
         mFile.close();
      }
      
      public int read()
         throws IOException
      {
         return mFile.read();
      }
      
      public int read( byte bytes[], int off, int len )
         throws IOException
      {
         return mFile.read( bytes, off, len );
      }
   }
}