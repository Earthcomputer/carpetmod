package carpet.utils;

import javax.annotation.Nullable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;

/**
 * @author Earthcomputer
 */
public class HashUtils
{

    // PRIVATE UTILITIES

    private static final Field MAP_FIELD;
    private static final Field TABLE_FIELD;
    static
    {
        try
        {
            MAP_FIELD = HashSet.class.getDeclaredField("map");
            MAP_FIELD.setAccessible(true);
            TABLE_FIELD = HashMap.class.getDeclaredField("table");
            TABLE_FIELD.setAccessible(true);
        }
        catch (Exception e)
        {
            throw new AssertionError(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <E> HashMap<E, Object> getMap(HashSet<E> set)
    {
        try
        {
            return (HashMap<E, Object>) MAP_FIELD.get(set);
        }
        catch (Exception e)
        {
            throw new AssertionError(e);
        }
    }

    @Nullable
    private static <K, V> Object getTable(HashMap<K, V> map)
    {
        try
        {
            return TABLE_FIELD.get(map);
        }
        catch (Exception e)
        {
            throw new AssertionError(e);
        }
    }

    // HASHMAP UTILITIES

    public static <K, V> int hashSize(HashMap<K, V> map)
    {
        Object table = getTable(map);
        return table == null ? 0 : Array.getLength(table);
    }

    public static int hash(@Nullable Object obj)
    {
        int h;
        return obj == null ? 0 : (h = obj.hashCode()) | (h >>> 16);
    }

    public static <K, V> int getBucketNumber(HashMap<K, V> map, @Nullable K obj)
    {
        return hash(obj) & (hashSize(map) - 1);
    }

    // HASHSET UTILITIES

    public static <E> int hashSize(HashSet<E> set)
    {
        return hashSize(getMap(set));
    }

    public static <E> int getBucketNumber(HashSet<E> set, @Nullable E obj)
    {
        return getBucketNumber(getMap(set), obj);
    }

}
