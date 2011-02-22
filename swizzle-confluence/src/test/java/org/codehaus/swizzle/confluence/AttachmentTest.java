/**
 *
 f * Copyright 2006 David Blevins
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
package org.codehaus.swizzle.confluence;

import java.util.HashMap;

/**
 * @version $Revision$ $Date$
 */
public class AttachmentTest extends SwizzleConfluenceTestCase {

  public void testNothing(){
  }

  // WE CANNOT TEST ANYTHING. PERMISSIONS ISSUE WITH CODEHAUS INSTANCE for swizzle account.
  public void _testAttachment() throws Exception {
    Confluence confluence = getConfluence();
    Page page = getNewTestPage();

    Attachment attachment = new Attachment(new HashMap());
    attachment.setComment("This is a test to attach a document from Swizzle");
    attachment.setContentType("text/plain");
    attachment.setFileName("test.txt");
    try {
      confluence.addAttachment(Long.parseLong(page.getId()), attachment, "This is a test".getBytes());
    } finally {
      // Remove the test page
      confluence.removePage(page.getId());
    }
  }

}
