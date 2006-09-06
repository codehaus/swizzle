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

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.Template;
import org.codehaus.swizzle.jira.JiraRss;
import org.codehaus.swizzle.jira.Jira;

import java.io.PrintWriter;
import java.io.PrintStream;
import java.util.Map;
import java.util.Iterator;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.text.SimpleDateFormat;

/**
 * @version $Revision$ $Date$
 */
public class Main {
    public static void main(String[] args) throws Exception {
        List newargs = new ArrayList();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("-D")) {
                String prop = arg.substring(arg.indexOf("-D") + 2, arg.indexOf("="));
                String val = arg.substring(arg.indexOf("=") + 1);

                System.setProperty(prop, val);
            } else {
                newargs.add(arg);
            }
        }

        args = (String[]) newargs.toArray(new String[]{});

        if (args.length > 0){
            System.setProperty("template", args[0]);
        }

        String templateName = System.getProperty("template","report.vm");

        generate(templateName, System.out);
    }

    public static void generate(String templateName, PrintStream result) throws Exception {
        VelocityContext context = new VelocityContext();
        generate(context, templateName, result);
    }

    public static void generate(VelocityContext context, String templateName, PrintStream result) throws Exception {
        List keys = Arrays.asList(context.getKeys());

        for (Iterator iterator = System.getProperties().entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String name = (String) entry.getKey();
            String value = (String) entry.getValue();

            // Don't overwrite anything explicitly added to the context.
            if (!keys.contains(name)){
                context.put(name, value);
            }
        }

        context.put("rss", new Rss());
        context.put("xmlrpc", new Xmlrpc());
        context.put("date", new DateUtil(System.getProperty("dateFormat", "yyyy-MM-dd")));
        VelocityEngine velocity = new VelocityEngine();
        velocity.setProperty(Velocity.RESOURCE_LOADER, "all");
        velocity.setProperty("all.resource.loader.class", FederatedResourceLoader.class.getName());
        velocity.init();

        Template template = velocity.getTemplate(templateName);


        PrintWriter writer = new PrintWriter(result);
        template.merge(context, writer);
        writer.flush();
    }

    public static class Rss {
        public JiraRss fetch(String url) throws Exception {
            return new JiraRss(url);
        }
    }

    public static class Xmlrpc {
        public Jira connect(String user, String url) throws Exception {
            String[] strings = user.split(":");
            Jira jira = new Jira(url);
            jira.login(strings[0], strings[1]);
            return jira;
        }
    }

    public static class DateUtil {
        private SimpleDateFormat dateFormat;
        private Date now = new Date();

        public DateUtil(String format) throws Exception {
            dateFormat = new SimpleDateFormat(format);
        }

        public String format(String format){
            dateFormat = new SimpleDateFormat(format);
            return dateFormat.format(now);
        }

        public String as(String format){
            SimpleDateFormat dateFormat = new SimpleDateFormat(format);
            return dateFormat.format(now);
        }

        public String toString() {
            return dateFormat.format(now);
        }
    }
}
