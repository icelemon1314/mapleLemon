package tools;

import java.io.Serializable;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class ConcurrentEnumMap<K extends Enum<K>, V> extends EnumMap<K, V> implements Serializable {

    private static final long serialVersionUID = 11920818021L;
    private final ReentrantReadWriteLock reentlock = new ReentrantReadWriteLock();
    private final Lock rL = this.reentlock.readLock();
    private final Lock wL = this.reentlock.writeLock();

    public ConcurrentEnumMap(Class<K> keyType) {
        super(keyType);
    }

    @Override
    public void clear() {
        this.wL.lock();
        try {
            super.clear();
        } finally {
            this.wL.unlock();
        }
    }

    @Override
    public EnumMap<K, V> clone() {
        return super.clone();
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public boolean containsKey(Object key) {
        this.rL.lock();
        try {
            boolean bool = super.containsKey(key);
            return bool;
        } finally {
            this.rL.unlock();
        }
    }

    @Override
    public boolean containsValue(Object value) {
        this.rL.lock();
        try {
            boolean bool = super.containsValue(value);
            return bool;
        } finally {
            this.rL.unlock();
        }
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        this.rL.lock();
        try {
            Set localSet = super.entrySet();
            return localSet;
        } finally {
            this.rL.unlock();
        }
    }

    @Override
    public V get(Object key) {
        this.rL.lock();
        try {
            return super.get(key);
        } finally {
            this.rL.unlock();
        }
    }

    @Override
    public Set<K> keySet() {
        this.rL.lock();
        try {
            return super.keySet();
        } finally {
            this.rL.unlock();
        }
    }

    @Override
    public V put(K key, V value) {
        this.wL.lock();
        try {
            return super.put(key, value);
        } finally {
            this.wL.unlock();
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        this.wL.lock();
        try {
            super.putAll(m);
        } finally {
            this.wL.unlock();
        }
    }

    @Override
    public V remove(Object key) {
        this.wL.lock();
        try {
            return super.remove(key);
        } finally {
            this.wL.unlock();
        }
    }

    @Override
    public int size() {
        this.rL.lock();
        try {
            return super.size();
        } finally {
            this.rL.unlock();
        }
    }

    @Override
    public Collection<V> values() {
        this.rL.lock();
        try {
            return super.values();
        } finally {
            this.rL.unlock();
        }
    }
}
