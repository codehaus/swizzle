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
public class AttachmentsTest extends TestCase {

    // Date is retrieved without timezone from xmlrpc (@codehaus?)
    SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");

    public void testJira() throws Exception {
        Jira jira = new Jira("http://jira.codehaus.org/rpc/xmlrpc");
        jira.login("swizzletester", "swizzle");
        jira.autofill("attachments", true);

        Issue issue = jira.getIssue("SWIZZLE-13");
        assertNotNull("issue", issue);
        assertEquals("Issue.getSummary()", "Test issue with attachments", issue.getSummary());
        assertEquals("Issue.getType()", 1, issue.getType().getId());
        assertEquals("Issue.getKey()", "SWIZZLE-13", issue.getKey());
        assertEquals("Issue.getLink()", "http://jira.codehaus.org/browse/SWIZZLE-13", issue.getLink());

        List attachments = issue.getAttachments();
        assertNotNull("attachments", attachments);
        assertEquals("attachments.size()", 3, attachments.size());

        Attachment attachment;
        attachment = (Attachment) attachments.get(0);
        assertEquals("Attachment.getFileName()", "ONE.patch", attachment.getFileName());
        assertEquals("Attachment.getUrl()", "http://jira.codehaus.org/secure/attachment/22972/ONE.patch", attachment
                .getUrl().toExternalForm());
        assertEquals("Attachment.getId()", 22972, attachment.getId());
        assertEquals("Attachment.getAuthor()", "David Blevins", attachment.getAuthor());
        assertEquals("Attachment.getCreated()", "Tue Sep 19 22:42:00 2006", formatter.format(attachment.getCreated()));

        attachment = (Attachment) attachments.get(1);
        assertEquals("Attachment.getFileName()", "THREE.tar.gz", attachment.getFileName());
        assertEquals("Attachment.getUrl()", "http://jira.codehaus.org/secure/attachment/22975/THREE.tar.gz", attachment
                .getUrl().toExternalForm());
        assertEquals("Attachment.getId()", 22975, attachment.getId());
        assertEquals("Attachment.getAuthor()", "David Blevins", attachment.getAuthor());
        assertEquals("Attachment.getCreated()", "Tue Sep 19 22:43:00 2006", formatter.format(attachment.getCreated()));

        attachment = (Attachment) attachments.get(2);
        assertEquals("Attachment.getFileName()", "TWO.txt", attachment.getFileName());
        assertEquals("Attachment.getUrl()", "http://jira.codehaus.org/secure/attachment/22973/TWO.txt", attachment
                .getUrl().toExternalForm());
        assertEquals("Attachment.getId()", 22973, attachment.getId());
        assertEquals("Attachment.getAuthor()", "David Blevins", attachment.getAuthor());
        assertEquals("Attachment.getCreated()", "Tue Sep 19 22:42:00 2006", formatter.format(attachment.getCreated()));

    }

}
