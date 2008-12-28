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

import java.util.List;

import junit.framework.TestCase;

/**
 * @version $Revision$ $Date$
 */
public class ComplexMapObjectListTest extends TestCase {

    private MapObjectList issues;
    private IssueType wish;
    private IssueType bug;
    private IssueType test;
    private IssueType improvement;
    private IssueType feature;
    private IssueType task;


    protected void setUp() throws Exception {

        // Create the standard issue types

        wish = new IssueType();
        wish.setId(5);
        wish.setName("Wish");

        bug = new IssueType();
        bug.setId(1);
        bug.setName("Bug");

        test = new IssueType();
        test.setId(6);
        test.setName("Test");

        improvement = new IssueType();
        improvement.setId(4);
        improvement.setName("Improvement");

        feature = new IssueType();
        feature.setId(2);
        feature.setName("New Feature");

        task = new IssueType();
        task.setId(3);
        task.setName("Task");

        // Create some test issues

        issues = new MapObjectList();
        Issue issue;

        issue = new Issue();
        issue.setId(41275);
        issue.setKey("SWIZZLE-7");
        issue.setSummary("Put a TV in the bathroom");
        issue.setType(wish);
        issues.add(issue);

        issue = new Issue();
        issue.setId(41280);
        issue.setKey("SWIZZLE-12");
        issue.setSummary("Fix whole in the wall");
        issue.setType(bug);
        issues.add(issue);

        issue = new Issue();
        issue.setId(41279);
        issue.setKey("SWIZZLE-11");
        issue.setSummary("Make sure the smoke alarms work");
        issue.setType(test);
        issues.add(issue);

        issue = new Issue();
        issue.setId(41276);
        issue.setKey("SWIZZLE-8");
        issue.setSummary("Get more colored lights for the disco ball");
        issue.setType(improvement);
        issues.add(issue);

        issue = new Issue();
        issue.setId(41278);
        issue.setKey("SWIZZLE-10");
        issue.setSummary("Beer fridge for the garage");
        issue.setType(feature);
        issues.add(issue);

        issue = new Issue();
        issue.setId(41277);
        issue.setKey("SWIZZLE-9");
        issue.setSummary("Clean the pool");
        issue.setType(task);
        issues.add(issue);

        issue = new Issue();
        issue.setId(41274);
        issue.setKey("SWIZZLE-6");
        issue.setSummary("Fix the toilet");
        issue.setType(bug);
        issues.add(issue);

        // Create the standard issue types again
        // to guarantee that they are not == comparable

        wish = new IssueType();
        wish.setId(5);
        wish.setName("Wish");

        bug = new IssueType();
        bug.setId(1);
        bug.setName("Bug");

        test = new IssueType();
        test.setId(6);
        test.setName("Test");

        improvement = new IssueType();
        improvement.setId(4);
        improvement.setName("Improvement");

        feature = new IssueType();
        feature.setId(2);
        feature.setName("New Feature");

        task = new IssueType();
        task.setId(3);
        task.setName("Task");

    }


    public void testSort() throws Exception {
        List list = issues.sort("type");

        assertEquals("size", 7, list.size());
        assertEquals("Issue.getKey()", "SWIZZLE-12", ((Issue)list.get(0)).getKey());
        assertEquals("Issue.getType()", bug, ((Issue)list.get(0)).getType());
        assertEquals("Issue.getKey()", "SWIZZLE-6", ((Issue)list.get(1)).getKey());
        assertEquals("Issue.getType()", bug, ((Issue)list.get(1)).getType());
        assertEquals("Issue.getKey()", "SWIZZLE-8", ((Issue)list.get(2)).getKey());
        assertEquals("Issue.getType()", improvement, ((Issue)list.get(2)).getType());
        assertEquals("Issue.getKey()", "SWIZZLE-10", ((Issue)list.get(3)).getKey());
        assertEquals("Issue.getType()", feature, ((Issue)list.get(3)).getType());
        assertEquals("Issue.getKey()", "SWIZZLE-9", ((Issue)list.get(4)).getKey());
        assertEquals("Issue.getType()", task, ((Issue)list.get(4)).getType());
        assertEquals("Issue.getKey()", "SWIZZLE-11", ((Issue)list.get(5)).getKey());
        assertEquals("Issue.getType()", test, ((Issue)list.get(5)).getType());
        assertEquals("Issue.getKey()", "SWIZZLE-7", ((Issue)list.get(6)).getKey());
        assertEquals("Issue.getType()", wish, ((Issue)list.get(6)).getType());
    }

    public void testReverseSort() throws Exception {
        List list = issues.sort("type", true);

        assertEquals("size", 7, list.size());
        assertEquals("Issue.getKey()", "SWIZZLE-7", ((Issue)list.get(0)).getKey());
        assertEquals("Issue.getType()", wish, ((Issue)list.get(0)).getType());
        assertEquals("Issue.getKey()", "SWIZZLE-11", ((Issue)list.get(1)).getKey());
        assertEquals("Issue.getType()", test, ((Issue)list.get(1)).getType());
        assertEquals("Issue.getKey()", "SWIZZLE-9", ((Issue)list.get(2)).getKey());
        assertEquals("Issue.getType()", task, ((Issue)list.get(2)).getType());
        assertEquals("Issue.getKey()", "SWIZZLE-10", ((Issue)list.get(3)).getKey());
        assertEquals("Issue.getType()", feature, ((Issue)list.get(3)).getType());
        assertEquals("Issue.getKey()", "SWIZZLE-8", ((Issue)list.get(4)).getKey());
        assertEquals("Issue.getType()", improvement, ((Issue)list.get(4)).getType());
        assertEquals("Issue.getKey()", "SWIZZLE-12", ((Issue)list.get(5)).getKey());
        assertEquals("Issue.getType()", bug, ((Issue)list.get(5)).getType());
        assertEquals("Issue.getKey()", "SWIZZLE-6", ((Issue)list.get(6)).getKey());
        assertEquals("Issue.getType()", bug, ((Issue)list.get(6)).getType());
    }

    public void testContains() throws Exception {
        List list = issues.contains("type", "t");

        assertEquals("size", 3, list.size());
        assertEquals("Issue.getKey()", "SWIZZLE-11", ((Issue)list.get(0)).getKey());
        assertEquals("Issue.getType()", test, ((Issue)list.get(0)).getType());
        assertEquals("Issue.getKey()", "SWIZZLE-8", ((Issue)list.get(1)).getKey());
        assertEquals("Issue.getType()", improvement, ((Issue)list.get(1)).getType());
        assertEquals("Issue.getKey()", "SWIZZLE-10", ((Issue)list.get(2)).getKey());
        assertEquals("Issue.getType()", feature, ((Issue)list.get(2)).getType());
    }

    public void testEquals() throws Exception {
        List list = issues.equals("type", "Bug");

        assertEquals("size", 2, list.size());
        assertEquals("Issue.getKey()", "SWIZZLE-12", ((Issue)list.get(0)).getKey());
        assertEquals("Issue.getType()", bug, ((Issue)list.get(0)).getType());
        assertEquals("Issue.getKey()", "SWIZZLE-6", ((Issue)list.get(1)).getKey());
        assertEquals("Issue.getType()", bug, ((Issue)list.get(1)).getType());
    }

    public void testMatches() throws Exception {
        List list = issues.matches("type", "New Feature|Improvement");

        assertEquals("size", 2, list.size());
        assertEquals("Issue.getKey()", "SWIZZLE-8", ((Issue)list.get(0)).getKey());
        assertEquals("Issue.getType()", improvement, ((Issue)list.get(0)).getType());
        assertEquals("Issue.getKey()", "SWIZZLE-10", ((Issue)list.get(1)).getKey());
        assertEquals("Issue.getType()", feature, ((Issue)list.get(1)).getType());
    }


    public void testGreater() throws Exception {
        List list = issues.greater("type", bug);

        assertEquals("size", 5, list.size());
        assertEquals("Issue.getKey()", "SWIZZLE-7", ((Issue)list.get(0)).getKey());
        assertEquals("Issue.getType()", wish, ((Issue)list.get(0)).getType());
        assertEquals("Issue.getKey()", "SWIZZLE-11", ((Issue)list.get(1)).getKey());
        assertEquals("Issue.getType()", test, ((Issue)list.get(1)).getType());
        assertEquals("Issue.getKey()", "SWIZZLE-8", ((Issue)list.get(2)).getKey());
        assertEquals("Issue.getType()", improvement, ((Issue)list.get(2)).getType());
        assertEquals("Issue.getKey()", "SWIZZLE-10", ((Issue)list.get(3)).getKey());
        assertEquals("Issue.getType()", feature, ((Issue)list.get(3)).getType());
        assertEquals("Issue.getKey()", "SWIZZLE-9", ((Issue)list.get(4)).getKey());
        assertEquals("Issue.getType()", task, ((Issue)list.get(4)).getType());
    }

    public void testLess() throws Exception {
        List list = issues.less("type", test);

        assertEquals("size", 5, list.size());
        assertEquals("Issue.getKey()", "SWIZZLE-12", ((Issue)list.get(0)).getKey());
        assertEquals("Issue.getType()", bug, ((Issue)list.get(0)).getType());
        assertEquals("Issue.getKey()", "SWIZZLE-8", ((Issue)list.get(1)).getKey());
        assertEquals("Issue.getType()", improvement, ((Issue)list.get(1)).getType());
        assertEquals("Issue.getKey()", "SWIZZLE-10", ((Issue)list.get(2)).getKey());
        assertEquals("Issue.getType()", feature, ((Issue)list.get(2)).getType());
        assertEquals("Issue.getKey()", "SWIZZLE-9", ((Issue)list.get(3)).getKey());
        assertEquals("Issue.getType()", task, ((Issue)list.get(3)).getType());
        assertEquals("Issue.getKey()", "SWIZZLE-6", ((Issue)list.get(4)).getKey());
        assertEquals("Issue.getType()", bug, ((Issue)list.get(4)).getType());
    }

    public void testSum() throws Exception {
        int sum = issues.sum("type");
        assertEquals("sum", 0, sum);
    }

    public void testAverage() throws Exception {
        int average = issues.average("type");
        assertEquals("average", 0, average);
    }


    public void testMin() throws Exception {
        Issue issue = (Issue) issues.min("type");

        assertEquals("Issue.getKey()", "SWIZZLE-12", issue.getKey());
        assertEquals("Issue.getType()", bug, issue.getType());
    }

    public void testMax() throws Exception {
        Issue issue = (Issue) issues.max("type");

        assertEquals("Issue.getKey()", "SWIZZLE-7", issue.getKey());
        assertEquals("Issue.getType()", wish, issue.getType());
    }


}
