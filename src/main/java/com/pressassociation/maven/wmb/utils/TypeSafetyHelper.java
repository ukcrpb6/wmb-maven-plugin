package com.pressassociation.maven.wmb.utils;

import com.google.common.base.Function;
import com.google.common.collect.*;

import javax.annotation.Nullable;
import java.util.*;

/**
 * @author Bob Browning <bob.browning@pressassociation.com>
 */
public class TypeSafetyHelper {

    public static <T> Function<Object, T> typeSafeCast(final Class<T> klass) {
        return new Function<Object, T>() {
            @Override
            public T apply(@Nullable Object input) {
                return klass.cast(input);
            }
        };
    }

    public static <T> List<T> typeSafeList(List<?> list, Class<T> klazz) {
        return Lists.transform(list, typeSafeCast(klazz));
    }

    public static <T> Set<T> typeSafeSet(Set<?> set, Class<T> klazz) {
        return ImmutableSet.copyOf(Iterables.transform(set, typeSafeCast(klazz)));
    }

    public static <T> Collection<T> typeSafeCollection(Collection<?> collection, Class<T> klazz) {
        return Collections2.transform(collection, typeSafeCast(klazz));
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
