package nstic.util;

public class DualKey {
    private final String key1;
    private final String key2;

    public DualKey(String key1, String key2) {
        this.key1 = key1;
        this.key2 = key2;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DualKey))
            return false;
        DualKey key = (DualKey) o;
        return key1.equals(key.key1) && key2.equals(key.key2);
    }

    @Override
    public int hashCode() {
        int result = key1.length();
        result = 31 * result + key2.length();
        return result;
    }
}
