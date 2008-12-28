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

import junit.framework.TestCase;

/**
 * @version $Revision$ $Date$
 */
public class CollectionsUtilTest extends TestCase {

    public void testJoin() throws Exception {
        CollectionsUtil util = new CollectionsUtil();

        ArrayList list = new ArrayList();
        list.add("apple");
        list.add("banana");
        list.add("orange");
        String actual = util.join(list, ", ");
        assertEquals("apple, banana, orange", actual);
    }
}
