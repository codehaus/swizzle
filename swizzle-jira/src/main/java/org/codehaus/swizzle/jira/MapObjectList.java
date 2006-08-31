/**
 *
 * Copyright 2006 David Blevins
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.codehaus.swizzle.jira;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Comparator;
import java.util.Collections;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * @version $Revision$ $Date$
 */
public class MapObjectList extends ArrayList {

    public MapObjectList() {
    }

    public MapObjectList(Collection collection) {
        super(collection);
    }

    public MapObjectList(int i) {
        super(i);
    }

    public MapObjectList contains(String field, String string){
        if (size() == 0) return this;
        MapObjectList subset = new MapObjectList();
        for (int i = 0; i < this.size(); i++) {
            MapObject mapObject = (MapObject) this.get(i);
            Map fields = mapObject.fields;
            if (fields.containsKey(field)){
                String value = fields.get(field) + "";
                if (value.indexOf(string) != -1){
                    subset.add(mapObject);
                }
            }
        }
        return subset;
    }

    public MapObjectList matches(String field, String string){
        if (size() == 0) return this;
        Pattern pattern = Pattern.compile(string);
        MapObjectList subset = new MapObjectList();
        for (int i = 0; i < this.size(); i++) {
            MapObject mapObject = (MapObject) this.get(i);
            Map fields = mapObject.fields;
            if (fields.containsKey(field)){
                String value = fields.get(field) + "";
                if (pattern.matcher(value).matches()){
                    subset.add(mapObject);
                }
            }
        }
        return subset;
    }

    public MapObjectList equals(String field, String string){
        if (size() == 0) return this;
        MapObjectList subset = new MapObjectList();
        for (int i = 0; i < this.size(); i++) {
            MapObject mapObject = (MapObject) this.get(i);
            Map fields = mapObject.fields;
            if (fields.containsKey(field)){
                String value = fields.get(field) + "";
                if (value.equals(string)){
                    subset.add(mapObject);
                }
            }
        }
        return subset;
    }

    public MapObjectList greater(String field, String string){
        return compare(field, string, 1);
    }

    public MapObjectList less(String field, String string){
        return compare(field, string, -1);
    }

    /**
     * Synonym for sort(field, false);
     * @param field
     */
    public MapObjectList ascending(String field){
        return sort(field);
    }

    /**
     * Synonym for sort(field, true);
     * @param field
     */
    public MapObjectList descending(String field){
        return sort(field, true);
    }

    public MapObjectList sort(String field){
        return sort(field, false);
    }

    public MapObjectList sort(String field, boolean reverse){
        if (size() == 0) return this;
        Comparator comparator = getComparator(field);

        comparator = reverse? new ReverseComparator(comparator): comparator;
        Collections.sort(this, comparator);

        return this;
    }

    private MapObjectList compare(String field, String string, int condition) {
        if (size() == 0) return this;
        try {
            Class type = get(0).getClass();
            HashMap map = new HashMap();
            map.put(field, string);
            Constructor constructor = type.getConstructor(new Class[]{Map.class});
            Object base = constructor.newInstance(new Object[]{map});

            Comparator comparator = getComparator(field);

            MapObjectList subset = new MapObjectList();
            for (int i = 0; i < this.size(); i++) {
                Object object = this.get(i);
                int value = comparator.compare(object, base);
                if (value == condition){
                    subset.add(object);
                }
            }
            return subset;
        } catch (Exception e) {
            return new MapObjectList();
        }
    }

    private Comparator getComparator(String field) {
        Method accessor = getAccessor(field);

        Comparator comparator;
        if (accessor != null && (accessor.getReturnType().isPrimitive() || Comparable.class.isAssignableFrom(accessor.getReturnType()))){
            comparator = new MethodValueComparator(accessor);
        } else {
            comparator = new FieldValueComparator(field);
        }
        return comparator;
    }

    private static class ReverseComparator implements Comparator {
        private final Comparator comparator;

        public ReverseComparator(Comparator comparator) {
            this.comparator = comparator;
        }

        public int compare(Object a, Object b) {
            return -1 * comparator.compare(a, b);
        }
    }

    private static class CollectingComparator implements Comparator {
        private final Comparator comparator;
        private final int condition;
        private final MapObjectList list;

        public CollectingComparator(MapObjectList list, int condition, Comparator comparator) {
            this.comparator = comparator;
            this.condition = condition;
            this.list = list;
        }

        public int compare(Object a, Object b) {
            int value = comparator.compare(a, b);
            if (value == condition){
                list.add(b);
            }
            return value;
        }
    }

    private static class FieldValueComparator implements Comparator {
        private final String field;

        public FieldValueComparator(String field) {
            this.field = field;
        }

        public int compare(Object objectA, Object objectB) {
            try {
                MapObject mapA = (MapObject) objectA;
                MapObject mapB = (MapObject) objectB;
                String a = mapA.fields.get(field)+"";
                String b = mapB.fields.get(field)+"";

                return a.compareTo(b);
            } catch (Exception e) {
                return 0;
            }
        }
    }

    private static class MethodValueComparator implements Comparator {
        private final Method accessor;
        private static final Class[] NO_ARGS = new Class[]{};

        public MethodValueComparator(Method accessor) {
            this.accessor = accessor;
        }

        public int compare(Object objectA, Object objectB) {
            try {
                Comparable a = (Comparable) accessor.invoke(objectA, NO_ARGS);
                Comparable b = (Comparable) accessor.invoke(objectB, NO_ARGS);
                return a.compareTo(b);
            } catch (Exception e){
                return 0;
            }
        }
    }
    /**
     * This only works as we assume the list is homogeneous
     */
    private Method getAccessor(String field) {
        try {
            MapObject first = (MapObject) get(0);
            StringBuffer sb = new StringBuffer(field);
            sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
            return first.getClass().getMethod("get" + sb, new Class[]{});
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

}
