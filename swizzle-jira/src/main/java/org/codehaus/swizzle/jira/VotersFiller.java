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

import org.codehaus.swizzle.stream.IncludeFilterInputStream;
import org.codehaus.swizzle.stream.DelimitedTokenReplacementInputStream;
import org.codehaus.swizzle.stream.StringTokenHandler;

import java.net.URL;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.IOException;

/**
 * @version $Revision$ $Date$
 */
public class VotersFiller implements IssueFiller {
    private final Jira jira;
    private boolean enabled;

    public VotersFiller(Jira jira) {
        this.jira = jira;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void fill(final Issue issue) {
        if (!enabled){
            return;
        }

        try {
            Project project = jira.getProject(issue.getProject().getKey());
            ServerInfo serverInfo = jira.getServerInfo();

            URL baseUrl = new URL(serverInfo.getBaseUrl());
            URL url = new URL(baseUrl, "secure/ViewVoters!default.jspa?id="+project.getId());

            InputStream in = new BufferedInputStream(url.openStream());
            in = new IncludeFilterInputStream(in, "<a id=\"voter_link","/a>");
            in = new DelimitedTokenReplacementInputStream(in, "name=","<", new StringTokenHandler(){
                public String handleToken(String token) throws IOException {
                    String[] s = token.split("\">");
                    try {
                        User user = jira.getUser(s[0]);
                        if (user != null){
                            issue.getVoters().add(user);
                        }
                    } catch (Exception e) {
                        System.err.println("Bad voter string: "+token);
                    }
                    return "";
                }
            });

            int i = in.read();
            while (i != -1){
                i = in.read();
            }
            in.close();
        } catch (IOException e) {
            System.err.println(e.getClass().getName()+": "+e.getMessage());
        }
    }

}
