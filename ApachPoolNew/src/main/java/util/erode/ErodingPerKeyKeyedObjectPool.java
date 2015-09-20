package util.erode;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import bean.inter.pool.KeyedObjectPool;

public class ErodingPerKeyKeyedObjectPool<K, V> extends ErodingKeyedObjectPool<K, V> {
    /** Erosion factor - same for all pools */
    private final float factor;

    /** Map of ErodingFactor instances keyed on pool keys */
    private final Map<K, ErodingFactor> factors = Collections.synchronizedMap(new HashMap<K, ErodingFactor>());

    /**
     * Create a new ErordingPerKeyKeyedObjectPool decorating the given keyed pool with the specified erosion factor.
     * 
     * @param keyedPool
     *            underlying keyed pool
     * @param factor
     *            erosion factor
     */
    public ErodingPerKeyKeyedObjectPool(final KeyedObjectPool<K, V> keyedPool, final float factor) {
        super(keyedPool, null);
        this.factor = factor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ErodingFactor getErodingFactor(final K key) {
        ErodingFactor eFactor = factors.get(key);
        // this may result in two ErodingFactors being created for a key
        // since they are small and cheap this is okay.
        if (eFactor == null) {
            eFactor = new ErodingFactor(this.factor);
            factors.put(key, eFactor);
        }
        return eFactor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "ErodingPerKeyKeyedObjectPool{" + "factor=" + factor + ", keyedPool=" + getKeyedPool() + '}';
    }
}