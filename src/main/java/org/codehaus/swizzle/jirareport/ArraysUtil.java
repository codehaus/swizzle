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

import java.util.List;
import java.util.Comparator;
import java.util.Arrays;

/**
 * I wish we could subclass java.util.Arrays and simply add two methods, but
 * that class has only one constructor and it's private and cannot be subclassed.
 *
 * We're forced to use delegation.  The downside is that any methods added after
 * java 1.4 won't automatically be available here.
 *
 * This class adds two methods not available in Arrays.
 *  - <type> first(<type>[] array)
 *  - <type> last(<type>[] array)
 *
 * Both are overloaded to accommodate the possible array types
 * @version $Revision$ $Date$
 */
public class ArraysUtil  {

    public Object first(Object[] objects) {
        return objects.length > 0 ? objects[0]: null;
    }

    public byte first(byte[] bytes) {
        return bytes.length > 0 ? bytes[0] : 0;
    }

    public char first(char[] chars) {
        return chars.length > 0 ? chars[0] : 0;
    }

    public double first(double[] doubles) {
        return doubles.length > 0 ? doubles[0] : 0;
    }

    public float first(float[] floats) {
        return floats.length > 0 ? floats[0] : 0;
    }

    public int first(int[] ints) {
        return ints.length > 0 ? ints[0] : 0;
    }

    public long first(long[] longs) {
        return longs.length > 0 ? longs[0] : 0;
    }

    public short first(short[] shorts) {
        return shorts.length > 0 ? shorts[0] : 0;
    }

    public boolean first(boolean[] booleans) {
        return booleans.length > 0 && booleans[0];
    }

    public Object last(Object[] objects) {
        return objects.length > 0 ? objects[objects.length-1]: null;
    }

    public byte last(byte[] bytes) {
        return bytes.length > 0 ? bytes[bytes.length - 1] : 0;
    }

    public char last(char[] chars) {
        return chars.length > 0 ? chars[chars.length - 1] : 0;
    }

    public double last(double[] doubles) {
        return doubles.length > 0 ? doubles[doubles.length - 1] : 0;
    }

    public float last(float[] floats) {
        return floats.length > 0 ? floats[floats.length - 1] : 0;
    }

    public int last(int[] ints) {
        return ints.length > 0 ? ints[ints.length - 1] : 0;
    }

    public long last(long[] longs) {
        return longs.length > 0 ? longs[longs.length - 1] : 0;
    }

    public short last(short[] shorts) {
        return shorts.length > 0 ? shorts[shorts.length - 1] : 0;
    }

    public boolean last(boolean[] booleans) {
        return booleans.length > 0 && booleans[booleans.length - 1];
    }

    public List asList(Object[] objects) {
        return Arrays.asList(objects);
    }

    public int binarySearch(byte[] bytes, byte b) {
        return Arrays.binarySearch(bytes, b);
    }

    public int binarySearch(char[] chars, char c) {
        return Arrays.binarySearch(chars, c);
    }

    public int binarySearch(double[] doubles, double v) {
        return Arrays.binarySearch(doubles, v);
    }

    public int binarySearch(float[] floats, float v) {
        return Arrays.binarySearch(floats, v);
    }

    public int binarySearch(int[] ints, int i) {
        return Arrays.binarySearch(ints, i);
    }

    public int binarySearch(long[] longs, long l) {
        return Arrays.binarySearch(longs, l);
    }

    public int binarySearch(Object[] objects, Object o) {
        return Arrays.binarySearch(objects, o);
    }

    public int binarySearch(Object[] objects, Object o, Comparator comparator) {
        return Arrays.binarySearch(objects, o, comparator);
    }

    public int binarySearch(short[] shorts, short i) {
        return Arrays.binarySearch(shorts, i);
    }

    public boolean equals(boolean[] booleans, boolean[] booleans1) {
        return Arrays.equals(booleans, booleans1);
    }

    public boolean equals(byte[] bytes, byte[] bytes1) {
        return Arrays.equals(bytes, bytes1);
    }

    public boolean equals(char[] chars, char[] chars1) {
        return Arrays.equals(chars, chars1);
    }

    public boolean equals(double[] doubles, double[] doubles1) {
        return Arrays.equals(doubles, doubles1);
    }

    public boolean equals(float[] floats, float[] floats1) {
        return Arrays.equals(floats, floats1);
    }

    public boolean equals(int[] ints, int[] ints1) {
        return Arrays.equals(ints, ints1);
    }

    public boolean equals(long[] longs, long[] longs1) {
        return Arrays.equals(longs, longs1);
    }

    public boolean equals(Object[] objects, Object[] objects1) {
        return Arrays.equals(objects, objects1);
    }

    public boolean equals(short[] shorts, short[] shorts1) {
        return Arrays.equals(shorts, shorts1);
    }

    public void fill(boolean[] booleans, boolean b) {
        Arrays.fill(booleans, b);
    }

    public void fill(boolean[] booleans, int i, int i1, boolean b) {
        Arrays.fill(booleans, i, i1, b);
    }

    public void fill(byte[] bytes, byte b) {
        Arrays.fill(bytes, b);
    }

    public void fill(byte[] bytes, int i, int i1, byte b) {
        Arrays.fill(bytes, i, i1, b);
    }

    public void fill(char[] chars, char c) {
        Arrays.fill(chars, c);
    }

    public void fill(char[] chars, int i, int i1, char c) {
        Arrays.fill(chars, i, i1, c);
    }

    public void fill(double[] doubles, double v) {
        Arrays.fill(doubles, v);
    }

    public void fill(double[] doubles, int i, int i1, double v) {
        Arrays.fill(doubles, i, i1, v);
    }

    public void fill(float[] floats, float v) {
        Arrays.fill(floats, v);
    }

    public void fill(float[] floats, int i, int i1, float v) {
        Arrays.fill(floats, i, i1, v);
    }

    public void fill(int[] ints, int i) {
        Arrays.fill(ints, i);
    }

    public void fill(int[] ints, int i, int i1, int i2) {
        Arrays.fill(ints, i, i1, i2);
    }

    public void fill(long[] longs, int i, int i1, long l) {
        Arrays.fill(longs, i, i1, l);
    }

    public void fill(long[] longs, long l) {
        Arrays.fill(longs, l);
    }

    public void fill(Object[] objects, int i, int i1, Object o) {
        Arrays.fill(objects, i, i1, o);
    }

    public void fill(Object[] objects, Object o) {
        Arrays.fill(objects, o);
    }

    public void fill(short[] shorts, int i, int i1, short i2) {
        Arrays.fill(shorts, i, i1, i2);
    }

    public void fill(short[] shorts, short i) {
        Arrays.fill(shorts, i);
    }

    public void sort(byte[] bytes) {
        Arrays.sort(bytes);
    }

    public void sort(byte[] bytes, int i, int i1) {
        Arrays.sort(bytes, i, i1);
    }

    public void sort(char[] chars) {
        Arrays.sort(chars);
    }

    public void sort(char[] chars, int i, int i1) {
        Arrays.sort(chars, i, i1);
    }

    public void sort(double[] doubles) {
        Arrays.sort(doubles);
    }

    public void sort(double[] doubles, int i, int i1) {
        Arrays.sort(doubles, i, i1);
    }

    public void sort(float[] floats) {
        Arrays.sort(floats);
    }

    public void sort(float[] floats, int i, int i1) {
        Arrays.sort(floats, i, i1);
    }

    public void sort(int[] ints) {
        Arrays.sort(ints);
    }

    public void sort(int[] ints, int i, int i1) {
        Arrays.sort(ints, i, i1);
    }

    public void sort(long[] longs) {
        Arrays.sort(longs);
    }

    public void sort(long[] longs, int i, int i1) {
        Arrays.sort(longs, i, i1);
    }

    public void sort(Object[] objects) {
        Arrays.sort(objects);
    }

    public void sort(Object[] objects, Comparator comparator) {
        Arrays.sort(objects, comparator);
    }

    public void sort(Object[] objects, int i, int i1) {
        Arrays.sort(objects, i, i1);
    }

    public void sort(Object[] objects, int i, int i1, Comparator comparator) {
        Arrays.sort(objects, i, i1, comparator);
    }

    public void sort(short[] shorts) {
        Arrays.sort(shorts);
    }

    public void sort(short[] shorts, int i, int i1) {
        Arrays.sort(shorts, i, i1);
    }
}
