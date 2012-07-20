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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @version $Revision$ $Date$
 */
public class AttachmentsTest extends SwizzleJiraTestCase {

    // Date is retrieved without timezone from xmlrpc (@codehaus?)
    SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");

    public void testJira() throws Exception {
        Jira jira = new Jira( "https://issues.apache.org/jira" );
        jira.login( "dblevins", "Sn0wmanj" );
        jira.autofill("attachments", true);

        Issue issue = jira.getIssue("OPENEJB-1636");
//        assertNotNull("issue", issue);
//        assertEquals("Issue.getSummary()", "Test issue with attachments", issue.getSummary());
//        assertEquals("Issue.getType()", 1, issue.getType().getId());
//        assertEquals("Issue.getKey()", "SWIZZLE-13", issue.getKey());
//        assertEquals("Issue.getLink()", "http://jira.codehaus.org/browse/SWIZZLE-13", issue.getLink());

        List attachments = issue.getAttachments();
//        assertNotNull("attachments", attachments);
//        assertEquals("attachments.size()", 3, attachments.size());

        Attachment attachment;
        attachment = (Attachment) attachments.get(0);
        final Date created = attachment.getCreated();

    }

}
