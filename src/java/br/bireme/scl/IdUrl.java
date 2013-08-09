package br.bireme.scl;

/**
 *
 * @author Heitor Barbieri
 * date: 20130730
 */
public class IdUrl implements Comparable<IdUrl> {        
    public final String id;  // LILACS & Mongo
    public final String url; // broken

    public IdUrl(final String id, 
                 final String url) {
        this.id = id;
        this.url = url;
    }             

    @Override
    public int compareTo(IdUrl other) {
        return url.compareTo(other.url);
    }
}
