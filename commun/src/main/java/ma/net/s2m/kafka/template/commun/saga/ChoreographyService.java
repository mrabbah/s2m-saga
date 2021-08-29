package ma.net.s2m.kafka.template.commun.saga;

/**
 *
 * @author rabbah
 */
public interface ChoreographyService<T, V> {
    
    public V proceed(T request);
    public boolean completed(T request);
    public boolean failed(T request);
    
}
