package com.pressassociation.maven.wmb.configure;

import com.google.common.base.Function;
import com.google.common.collect.*;

import javax.annotation.Nullable;
import java.util.*;

/**
 * @author Bob Browning <bob.browning@pressassociation.com>
 */
public class TypeSafetyHelper {

    public static <T> List<T> typeSafeList(List<?> list, final Class<T> klazz) {
        return Lists.transform(list, new Function<Object, T>() {
            @Override
            public T apply(@Nullable Object o) {
                return klazz.cast(o);
            }
        });
    }

    public static <T> Set<T> typeSafeSet(Set<?> set, final Class<T> klazz) {
        return Sets.newHashSet(typeSafeCollection(set, klazz));
    }

    public static <T> Collection<T> typeSafeCollection(Collection<?> collection, final Class<T> klazz) {
        return Collections2.transform(collection, new Function<Object, T>() {
            @Override
            public T apply(@Nullable Object o) {
                return klazz.cast(o);
            }
        });
    }

    public static <T> Iterator<T> typeSafeCaptureOfIterator(final Enumeration<? extends T> enumeration) {
        return new UnmodifiableIterator<T>() {
            @Override
            public boolean hasNext() {
                return enumeration.hasMoreElements();
            }

            @Override
            public T next() {
                return enumeration.nextElement();
            }
        };
    }

    public static <T> Iterable<T> typeSafeCaptureOfIterable(final Enumeration<? extends T> enumeration) {
        return new FluentIterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return typeSafeCaptureOfIterator(enumeration);
            }
        };
    }
}
