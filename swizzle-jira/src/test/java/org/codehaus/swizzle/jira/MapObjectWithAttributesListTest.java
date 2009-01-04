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
public class MapObjectWithAttributesListTest extends TestCase {
    private MapObjectList list;

    protected void setUp() throws Exception {
        list = new MapObjectList();

        Version version = new Version();
        version.setName("two");
        version.setSequence(5);
        version.setArchived(true);
        version.getAttributes().put("name", "dos");
        version.getAttributes().put("seq", "50");
        version.getAttributes().put("arc", "true");
        list.add(version);

        version = new Version();
        version.setName("one");
        version.setSequence(1);
        version.setArchived(true);
        version.getAttributes().put("name", "uno");
        version.getAttributes().put("seq", "10");
        version.getAttributes().put("arc", "true");
        list.add(version);

        version = new Version();
        version.setName("four");
        version.setSequence(20);
        version.setArchived(false);
        version.getAttributes().put("name", "cuatro");
        version.getAttributes().put("seq", "200");
        version.getAttributes().put("arc", "false");
        list.add(version);

        version = new Version();
        version.setName("three");
        version.setSequence(15);
        version.setArchived(false);
        version.getAttributes().put("name", "tres");
        version.getAttributes().put("seq", "150");
        version.getAttributes().put("arc", "false");
        list.add(version);

    }

    public void testSort() throws Exception {
        List sorted = list.sort("@seq");

        assertEquals("size", 4, sorted.size());
        assertEquals("10", ((Version) sorted.get(0)).getAttributes().get("seq"));
        assertEquals("150", ((Version) sorted.get(1)).getAttributes().get("seq"));
        assertEquals("200", ((Version) sorted.get(2)).getAttributes().get("seq"));
        assertEquals("50", ((Version) sorted.get(3)).getAttributes().get("seq"));

        sorted = list.sort("@name");
        assertEquals("cuatro", ((Version) sorted.get(0)).getAttributes().get("name"));
        assertEquals("dos", ((Version) sorted.get(1)).getAttributes().get("name"));
        assertEquals("tres", ((Version) sorted.get(2)).getAttributes().get("name"));
        assertEquals("uno", ((Version) sorted.get(3)).getAttributes().get("name"));
    }

    public void testSortReverse() throws Exception {
        List sorted = list.sort("@seq", true);

        assertEquals("size", 4, sorted.size());
        assertEquals("50", ((Version) sorted.get(0)).getAttributes().get("seq"));
        assertEquals("200", ((Version) sorted.get(1)).getAttributes().get("seq"));
        assertEquals("150", ((Version) sorted.get(2)).getAttributes().get("seq"));
        assertEquals("10", ((Version) sorted.get(3)).getAttributes().get("seq"));

        sorted = list.sort("@name", true);
        assertEquals("uno", ((Version) sorted.get(0)).getAttributes().get("name"));
        assertEquals("tres", ((Version) sorted.get(1)).getAttributes().get("name"));
        assertEquals("dos", ((Version) sorted.get(2)).getAttributes().get("name"));
        assertEquals("cuatro", ((Version) sorted.get(3)).getAttributes().get("name"));
    }

    public void testContains() throws Exception {
        List sorted = list.contains("@name", "e");

        assertEquals("size", 1, sorted.size());
        assertEquals("tres", ((Version) sorted.get(0)).getAttributes().get("name"));
    }

    public void testEquals() throws Exception {
        List sorted = list.equals("@arc", "true");

        assertEquals("size", 2, sorted.size());
        assertEquals("two", ((Version) sorted.get(0)).getName());
        assertEquals("one", ((Version) sorted.get(1)).getName());

        sorted = list.equals("archived", "false");

        assertEquals("size", 2, sorted.size());
        assertEquals("four", ((Version) sorted.get(0)).getName());
        assertEquals("three", ((Version) sorted.get(1)).getName());
    }

    public void testMatches() throws Exception {
        List sorted = list.matches("@name", ".o.*");

        assertEquals("size", 1, sorted.size());
        assertEquals("dos", ((Version) sorted.get(0)).getAttributes().get("name"));

        sorted = list.matches("@name", "t.*");

        assertEquals("size", 1, sorted.size());
        assertEquals("tres", ((Version) sorted.get(0)).getAttributes().get("name"));
    }

    public void testGreater() throws Exception {
        List sorted = list.greater("@seq", "19");

        assertEquals("size", 2, sorted.size());
        assertEquals("50", ((Version) sorted.get(0)).getAttributes().get("seq"));
        assertEquals("200", ((Version) sorted.get(1)).getAttributes().get("seq"));
    }

    public void testLess() throws Exception {
        List sorted = list.less("@seq", "2");

        assertEquals("size", 2, sorted.size());
        assertEquals("10", ((Version) sorted.get(0)).getAttributes().get("seq"));
        assertEquals("150", ((Version) sorted.get(1)).getAttributes().get("seq"));
    }

    public void testSum() throws Exception {
        assertEquals(410, list.sum("@seq"));
        assertEquals(0, list.sum("@name"));
    }

    public void testAverage() throws Exception {
        assertEquals(102, list.average("@seq"));
        assertEquals(0, list.average("@name"));
    }

}
