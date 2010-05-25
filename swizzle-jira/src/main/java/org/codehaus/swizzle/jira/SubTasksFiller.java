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

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
        if (!enabled) {
            return;
        }

        // Subtasks can't have subtasks, so we can skip this one
        if (issue.getParentTask() != null) return;

        fillSubtasks(issue, new JiraResolver(jira));
    }

    public static void main(String[] args) throws Exception {
//        JiraRss jiraRss = new JiraRss("https://jira.codehaus.org/si/jira.issueviews:issue-xml/OPENEJB-90/OPENEJB-90.xml");
        JiraRss jiraRss = new JiraRss("https://issues.apache.org/jira/si/jira.issueviews:issue-xml/OPENEJB-1133/OPENEJB-1133.xml");
//        JiraRss jiraRss = new JiraRss("https://issues.apache.org/jira/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml?&pid=12310530&component=12313252");
        fill(jiraRss);
    }

    public static List fill(JiraRss jiraRss) throws Exception {
        MapObjectList<Issue> issues = (MapObjectList<Issue>) jiraRss.getIssues();
        issues = issues.ascending("id");

        RssResolver rssResolver = new RssResolver();

        for (int i = 0; i < issues.size(); i++) {
            Issue issue = (Issue) issues.get(i);
            fillSubtasks(issue, rssResolver);
        }

        return issues;
    }

    public static void fillSubtasks(Issue parent, Resolver resolver) {
        // Subtasks can't have subtasks, so we can skip this one
        if (parent.getParentTask() != null) return;

        List subtasks = parent.getSubTasks();
        List<Issue> replacements = new ArrayList<Issue>();

        Iterator<Issue> it = subtasks.iterator();
        while (it.hasNext()) {
            Issue subtask = it.next();
            if (subtask instanceof IssueRef) {
                Issue full = resolver.getIssue(parent, subtask.getKey());
                if (full != null) {
                    it.remove();
                    full.setParentTask(parent);
                    replacements.add(full);
                }
            }
        }

        subtasks.addAll(replacements);
    }

    public static interface Resolver {
        Issue getIssue(Issue parent, String key);
    }

    public static class JiraRssResolver implements Resolver {
        private final JiraRss jiraRss;

        public JiraRssResolver(JiraRss jiraRss) {
            this.jiraRss = jiraRss;
        }

        public Issue getIssue(Issue parent, String key) {
            return jiraRss.getIssue(key);
        }
    }

    public static class JiraResolver implements Resolver {
        private final Jira jira;

        public JiraResolver(Jira jira) {
            this.jira = jira;
        }

        public Issue getIssue(Issue parent, String key) {
            return jira.getIssue(key);
        }
    }

    public static class RssResolver implements Resolver {

        public Issue getIssue(Issue parent, String key) {
            try {
                // turn a into b
                // a = https://issues.apache.org/jira/browse/OPENEJB-112
                // b = https://issues.apache.org/jira/si/jira.issueviews:issue-xml/OPENEJB-114/OPENEJB-114.xml

                String link = parent.getLink();
                URL base = new URL(link.replaceAll("/browse/.*", "/"));

                URL rssURL = new URL(base, "si/jira.issueviews:issue-xml/" + key + "/" + key + ".xml");
                JiraRss jiraRss = new JiraRss(rssURL);
                return jiraRss.getIssue(key);
            } catch (Exception e) {
                throw new RuntimeException("Unable to get issue " + key + " via rss", e);
            }
        }
    }
}
