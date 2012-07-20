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

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * @version $Revision$ $Date$
 */
public class SubTasksTest extends SwizzleJiraTestCase {

    // Date is retrieved without timezone from xmlrpc (@codehaus?)
    SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");

    public void testJira() throws Exception {
        Jira jira = getJira();
        jira.autofill("subtasks", true);

        Issue issue = jira.getIssue("SWIZZLE-2");
        assertEquals("Issue.getCreated()", "Sun Aug 27 18:23:40 2006", formatter.format(issue.getCreated()));
        assertEquals("Issue.getSummary()", "Need Wilhemina to get some things from the store", issue.getSummary());
        assertEquals("Issue.getType()", 3, issue.getType().getId());
        assertEquals("Issue.getEnvironment()", null, issue.getEnvironment());
        assertEquals("Issue.getStatus()", 6, issue.getStatus().getId());
        assertEquals("Issue.getUpdated()", "Sun Aug 27 18:27:49 2006", formatter.format(issue.getUpdated()));
        assertEquals("Issue.getId()", 40911, issue.getId());
        assertEquals("Issue.getKey()", "SWIZZLE-2", issue.getKey());
        assertEquals("Issue.getDescription()", null, issue.getDescription());
        assertEquals("Issue.getReporter()", "dblevins", issue.getReporter().getName());
        assertEquals("Issue.getProject()", "SWIZZLE", issue.getProject().getKey());
        assertEquals("Issue.getResolution()", 1, issue.getResolution().getId());
        assertEquals("Issue.getVotes()", 0, issue.getVotes());
        assertEquals("Issue.getAssignee()", "dblevins", issue.getAssignee().getName());
        assertEquals("Issue.getPriority()", 3, issue.getPriority().getId());
        assertEquals("Issue.getLink()", "https://jira.codehaus.org/browse/SWIZZLE-2", issue.getLink());

        List subTasks = issue.getSubTasks();
        assertNotNull("subtasks", subTasks);
        //TODO: there is no way to get subtasks directly from an issue ...
        //assertTrue("three subtasks", subTasks.size() == 3);
        
//        issue = (Issue) subTasks.get(0);
//        assertEquals("Issue.getSummary()", "a loaf of bread", issue.getSummary());
//        assertEquals("Issue.getType()", 7, issue.getType().getId());
//        assertEquals("Issue.getStatus()", 6, issue.getStatus().getId());
//        assertEquals("Issue.getId()", 40912, issue.getId());
//        assertEquals("Issue.getKey()", "SWIZZLE-3", issue.getKey());
//        assertEquals("Issue.getReporter()", "dblevins", issue.getReporter().getName());
//        assertEquals("Issue.getProject()", "SWIZZLE", issue.getProject().getKey());
//        assertEquals("Issue.getResolution()", 1, issue.getResolution().getId());
//
//        issue = (Issue) subTasks.get(1);
//        assertEquals("Issue.getSummary()", "a container of milk", issue.getSummary());
//        assertEquals("Issue.getType()", 7, issue.getType().getId());
//        assertEquals("Issue.getStatus()", 6, issue.getStatus().getId());
//        assertEquals("Issue.getId()", 40913, issue.getId());
//        assertEquals("Issue.getKey()", "SWIZZLE-4", issue.getKey());
//        assertEquals("Issue.getReporter()", "dblevins", issue.getReporter().getName());
//        assertEquals("Issue.getProject()", "SWIZZLE", issue.getProject().getKey());
//        assertEquals("Issue.getResolution()", 1, issue.getResolution().getId());
//
//        issue = (Issue) subTasks.get(2);
//        assertEquals("Issue.getSummary()", "a stick of butter", issue.getSummary());
//        assertEquals("Issue.getType()", 7, issue.getType().getId());
//        assertEquals("Issue.getStatus()", 6, issue.getStatus().getId());
//        assertEquals("Issue.getId()", 40914, issue.getId());
//        assertEquals("Issue.getKey()", "SWIZZLE-5", issue.getKey());
//        assertEquals("Issue.getReporter()", "dblevins", issue.getReporter().getName());
//        assertEquals("Issue.getProject()", "SWIZZLE", issue.getProject().getKey());
//        assertEquals("Issue.getResolution()", 1, issue.getResolution().getId());

    }

    public void testJiraRss() throws Exception {
        JiraRss jira = new JiraRss("http://jira.codehaus.org/browse/SWIZZLE-2?decorator=none&view=rss");
        jira.fillSubTasks();

        Issue issue = jira.getIssue("SWIZZLE-2");
        // assertEquals("Issue.getCreated()", "Sun Aug 27 18:23:40 PDT 2006",
        // issue.getCreated().toString());
        assertEquals("Issue.getSummary()", "Need Wilhemina to get some things from the store", issue.getSummary());
        assertEquals("Issue.getType()", 3, issue.getType().getId());
        assertEquals("Issue.getEnvironment()", "", issue.getEnvironment());
        assertEquals("Issue.getStatus()", 6, issue.getStatus().getId());
        // assertEquals("Issue.getUpdated()", "Sun Aug 27 18:27:49 PDT 2006",
        // issue.getUpdated().toString());
        assertEquals("Issue.getId()", 40911, issue.getId());
        assertEquals("Issue.getKey()", "SWIZZLE-2", issue.getKey());
        assertEquals("Issue.getDescription()", "", issue.getDescription());
        assertEquals("Issue.getReporter()", "dblevins", issue.getReporter().getName());
        assertEquals("Issue.getProject()", "SWIZZLE", issue.getProject().getKey());
        assertEquals("Issue.getResolution()", 1, issue.getResolution().getId());
        assertEquals("Issue.getVotes()", 0, issue.getVotes());
        assertEquals("Issue.getAssignee()", "dblevins", issue.getAssignee().getName());
        assertEquals("Issue.getPriority()", 3, issue.getPriority().getId());

        List subTasks = issue.getSubTasks();
        assertNotNull("subtasks", subTasks);
        assertTrue("three subtasks", subTasks.size() == 3);

        issue = (Issue) subTasks.get(0);
        assertEquals("Issue.getSummary()", "a loaf of bread", issue.getSummary());
        assertEquals("Issue.getType()", 7, issue.getType().getId());
        assertEquals("Issue.getStatus()", 6, issue.getStatus().getId());
        assertEquals("Issue.getId()", 40912, issue.getId());
        assertEquals("Issue.getKey()", "SWIZZLE-3", issue.getKey());
        assertEquals("Issue.getReporter()", "dblevins", issue.getReporter().getName());
        assertEquals("Issue.getProject()", "SWIZZLE", issue.getProject().getKey());
        assertEquals("Issue.getResolution()", 1, issue.getResolution().getId());

        issue = (Issue) subTasks.get(1);
        assertEquals("Issue.getSummary()", "a container of milk", issue.getSummary());
        assertEquals("Issue.getType()", 7, issue.getType().getId());
        assertEquals("Issue.getStatus()", 6, issue.getStatus().getId());
        assertEquals("Issue.getId()", 40913, issue.getId());
        assertEquals("Issue.getKey()", "SWIZZLE-4", issue.getKey());
        assertEquals("Issue.getReporter()", "dblevins", issue.getReporter().getName());
        assertEquals("Issue.getProject()", "SWIZZLE", issue.getProject().getKey());
        assertEquals("Issue.getResolution()", 1, issue.getResolution().getId());

        issue = (Issue) subTasks.get(2);
        assertEquals("Issue.getSummary()", "a stick of butter", issue.getSummary());
        assertEquals("Issue.getType()", 7, issue.getType().getId());
        assertEquals("Issue.getStatus()", 6, issue.getStatus().getId());
        assertEquals("Issue.getId()", 40914, issue.getId());
        assertEquals("Issue.getKey()", "SWIZZLE-5", issue.getKey());
        assertEquals("Issue.getReporter()", "dblevins", issue.getReporter().getName());
        assertEquals("Issue.getProject()", "SWIZZLE", issue.getProject().getKey());
        assertEquals("Issue.getResolution()", 1, issue.getResolution().getId());

    }
}
