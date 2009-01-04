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

import junit.framework.TestCase;

import java.util.List;

/**
 * @version $Revision$ $Date$
 */
public class MapObjectListTest extends TestCase {
    private MapObjectList list;
    private MapObjectList listb;

    protected void setUp() throws Exception {
        list = new MapObjectList();
        listb = new MapObjectList();

        Version version = new Version();
        version = new Version();
        version.setName("six");
        version.setSequence(25);
        version.setArchived(false);
        listb.add(version);

        version = new Version();
        version.setName("five");
        version.setSequence(30);
        version.setArchived(false);
        listb.add(version);

        version = new Version();
        version.setName("two");
        version.setSequence(5);
        version.setArchived(true);
        list.add(version);

        version = new Version();
        version.setName("one");
        version.setSequence(1);
        version.setArchived(true);
        list.add(version);

        version = new Version();
        version.setName("four");
        version.setSequence(20);
        version.setArchived(false);
        list.add(version);
        listb.add(version);

        version = new Version();
        version.setName("three");
        version.setSequence(15);
        version.setArchived(false);
        list.add(version);
        listb.add(version);

    }

    public void testSort() throws Exception {
        List sorted = list.sort("sequence");

        assertEquals("size", 4, sorted.size());
        assertEquals(1, ((Version) sorted.get(0)).getSequence());
        assertEquals(5, ((Version) sorted.get(1)).getSequence());
        assertEquals(15, ((Version) sorted.get(2)).getSequence());
        assertEquals(20, ((Version) sorted.get(3)).getSequence());

        sorted = list.sort("name");
        assertEquals("four", ((Version) sorted.get(0)).getName());
        assertEquals("one", ((Version) sorted.get(1)).getName());
        assertEquals("three", ((Version) sorted.get(2)).getName());
        assertEquals("two", ((Version) sorted.get(3)).getName());
    }

    public void testSortReverse() throws Exception {
        List sorted = list.sort("sequence", true);

        assertEquals("size", 4, sorted.size());
        assertEquals(20, ((Version) sorted.get(0)).getSequence());
        assertEquals(15, ((Version) sorted.get(1)).getSequence());
        assertEquals(5, ((Version) sorted.get(2)).getSequence());
        assertEquals(1, ((Version) sorted.get(3)).getSequence());

        sorted = list.sort("name", true);
        assertEquals("two", ((Version) sorted.get(0)).getName());
        assertEquals("three", ((Version) sorted.get(1)).getName());
        assertEquals("one", ((Version) sorted.get(2)).getName());
        assertEquals("four", ((Version) sorted.get(3)).getName());
    }

    public void testContains() throws Exception {
        List sorted = list.contains("name", "e");

        assertEquals("size", 2, sorted.size());
        assertEquals("one", ((Version) sorted.get(0)).getName());
        assertEquals("three", ((Version) sorted.get(1)).getName());
    }

    public void testEquals() throws Exception {
        List sorted = list.equals("archived", "true");

        assertEquals("size", 2, sorted.size());
        assertEquals("two", ((Version) sorted.get(0)).getName());
        assertEquals("one", ((Version) sorted.get(1)).getName());

        sorted = list.equals("archived", "false");

        assertEquals("size", 2, sorted.size());
        assertEquals("four", ((Version) sorted.get(0)).getName());
        assertEquals("three", ((Version) sorted.get(1)).getName());
    }

    public void testMatches() throws Exception {
        List sorted = list.matches("name", ".o.*");

        assertEquals("size", 1, sorted.size());
        assertEquals("four", ((Version) sorted.get(0)).getName());

        sorted = list.matches("name", "t.*");

        assertEquals("size", 2, sorted.size());
        assertEquals("two", ((Version) sorted.get(0)).getName());
        assertEquals("three", ((Version) sorted.get(1)).getName());
    }

    public void testGreater() throws Exception {
        List sorted = list.greater("sequence", "5");

        assertEquals("size", 2, sorted.size());
        assertEquals(20, ((Version) sorted.get(0)).getSequence());
        assertEquals(15, ((Version) sorted.get(1)).getSequence());
    }

    public void testLess() throws Exception {
        List sorted = list.less("sequence", "10");

        assertEquals("size", 2, sorted.size());
        assertEquals(5, ((Version) sorted.get(0)).getSequence());
        assertEquals(1, ((Version) sorted.get(1)).getSequence());
    }

    public void testSum() throws Exception {
        assertEquals(41, list.sum("sequence"));
        assertEquals(0, list.sum("name"));
    }

    public void testAverage() throws Exception {
        assertEquals(10, list.average("sequence"));
        assertEquals(0, list.average("name"));
    }

    public void testMin() throws Exception {
        Version version = (Version) list.min("sequence");

        assertNotNull("version", version);
        assertEquals("Version.getSequence()", 1, version.getSequence());
        assertEquals("Version.getName()", "one", version.getName());
    }

    public void testMax() throws Exception {
        Version version = (Version) list.max("sequence");

        assertNotNull("version", version);
        assertEquals("Version.getSequence()", 20, version.getSequence());
        assertEquals("Version.getName()", "four", version.getName());
    }

    public void testUnique() throws Exception {
        MapObjectList sublist = list.unique("archived");

        assertEquals("size", 2, sublist.size());
        assertEquals("two", ((Version) sublist.get(0)).getName());
        assertEquals("four", ((Version) sublist.get(1)).getName());
    }

    public void testUnion() throws Exception {
        MapObjectList result = list.union(listb);

        assertEquals("size", 6, result.size());
        assertEquals("two", ((Version) result.get(0)).getName());
        assertEquals("one", ((Version) result.get(1)).getName());
        assertEquals("four", ((Version) result.get(2)).getName());
        assertEquals("three", ((Version) result.get(3)).getName());
        assertEquals("six", ((Version) result.get(4)).getName());
        assertEquals("five", ((Version) result.get(5)).getName());
    }

    public void testIntersection() throws Exception {
        MapObjectList result = list.intersection(listb);

        assertEquals("size", 2, result.size());
        assertEquals("four", ((Version) result.get(0)).getName());
        assertEquals("three", ((Version) result.get(1)).getName());
    }

    public void testSubtract() throws Exception {
        MapObjectList result = list.subtract(listb);

        assertEquals("size", 2, result.size());
        assertEquals("two", ((Version) result.get(0)).getName());
        assertEquals("one", ((Version) result.get(1)).getName());
    }

    public void testDifference() throws Exception {
        MapObjectList result = list.difference(listb);

        assertEquals("size", 4, result.size());
        assertEquals("two", ((Version) result.get(0)).getName());
        assertEquals("one", ((Version) result.get(1)).getName());
        assertEquals("six", ((Version) result.get(2)).getName());
        assertEquals("five", ((Version) result.get(3)).getName());
    }

}
