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
package org.codehaus.swizzle;

import org.codehaus.swizzle.confluence.Confluence;
import org.codehaus.swizzle.confluence.Page;

public class UpdatePageExample {
    public static void main(String[] args) throws Exception {
        String username = "david";
        String password = "snarf";
        String endpoint = "http://docs.codehaus.org/rpc/xmlrpc";

        Confluence confluence = new Confluence(endpoint);
        confluence.login(username, password);

        Page page = confluence.getPage("SWIZZLE", "Test Page");

        page.setContent("This is the new content for the the test");

        confluence.storePage(page);

        confluence.logout();
    }
}
