package nstic.util;

import java.util.concurrent.ConcurrentHashMap;

public class DualKeyConcurrentHashMap<K, V> {
    private final ConcurrentHashMap<K, V> map = new ConcurrentHashMap<>();

    public V put(K key, V value) {

        return map.put(key, value);
    }

    public V get(DualKey k) {
        return map.get(k);
    }

    public V remove(DualKey k) {
        return map.remove(k);
    }

    public boolean containsKey(DualKey k) {
        return map.containsKey(k);
    }

    public void clear() {
        map.clear();
    }
}
