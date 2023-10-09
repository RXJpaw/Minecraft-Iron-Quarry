package pw.rxj.iron_quarry.util;

import pw.rxj.iron_quarry.records.IndexedValue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class IndexedMap<K, V> {
    private final LinkedHashMap<K, IndexedValue<V>> hashMap = new LinkedHashMap<>();
    private int nextIndex = 0;

    public IndexedMap() { }

    public void put(K key, V value){
        hashMap.put(key, new IndexedValue<>(nextIndex, value));
        nextIndex++;
    }

    public IndexedValue<V> get(K key){
        return hashMap.get(key);
    }
    public int getIndex(K key){
        return hashMap.get(key).index();
    }
    public V getValue(K key){
        return hashMap.get(key).value();
    }

    public List<K> keys(){
        return new ArrayList<>(hashMap.keySet());
    }
    public int size(){
        return hashMap.size();
    }
}
