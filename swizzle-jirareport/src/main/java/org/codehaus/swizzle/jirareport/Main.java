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

import org.codehaus.swizzle.jira.Jira;
import org.codehaus.swizzle.jira.JiraRss;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @version $Revision$ $Date$
 */
public class Main {
    public static void main(String[] args) throws Throwable {
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

        args = (String[]) newargs.toArray(new String[] {});

        if (args.length == 0 && System.getProperty("export") != null) {
            String template = System.getProperty("export");
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            InputStream in = cl.getResourceAsStream(template);
            in = new BufferedInputStream(in);
            int i = in.read();
            while (i != -1) {
                System.out.write(i);
                i = in.read();
            }
            in.close();
            return;
        } else if (args.length > 0) {
            System.setProperty("template", args[0]);
        }

        String templateName = System.getProperty("template", "report.vm");

        try {
            generate(templateName, System.out);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        } catch (MethodInvocationException e) {
            Throwable wrappedThrowable = e.getWrappedThrowable();
            if (wrappedThrowable instanceof MissingParamsException) {
                MissingParamsException missingParamsException = (MissingParamsException) wrappedThrowable;
                Param[] missingArgs = missingParamsException.getParams();

                System.err.println("Invalid or missing arguments.");

                for (int i = 0; i < missingArgs.length; i++) {
                    Param param = (Param) missingArgs[i];
                    System.err.print("  [");
                    System.err.print(param.getStatus());
                    System.err.print("]    ");
                    System.err.print(param.getName());
                    System.err.print("    : ");
                    System.err.print(param.getDescription());
                    System.err.print(". Must Match Pattern '");
                    System.err.print(param.getRegex());
                    System.err.println("'");
                }
                System.exit(1);
            } else {
                throw e;
            }
        }
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
            if (!keys.contains(name)) {
                context.put(name, value);
            }
        }

        context.put("rss", new Rss());
        context.put("xmlrpc", new Xmlrpc());
        context.put("arrays", new ArraysUtil());
        context.put("collections", new CollectionsUtil());
        context.put("strings", new StringsUtil());
        context.put("utils", new UtilsUtil());
        context.put("params", new ParamsUtil(context));
        context.put("date", new DateUtil(System.getProperty("dateFormat", "yyyy-MM-dd")));
        VelocityEngine velocity = new VelocityEngine();
        velocity.setProperty(Velocity.RESOURCE_LOADER, "all");
        velocity.setProperty("all.resource.loader.class", FederatedResourceLoader.class.getName());
        velocity.init();

        Template template = velocity.getTemplate(templateName);

        try {
            PrintWriter writer = new PrintWriter(result);
            template.merge(context, writer);
            writer.flush();
        } catch (MethodInvocationException e) {
            Throwable cause = e.getWrappedThrowable();
            if (cause instanceof Exception) throw (Exception) cause;
            if (cause instanceof Error) throw (Error) cause;
            throw e;
        }
    }

    public static class UtilsUtil {
        public Object load(String classname) throws Exception {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Class clazz = cl.loadClass(classname);
            return clazz.newInstance();
        }
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

}
