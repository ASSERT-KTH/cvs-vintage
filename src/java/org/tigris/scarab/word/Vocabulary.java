package org.tigris.scarab.word;

// JDK classes
import java.util.*;

// Turbine classes
import org.apache.turbine.util.db.Criteria;

import org.tigris.scarab.om.*;

/**
  * This class handles vocabulary information for a single issue
  *
  */
public class Vocabulary
{
    /**
     *  this class represents a vocabulary entry
     *
     */

    class Entry
    {
        private String word;
        private int count;
        private int firstPos;
        private int rating=0;
        private boolean isNew=true;

        public Entry(String word, int firstPos)
        {
            this.word = word;
            this.firstPos = firstPos;
            this.count = 1;
        }

        public void inc()
        {
            this.count++;
        }

        public void setRating(int rating)
        {
            this.rating = rating;
        }

        public void setNew(boolean isNew)
        {
            this.isNew = isNew;
        }
    }

    private static HashSet ignoredWords;
    static
    {
        Criteria crit = new Criteria().add(WordPeer.IGNORE, 1);
        ignoredWords = new HashSet();
        try
        {
            Vector v = WordPeer.doSelect(crit);
            for(int i=0; i<v.size(); i++)
                ignoredWords.add(((Word)v.get(i)).getWord());
        }
        catch (Exception e)
        {
            //anything better than just swallowing the exception?
        }
    }

    private Hashtable words;
    private int pos = 1;

    public Vocabulary() throws Exception
    {
        this("");
    }

    public Vocabulary(String text) throws Exception
    {
        words = new Hashtable(1+text.length()/5); // should be a good guess?
        add(text);
    }

    /**
     *  used to determine whether a word should not be indexed
     *  should return true for very frequently used word or non-words
     *  or in other cases when apropriate
     *
     */

    protected boolean ignore(String word)
    {
        /* for now only ignore words in the ignore list.
            should _probably_ also ignore non-alpha words i.e. "3.1415" "&*%$#^" etc... */
        return ignoredWords.contains(word);
    }

    /**
     *  indexes more text. this is incremental.
     *
     */

    public void add(String text) throws Exception
    {
        StringTokenizer st = new StringTokenizer(text);
        String tok;
        HashSet newEntries = new HashSet();
        while (st.hasMoreTokens())
        {
            tok = st.nextToken();
            if (!ignore(tok))
            {
                Entry entry = (Entry)words.get(tok);
                if (entry==null)
                {
                    entry = new Entry(tok, pos);
                    words.put(tok, entry);
                    newEntries.add(tok);
                }
                else
                    entry.inc();
            }
            pos++;
        }
        if (!newEntries.isEmpty())
        {
            Criteria crit = new Criteria()
                .addIn(WordPeer.WORD, newEntries.toArray());
            Vector v = WordPeer.doSelect(crit);
            for (int i=0; i<v.size(); i++)
            {
                Word word = (Word)v.get(i);
                Entry entry = (Entry)words.get(word.getWord());
                entry.setRating(word.getRating());
                entry.setNew(false);
            }
        }
    }
}



