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

import org.codehaus.swizzle.stream.DelimitedTokenReplacementInputStream;
import org.codehaus.swizzle.stream.StringTokenHandler;
import org.codehaus.swizzle.stream.ReplaceStringInputStream;
import org.codehaus.swizzle.stream.StreamTokenHandler;
import org.codehaus.swizzle.stream.IncludeFilterInputStream;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.net.URL;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.IOException;

/**
 * @version $Revision$ $Date$
 */
public class SubTasksFiller implements IssueFiller {
    private final Jira jira;
    private boolean enabled;

    public SubTasksFiller(Jira jira) {
        this.jira = jira;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void fill(final Issue issue) {
        if (!enabled){
            return;
        }

        List issueKeys = getSubTasks(issue);
        issueKeys.remove(issue.getKey()); // just in case of some freak accident
        for (int i = 0; i < issueKeys.size(); i++) {
            String issueKey = (String) issueKeys.get(i);
            Issue subTask = jira.getIssue(issueKey);
            issue.getSubTasks().add(subTask);
        }
    }

    public static void main(String[] args) throws Exception {
        JiraRss jiraRss = new JiraRss("http://jira.codehaus.org/browse/OPENEJB-90?decorator=none&view=rss");
        fill(jiraRss);
    }
    public static List fill(JiraRss jiraRss) throws Exception {
        SubTasksFiller filler = new SubTasksFiller(null);
        List issues = jiraRss.getIssues();
        for (int i = 0; i < issues.size(); i++) {
            Issue issue = (Issue) issues.get(i);
            String link = issue.getLink();
            link = link.replaceFirst("/browse/.*$", "/");
            List issueKeys = filler.getSubTasks(issue);

            issueKeys.remove(issue.getKey()); // just in case of some freak accident

            for (int j = 0; j < issueKeys.size(); j++) {
                String issueKey = (String) issueKeys.get(j);
                URL issueRssUrl = new URL(link +"browse/"+ issueKey + "?decorator=none&view=rss");
                JiraRss subtaskJiraRss = new JiraRss(issueRssUrl);
                Issue subTask = subtaskJiraRss.getIssue(issueKey);
                if (subTask != null) {
                    issue.getSubTasks().add(subTask);
                }
            }
        }
        return issues;
    }

    private List getSubTasks(final Issue issue) {
        try {
            URL url = new URL(issue.getLink()+"?subTaskView=all");

            ArrayList issueIds = new ArrayList();

            InputStream in = new BufferedInputStream(url.openStream());
            in = new ReplaceStringInputStream(in, " ","");
            in = new ReplaceStringInputStream(in, "\t","");
            in = new ReplaceStringInputStream(in, "\n","");
            in = new ReplaceStringInputStream(in, "\r","");
            in = new ReplaceStringInputStream(in, "<tr","\n<tr");
            in = new ReplaceStringInputStream(in, "</tr>","</tr>\n");
            in = new GrepStream(in, "issue_subtask.gif");
            in = new IncludeFilterInputStream(in, "<ahref",">");
            in = new DelimitedTokenReplacementInputStream(in, "browse/","\"", new CollectTokensHandler(issueIds));

            int i = in.read();
            while (i != -1){
                i = in.read();
//                System.out.print((char)i);
            }
            in.close();

            return issueIds;
        } catch (IOException e) {
            System.err.println(e.getClass().getName()+": "+e.getMessage());
        }
        return new ArrayList();
    }

    public static class GrepStream extends DelimitedTokenReplacementInputStream {
        public GrepStream(InputStream in, String match, String lineTerminator) {
            super(in, "<", lineTerminator, new LineMatcher(match, lineTerminator));

        }

        public GrepStream(InputStream in, String match) {
            this(in, match, "\n");
        }

        public GrepStream(InputStream in, String begin, String end, StreamTokenHandler tokenHandler, boolean caseSensitive) {
            super(in, begin, end, tokenHandler, caseSensitive);
        }

        public static class LineMatcher extends StringTokenHandler {
            private final String match;
            private final String lineTerminator;

            public LineMatcher(String match, String lineTerminator) {
                this.match = match;
                this.lineTerminator = lineTerminator;
            }

            public String handleToken(String token) throws IOException {
                if (token.indexOf(match) == -1){
                    return "";
                }
                return "<"+token + lineTerminator;
            }
        }
    }

    public static class CollectTokensHandler extends StringTokenHandler {
        private final Collection collection;

        public CollectTokensHandler(Collection collection) {
            this.collection = collection;
        }

        public String handleToken(String token) throws IOException {
            collection.add(token);
            return token;
        }
    }
}
