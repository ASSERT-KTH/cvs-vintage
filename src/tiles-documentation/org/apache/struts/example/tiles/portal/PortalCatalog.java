package org.apache.struts.example.tiles.portal;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;


/**
 * A catalog of available tiles for a portal.
 * Tiles denote a local URL or a Tile definition name.
 * To check : should be possible to put ComponentDefinition class also.
 *
 * @author Cedric Dumoulin
 */
public class PortalCatalog
{
       /** List of available Tiles */
     protected List tiles = new ArrayList();
       /** List of Tiles labels */
     protected List tileLabels = new ArrayList();

       /**
        * Set list of tiles.
        * Labels come from tiles names
        * @param list list of tiles
        */
     public void setTiles( List list)
       {
       setTiles(list, list);
       }

       /**
        * add list to list of available Tiles
        * Labels come from tiles names
        * @param list list of tiles
        */
     public void addTiles( List list)
       {
       addTiles( list, list);
       }

       /**
        * Set list of available Tiles.
        * Previous list is disguarded.
        * @param list list of tiles
        * @param labels corresponding labels. List size must be the same as list.
        * If labels is null, use list of tiles.
        * @throws ArrayIndexOutOfBoundsException if list and labels aren't the same size.
        */
     public void setTiles( List list, List labels)
         throws ArrayIndexOutOfBoundsException
       {
         // If no labels, use list keys
       if( labels == null )
         labels = list;
         // Check sizes
       if( list.size() != labels.size() )
         {// error
         System.out.println( "Error : list and labels size must be the same." );
         throw new java.lang.ArrayIndexOutOfBoundsException( "List of tiles and list of labels must be of the same size" );
         }
       this.tiles = list;
       tileLabels = labels;
       }

       /**
        * add list and labels to list of available Tiles.
        * If labels is null, use keys list as labels.
        * @list list of choice keys to add
        * @param labels corresponding labels. List size must be the same as list.
        * If labels is null, use list of tiles.
        * @throws ArrayIndexOutOfBoundsException if list and labels aren't the same size.
        */
     public void addTiles( List list, List labels)
         throws ArrayIndexOutOfBoundsException
       {
         // If no labels, use list keys
       if( labels == null )
         labels = list;
         // Check sizes
        if(tiles== null)
         {
         setTiles(list, labels);
         return;
         }

       if( list.size() != labels.size() )
         {// error
         System.out.println( "Error : list and labels size must be the same." );
         throw new java.lang.ArrayIndexOutOfBoundsException( "List of tiles and list of labels must be of the same size" );
         }
       tiles.addAll(list);
       tileLabels.addAll(labels);
       }

       /**
        * Get list of available Tiles
        */
     public List getTiles( )
       {
       return tiles;
       }

       /**
        * Get list of labels for Tiles
        */
     public List getTilesLabels( )
       {
       return tileLabels;
       }

       /**
        * Get label for specified Tile, identified by its key.
        * @param key Tile key
        */
     public String getTileLabel( Object key )
       {
       int index = tiles.indexOf( key );
       if(index==-1)
         return null;
       return (String)tileLabels.get(index);
       }

       /**
        * Get list of labels for Tile keys
        * @param keys List of keys to search for labels.
        */
     public List getTileLabels( List Keys )
       {
       List listLabels = new ArrayList();

       Iterator i = Keys.iterator();
       while(i.hasNext())
         {
         Object key = i.next();
         listLabels.add( getTileLabel(key) );
         } // end loop
       return listLabels;
       }

       /**
        * Get Tiles corresponding to keys.
        * Keys are the one returned by the setting page. Keys are usually issue
        * from values returned by getTiles().
        * If a key isn't recognize, it is disguarded from the returned list.
        * If a key correspond to a special key, appropriate 'definition' is created.
        * Returned list contains tiles URL, definition name and definitions suitable
        * as attribute of <tiles:insert >.
        *
        * @keys array of keys to add to list.
        */
     public List getTiles( String keys[] )
       {
       List list = new ArrayList();

         // add keys to list
       for(int i=0;i<keys.length;i++)
         {
         String key = keys[i];
         if( key.indexOf( '@' )>0 )
           { // special key
           }
         if( tiles.contains( key ) )
           { // ok, add it
           list.add( key );
           }
         } // end loop
       return list;
       }

       /**
        * Set labels for tiles Tiles.
        */
     protected void setTileLabels( List list)
       {
       this.tileLabels = list;
       }
       /**
        * add list to list of tiles Tiles
        */
     protected void addTileLabels( List list)
       {
       if(tileLabels== null)
         {
         setTileLabels(list);
         return;
         }
       tileLabels.addAll(list);
       }

}