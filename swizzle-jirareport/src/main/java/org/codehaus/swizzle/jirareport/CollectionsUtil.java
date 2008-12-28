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
package org.codehaus.swizzle.jirareport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

/**
 * I wish we could subclass java.util.Collections and simply add two methods, but
 * that class has only one constructor and it's private and cannot be subclassed.
 *
 * We're forced to use delegation.  The downside is that any methods added after
 * java 1.4 won't automatically be available here.
 *
 * This class adds two methods not available in Collections.
 *  - Object first(List list)
 *  - Object last(List list)
 *
 * @version $Revision$ $Date$
 */
public class CollectionsUtil {

    public String join(List list, String s){
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < list.size(); i++) {
            Object object = list.get(i);
            sb.append(object.toString());
            sb.append(s);
        }
        if (sb.length() > 0){
            sb.delete(sb.length() - s.length(), sb.length());
        }
        return sb.toString();
    }
    
    public Object first(List list){
        return list.size() > 0? list.get(0): null;
    }

    public Object last(List list){
        return list.size() > 0? list.get(list.size() -1): null;
    }

    public int binarySearch(List list, Object o) {
        return Collections.binarySearch(list, o);
    }

    public int binarySearch(List list, Object o, Comparator comparator) {
        return Collections.binarySearch(list, o, comparator);
    }

    public void copy(List list, List list1) {
        Collections.copy(list, list1);
    }

    public Enumeration enumeration(Collection collection) {
        return Collections.enumeration(collection);
    }

    public void fill(List list, Object o) {
        Collections.fill(list, o);
    }

    public int indexOfSubList(List list, List list1) {
        return Collections.indexOfSubList(list, list1);
    }

    public int lastIndexOfSubList(List list, List list1) {
        return Collections.lastIndexOfSubList(list, list1);
    }

    public ArrayList list(Enumeration enumeration) {
        return Collections.list(enumeration);
    }

    public Object max(Collection collection) {
        return Collections.max(collection);
    }

    public Object max(Collection collection, Comparator comparator) {
        return Collections.max(collection, comparator);
    }

    public Object min(Collection collection) {
        return Collections.min(collection);
    }

    public Object min(Collection collection, Comparator comparator) {
        return Collections.min(collection, comparator);
    }

    public List nCopies(int i, Object o) {
        return Collections.nCopies(i, o);
    }

    public boolean replaceAll(List list, Object o, Object o1) {
        return Collections.replaceAll(list, o, o1);
    }

    public void reverse(List list) {
        Collections.reverse(list);
    }

    public Comparator reverseOrder() {
        return Collections.reverseOrder();
    }

    public void rotate(List list, int i) {
        Collections.rotate(list, i);
    }

    public void shuffle(List list) {
        Collections.shuffle(list);
    }

    public void shuffle(List list, Random random) {
        Collections.shuffle(list, random);
    }

    public Set singleton(Object o) {
        return Collections.singleton(o);
    }

    public List singletonList(Object o) {
        return Collections.singletonList(o);
    }

    public Map singletonMap(Object o, Object o1) {
        return Collections.singletonMap(o, o1);
    }

    public void sort(List list) {
        Collections.sort(list);
    }

    public void sort(List list, Comparator comparator) {
        Collections.sort(list, comparator);
    }

    public void swap(List list, int i, int i1) {
        Collections.swap(list, i, i1);
    }

    public Collection synchronizedCollection(Collection collection) {
        return Collections.synchronizedCollection(collection);
    }

    public List synchronizedList(List list) {
        return Collections.synchronizedList(list);
    }

    public Map synchronizedMap(Map context) {
        return Collections.synchronizedMap(context);
    }

    public Set synchronizedSet(Set set) {
        return Collections.synchronizedSet(set);
    }

    public SortedMap synchronizedSortedMap(SortedMap sortedMap) {
        return Collections.synchronizedSortedMap(sortedMap);
    }

    public SortedSet synchronizedSortedSet(SortedSet sortedSet) {
        return Collections.synchronizedSortedSet(sortedSet);
    }

    public Collection unmodifiableCollection(Collection collection) {
        return Collections.unmodifiableCollection(collection);
    }

    public List unmodifiableList(List list) {
        return Collections.unmodifiableList(list);
    }

    public Map unmodifiableMap(Map context) {
        return Collections.unmodifiableMap(context);
    }

    public Set unmodifiableSet(Set set) {
        return Collections.unmodifiableSet(set);
    }

    public SortedMap unmodifiableSortedMap(SortedMap sortedMap) {
        return Collections.unmodifiableSortedMap(sortedMap);
    }

    public SortedSet unmodifiableSortedSet(SortedSet sortedSet) {
        return Collections.unmodifiableSortedSet(sortedSet);
    }
}
