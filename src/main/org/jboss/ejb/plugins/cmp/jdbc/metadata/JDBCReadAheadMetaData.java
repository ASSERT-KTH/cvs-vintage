/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc.metadata;

import java.util.Arrays;
import java.util.List;

import org.jboss.deployment.DeploymentException;
import org.jboss.metadata.MetaData;

import org.w3c.dom.Element;

/**
 * Imutable class which holds all the information about read-ahead settings.
 * It loads its data from standardjbosscmp-jdbc.xml and jbosscmp-jdbc.xml
 *
 * @author <a href="mailto:on@ibis.odessa.ua">Oleg Nitz</a>
 * @version $Revision: 1.4 $
 */
public final class JDBCReadAheadMetaData {

   public static final JDBCReadAheadMetaData DEFAULT = new JDBCReadAheadMetaData();

   /*
    * Constants for read ahead strategy
    */
   /**
    * Don't read ahead.
    */
   public static final byte NONE = 0;

   /**
    * Read ahead when some entity is being loaded (lazily, good for all queries).
    */
   public static final byte ON_LOAD = 1;

   /**
    * Read ahead during "find" (not lazily, the best for queries with small result set).
    */
   public static final byte ON_FIND = 2;


   private static final List STRATEGIES = Arrays.asList(new String[] {"none", "on-load", "on-find"});

   public static final byte DEFAULT_STRATEGY = ON_LOAD;

   public static final int DEFAULT_LIMIT = 255;

   public static final int DEFAULT_CACHE_SIZE = 100;

   /**
    * The strategy of reading ahead, one of {@link #NONE}, {@link #ON_LOAD}, {@link #ON_FIND}.
    */
   private final byte strategy;

   /**
    * The limit of the read ahead buffer
    */
   private final int limit;

   /**
    * The size of the cache of queries
    */
   private final int cacheSize;

   /**
    * Constructs default read ahead meta data: no read ahead.
    */
   private JDBCReadAheadMetaData() {
      strategy = DEFAULT_STRATEGY;
      limit = DEFAULT_LIMIT;
      cacheSize = DEFAULT_CACHE_SIZE;
   }

   /**
    * Constructs read ahead meta data with the data contained in the cmp-field xml
    * element from a jbosscmp-jdbc xml file. Optional values of the xml element that
    * are not present are instead loaded from the defalutValues parameter.
    *
    * @param element the xml Element which contains the metadata about this field
    * @param defaultValues the JDBCCMPFieldMetaData which contains the values
    * for optional elements of the element
    * @throws DeploymentException if data in the entity is inconsistent with field type
    */
   public JDBCReadAheadMetaData(Element element) throws DeploymentException {
      // "true"/"false" content is JAWS style, we support it.
      String trueOrFalse = MetaData.getElementContent(element);
      if (trueOrFalse.equals("true")) {
         strategy = DEFAULT_STRATEGY;
         limit = DEFAULT_LIMIT;
         cacheSize = DEFAULT_CACHE_SIZE;
      } else if (trueOrFalse.equals("false")) {
         strategy = NONE;
         limit = DEFAULT_LIMIT;
         cacheSize = DEFAULT_CACHE_SIZE;
      } else {
         // This is new style: strategy and limit sub-elements

         // Strategy
         String strategyStr = MetaData.getOptionalChildContent(element, "strategy");
         if (strategyStr != null) {
            strategy = (byte) STRATEGIES.indexOf(strategyStr);
            if (strategy < 0) {
               throw new DeploymentException("Unknown read ahead strategy '" + strategyStr + "'.");
            }
         } else {
            strategy = DEFAULT_STRATEGY;
         }

         // Limit
         String limitStr = MetaData.getOptionalChildContent(element, "limit");
         if (limitStr != null) {
            try {
               limit = Integer.parseInt(limitStr);
            } catch (NumberFormatException ex) {
               throw new DeploymentException("Wrong number format of read ahead limit '" + limitStr + "': " + ex);
            }
            if (limit < 0) {
               throw new DeploymentException("Negative value for read ahead limit '" + limitStr + "'.");
            }
         } else {
            limit = DEFAULT_LIMIT;
         }

         // Size of the cache of queries
         String cacheSizeStr = MetaData.getOptionalChildContent(element, "cache-size");
         if (cacheSizeStr != null) {
            try {
               cacheSize = Integer.parseInt(cacheSizeStr);
            } catch (NumberFormatException ex) {
               throw new DeploymentException("Wrong number format of read ahead cache size '" + cacheSizeStr + "': " + ex);
            }
            if (cacheSize < 2) {
               throw new DeploymentException("The ahead cache size is '" + cacheSizeStr + "', should be >= 2.");
            }
         } else {
            cacheSize = DEFAULT_CACHE_SIZE;
         }
      }
   }

   /**
    * Convenience method, tells whether read ahead is used (i.e. whether the strategy is not NONE).
    */
   public boolean isUsed() {
      return (strategy != NONE);
   }

   /**
    * Convenience method, tells whether read ahead on load is used (i.e. whether the strategy is not ON_LOAD).
    */
   public boolean isOnLoadUsed() {
      return (strategy == ON_LOAD);
   }

   /**
    * @returns Read ahead strategy, one of {@link #NONE}, {@link #ON_LOAD}, {@link #ON_FIND}.
    */
   public byte getStrategy() {
      return strategy;
   }

   /**
    * @returns Limit for read ahead buffer, 0 means "infinite".
    */
   public int getLimit() {
      return limit;
   }

   /**
    * @returns Size of the cache of queries.
    */
   public int getCacheSize() {
      return cacheSize;
   }

   /**
    * Returns a string describing this JDBCReadAheadMetaData.
    * @return a string representation of the object
    */
   public String toString() {
      return "[JDBCReadAheadMetaData : strategy=" + STRATEGIES.get(strategy) + ", limit=" + limit + "]";
   }
}
