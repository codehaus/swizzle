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
package org.codehaus.swizzle.jiramacro;

import org.codehaus.swizzle.jira.Issue;

import com.atlassian.confluence.renderer.radeox.macros.AbstractHtmlGeneratingMacro;
import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.util.velocity.VelocityUtils;
import org.radeox.macro.parameter.MacroParameter;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @version $Revision$ $Date$
 */
public class SwizzleJiraIssuesMacro extends AbstractHtmlGeneratingMacro {

    private static final Map<String, String> iconMapping;

    static {
        iconMapping = new HashMap<String, String>();

        iconMapping.put("Bug", "bug.gif");
        iconMapping.put("New Feature", "newfeature.gif");
        iconMapping.put("Task", "task.gif");
        iconMapping.put("Sub-task", "issue_subtask.gif");
        iconMapping.put("Improvement", "improvement.gif");

        iconMapping.put("Blocker", "priority_blocker.gif");
        iconMapping.put("Critical", "priority_critical.gif");
        iconMapping.put("Major", "priority_major.gif");
        iconMapping.put("Minor", "priority_minor.gif");
        iconMapping.put("Trivial", "priority_trivial.gif");

        iconMapping.put("Assigned", "status_assigned.gif");
        iconMapping.put("Closed", "status_closed.gif");
        iconMapping.put("In Progress", "status_inprogress.gif");
        iconMapping.put("Need Info", "status_needinfo.gif");
        iconMapping.put("Open", "status_open.gif");
        iconMapping.put("Reopened", "status_reopened.gif");
        iconMapping.put("Resolved", "status_resolved.gif");
        iconMapping.put("Unassigned", "status_unassigned.gif");
    }

    private final Map columnsMap;

    public SwizzleJiraIssuesMacro() {
        columnsMap = new LinkedHashMap();
        columnsMap.put("type", "T");
        columnsMap.put("key", "Key");
        columnsMap.put("summary", "Summary");
        columnsMap.put("assignee", "Assignee");
        columnsMap.put("reporter", "Reporter");
        columnsMap.put("priority", "Pr");
        columnsMap.put("status", "Status");
        columnsMap.put("resolution", "Res");
        columnsMap.put("created", "Created");
        columnsMap.put("updated", "Updated");
        columnsMap.put("due", "Due");
        columnsMap.put("votes", "Votes");
    }

    public String getHtml(MacroParameter macroParameter) throws IllegalArgumentException, IOException {
        String issuesRef = (String) macroParameter.getParams().get("issues");
        String columnsString = (String) macroParameter.getParams().get("columns");
        String title = (String) macroParameter.getParams().get("title");
        String style = (String) macroParameter.getParams().get("style") + "";

        String template = "swizzlejiraissues.vm";
        if (style.equals("progress")) {
            template = "swizzlejiraissues-progress.vm";
        }

        List issues = IssuesUtil.getIssues(issuesRef);
        if (issues == null) {
            return "No issues specified.  Usage {swizzlejiraissues:issues=$as.param($issues)}";
        }

        String iconsUrl = null;
        if (issues.size() > 0) {
            Issue issue = (Issue) issues.get(0);
            String link = issue.getLink();
            iconsUrl = link.replaceFirst("/browse/.*$", "/images/icons/");
            if (title == null) {
                URL url = new URL(issue.getLink());
                title = url.getHost();
            }
        }

        if (title == null) {
            title = "issues";
        }

        Map columns = new LinkedHashMap();
        if (columnsString != null && columnsString.length() > 0) {
            String[] columnNames = columnsString.split(" *; *");
            for (int i = 0; i < columnNames.length; i++) {

                String columnName = columnNames[i];
                if (columnsMap.containsKey(columnName)) {
                    columns.put(columnName, columnsMap.get(columnName.toLowerCase()));
                }
            }
        } else {
            columns = columnsMap;
        }

        Map context = MacroUtils.defaultVelocityContext();

        for (Iterator iterator = columns.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            Object key = entry.getKey();
            Object value = entry.getValue();
            System.out.println("columns entry: " + key + "=" + value);
        }

        context.put("columns", columns);
        context.put("issues", issues);
        context.put("clickableUrl", "");
        context.put("title", title);
        context.put("icons", prepareIconMap(iconsUrl));

        return VelocityUtils.getRenderedTemplate("swizzle/jira/templates/" + template, context);
    }

    private Map prepareIconMap(String iconsUrl) {
        Map map = new HashMap();

        Map<String,String> iconMappings = new HashMap<String, String>(iconMapping);
        for (Map.Entry<String, String> entry : iconMappings.entrySet()) {
            String key = entry.getKey();
            String icon = entry.getValue();
            icon = (icon.matches("https?://.*")) ? icon : iconsUrl + icon;
            map.put(key, icon);
        }

        map.put("spacer", iconsUrl.replace("images/icons/", "images/border/spacer.gif"));

        return map;
    }

    public String getName() {
        return "swizzlejiraissues";
    }

}
