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

import org.codehaus.swizzle.jira.Attachment;
import org.codehaus.swizzle.jira.Issue;
import org.codehaus.swizzle.jira.Jira;

import junit.framework.TestCase;

import java.io.File;
import java.util.List;

/**
 * @version $Revision$ $Date$
 */
public class AttachmentsUtilTest extends SwizzleJiraTestCase {
    public void testSaveAttachment() throws Exception {
        AttachmentsUtil attachmentsUtil = new AttachmentsUtil();

        Jira jira = getJira();
        jira.autofill("attachments", true);

        Issue issue = jira.getIssue("SWIZZLE-13");
        assertNotNull("issue", issue);
        assertEquals("Issue.getKey()", "SWIZZLE-13", issue.getKey());

        List attachments = issue.getAttachments();
        assertNotNull("attachments", attachments);
        assertEquals("attachments.size()", 3, attachments.size());

        Attachment attachment = (Attachment) attachments.get(0);
        assertEquals("Attachment.getFileName()", "ONE.patch", attachment.getFileName());
        assertEquals("Attachment.getUrl()", "https://jira.codehaus.org/secure/attachment/22972/ONE.patch", attachment.getUrl().toExternalForm());

        File file = attachmentsUtil.saveAttachment(attachment);
        assertEquals("File.exists()", true, file.exists());
        assertEquals("File.getName()", "ONE.patch", file.getName());
        assertEquals("File.length()", 9, file.length());
    }
}
