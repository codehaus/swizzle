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
public class FilterTest extends TestCase {

    public void testGetSavedFilter() throws Exception {
        Jira jira = new Jira("http://jira.codehaus.org/rpc/xmlrpc");
        jira.login("swizzletester", "swizzle");

        Filter filter = jira.getSavedFilter("swizzle test issues");
        assertNotNull("filter", filter);
        assertEquals("Filter.getId()", 11892, filter.getId());
        assertEquals("Filter.getName()", "swizzle test issues", filter.getName());
        assertEquals("Filter.getDescription()", "The swizzle test filter description", filter.getDescription());
        assertEquals("Filter.getAuthor().getName()", "swizzletester", filter.getAuthor().getName());

    }

    public void testGetIssuesFromFilter() throws Exception {
        Jira jira = new Jira("http://jira.codehaus.org/rpc/xmlrpc");
        jira.login("swizzletester", "swizzle");

        List issues = jira.getIssuesFromFilter("swizzle test issues");

        assertNotNull("issues", issues);
        assertEquals("Issues.size()", 7, issues.size());

        Issue issue = (Issue) issues.get(0);
        assertNotNull("issue",issue);
        assertEquals("Issue.getKey()", "SWIZZLE-7", issue.getKey());
        assertEquals("Issue.getSummary()", "Put a TV in the bathroom", issue.getSummary());

        issue = (Issue) issues.get(1);
        assertNotNull("issue",issue);
        assertEquals("Issue.getKey()", "SWIZZLE-12", issue.getKey());
        assertEquals("Issue.getSummary()", "Fix whole in the wall", issue.getSummary());

        issue = (Issue) issues.get(2);
        assertNotNull("issue",issue);
        assertEquals("Issue.getKey()", "SWIZZLE-11", issue.getKey());
        assertEquals("Issue.getSummary()", "Make sure the smoke alarms work", issue.getSummary());

        issue = (Issue) issues.get(3);
        assertNotNull("issue",issue);
        assertEquals("Issue.getKey()", "SWIZZLE-8", issue.getKey());
        assertEquals("Issue.getSummary()", "Get more colored lights for the disco ball", issue.getSummary());

        issue = (Issue) issues.get(4);
        assertNotNull("issue",issue);
        assertEquals("Issue.getKey()", "SWIZZLE-10", issue.getKey());
        assertEquals("Issue.getSummary()", "Beer fridge for the garage", issue.getSummary());

        issue = (Issue) issues.get(5);
        assertNotNull("issue",issue);
        assertEquals("Issue.getKey()", "SWIZZLE-9", issue.getKey());
        assertEquals("Issue.getSummary()", "Clean the pool", issue.getSummary());

        issue = (Issue) issues.get(6);
        assertNotNull("issue",issue);
        assertEquals("Issue.getKey()", "SWIZZLE-6", issue.getKey());
        assertEquals("Issue.getSummary()", "Fix the toilet", issue.getSummary());

    }
}
