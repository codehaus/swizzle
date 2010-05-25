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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import java.io.InputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Iterator;
import java.util.ArrayList;

/**
 * @version $Revision$ $Date$
 */
public class JiraRss {
    private static final Map autofillProviders = new HashMap();
    static {
        autofillProviders.put("voters", "org.codehaus.swizzle.jira.VotersFiller");
        autofillProviders.put("subtasks", "org.codehaus.swizzle.jira.SubTasksFiller");
        autofillProviders.put("attachments", "org.codehaus.swizzle.jira.AttachmentsFiller");
    }

    private Map<String, Issue> issues = new HashMap<String, Issue>();
    private URL url;

    public JiraRss(String query) throws Exception {
        this(new URL(query));
    }

    public JiraRss(URL url) throws Exception {
        this(openStream(url));
        this.url = url;
    }

    public JiraRss(InputStream in) throws Exception {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        SAXParser saxParser = saxParserFactory.newSAXParser();
        ObjectBuilder objectBuilder = new ObjectBuilder();

        saxParser.parse(in, objectBuilder);

        List<Issue> list = objectBuilder.getIssues();
        for (Issue issue : list) {
            issues.put(issue.getKey(), issue);

            try {
                // Fix: the project name isn't in the RSS feed
                String project = issue.getKey().split("-")[0];
                issue.setString("project", project);
            } catch (Exception dontCare) {
            }
        }

        SubTasksFiller.JiraRssResolver existingIssues = new SubTasksFiller.JiraRssResolver(this);
        for (Issue issue : list) {
            SubTasksFiller.fillSubtasks(issue, existingIssues);
        }
    }

    private static InputStream openStream(URL url) throws IOException {
        URLConnection urlConnection = url.openConnection();
        if (urlConnection instanceof HttpURLConnection) {
            HttpURLConnection httpConnection = (HttpURLConnection) urlConnection;
            int code = httpConnection.getResponseCode();
            if (code == 301 || code == 302) {
                String location = httpConnection.getHeaderField("Location");
                if (location != null) {
                    URL redirect = new URL(url, location);
                    return openStream(redirect);
                }
            }
        }
        return urlConnection.getInputStream();
    }


    /**
     * Valid schemes are "issue", "project", "voters", and "attachments" "issues" is enabled by default
     * 
     * @param scheme
     */
    public void autofill(String scheme) {
        if (!autofillProviders.containsKey(scheme)) {
            throw new UnsupportedOperationException("Autofill Scheme not supported: " + scheme);
        }

        try {
            String className = (String) autofillProviders.get(scheme);
            List<Issue> list = fill(className);
            for (Issue issue : list) {
                issues.put(issue.getKey(), issue);
            }
        } catch (Exception e) {
            System.err.println("Cannot install autofill provider " + scheme);
            e.printStackTrace();
        }
    }

    public List<Issue> fillVotes() throws Exception {
        return fill("org.codehaus.swizzle.jira.VotersFiller");
    }

    public List<Issue> fillSubTasks() throws Exception {
        return fill("org.codehaus.swizzle.jira.SubTasksFiller");
    }

    private List<Issue> fill(String className) throws Exception {
        ClassLoader classLoader = this.getClass().getClassLoader();
        Class clazz = classLoader.loadClass(className);
        Method fill = clazz.getMethod("fill", JiraRss.class);
        try {
            return (List) fill.invoke(null, new Object[] {this});
        } catch (Exception e) {
            if (e instanceof InvocationTargetException) {
                Throwable cause = e.getCause();
                if (cause instanceof Exception) throw (Exception) cause;
                if (cause instanceof Error) throw (Error) cause;
            }
            throw e;
        }
    }

    public List<Issue> fillAttachments() throws Exception {
        autofill("attachments");
        return getIssues();
    }

    public List<Issue> getIssues() {
        return new MapObjectList<Issue>(issues.values());
    }

    public Issue getIssue(String key) {
        return (Issue) issues.get(key);
    }

    private class ObjectBuilder extends DefaultHandler {
        private Map handlers = new HashMap();
        private Stack<DefaultHandler> handlerStack = new Stack<DefaultHandler>();
        private Channel channel;

        public ObjectBuilder() {
            // channelHandler = new MapObjectHandler(Channel.class);
            TextHandler textHandler = new TextHandler();
            // this.registerHandler("channel", channelHandler);
            this.registerHandler("item", new MapObjectListHandler(Issue.class, null));
            this.registerHandler("priority", new MapObjectHandler(Priority.class));
            this.registerHandler("status", new MapObjectHandler(Status.class));
            this.registerHandler("type", new MapObjectHandler(IssueType.class));
            this.registerHandler("resolution", new MapObjectHandler(Resolution.class));
            this.registerHandler("fixVersion", new MapObjectListHandler(Version.class));
            this.registerHandler("affectsVersion", new MapObjectListHandler(Version.class));
            this.registerHandler("subtask", new SubtaskHanlder());
            this.registerHandler("assignee", new UserHandler());
            this.registerHandler("reporter", new UserHandler());
            this.registerHandler("component", new MapObjectListHandler(Component.class));
            this.registerHandler("comment", new CommentHandler());
            this.registerHandler("title", textHandler);
            this.registerHandler("link", textHandler);
            this.registerHandler("description", textHandler);
            this.registerHandler("environment", textHandler);
            this.registerHandler("summary", textHandler);
            this.registerHandler("created", textHandler);
            this.registerHandler("updated", textHandler);
            this.registerHandler("votes", textHandler);
            this.registerHandler("due", new TextHandler("duedate"));
            this.registerHandler("key", new KeyHandler());
            channel = new Channel();
            objects.push(channel);
        }

        public void registerHandler(String name, Object handler) {
            handlers.put(name, handler);
        }

        public List<Issue> getIssues() {
            return channel.getIssues();
        }

        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            DefaultHandler handler = createHandler(qName);
            handlerStack.push(handler);
            handler.startElement(uri, localName, qName, attributes);
        }

        public void characters(char[] chars, int i, int i1) throws SAXException {
            DefaultHandler handler = handlerStack.peek();
            handler.characters(chars, i, i1);
        }

        public void endElement(String string, String string1, String string2) throws SAXException {
            DefaultHandler handler = handlerStack.pop();
            handler.endElement(string, string1, string2);
        }

        private DefaultHandler createHandler(String qName) {
            Object object = handlers.get(qName);

            if (object == null) return new DefaultHandler();

            if (object instanceof DefaultHandler) {
                try {
                    DefaultHandler handler = (DefaultHandler) object;
                    return (DefaultHandler) handler.clone();
                } catch (CloneNotSupportedException e) {
                    throw new RuntimeException(e);
                }
            }

            if (object instanceof Class) {
                Class handlerClass = (Class) object;
                try {
                    return (DefaultHandler) handlerClass.newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            throw new IllegalStateException("Unknown handler type " + object.getClass().getName());
        }
    }

    public static class Channel extends MapObject {
        public Channel() {
            super(new HashMap());
        }

        public Channel(Map data) {
            super(data);
        }

        public List<Issue> getIssues() {
            return getMapObjects("items", Issue.class);
        }
    }

    private Stack<MapObject> objects = new Stack<MapObject>();

    public class DefaultHandler extends org.xml.sax.helpers.DefaultHandler implements Cloneable {
        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
    }

    public class TextHandler extends DefaultHandler {

        protected StringBuffer value = new StringBuffer();
        protected String name;

        public TextHandler() {
        }

        public TextHandler(String name) {
            this.name = name;
        }

        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (name == null) name = qName;
        }

        public void characters(char[] chars, int i, int i1) throws SAXException {
            value.append(chars, i, i1);
        }

        public void endElement(String string, String string1, String string2) throws SAXException {
            MapObject status = objects.peek();
            String text = value.toString();
            text = text.replaceAll("^<p>|</p>$", "");
            status.setString(name, text);
        }

        protected Object clone() {
            return new TextHandler(name);
        }
    }

    public class KeyHandler extends TextHandler {
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            MapObject status = objects.peek();
            status.setString("id", attributes.getValue("id"));
            super.startElement(uri, localName, qName, attributes);
        }

        protected Object clone() {
            return new KeyHandler();
        }
    }

    public class MapObjectHandler<T extends MapObject> extends DefaultHandler {
        protected Map<String, String> atts = new HashMap<String, String>();
        protected T mapObject;
        protected StringBuffer value = new StringBuffer();
        protected String contentField;
        protected Class<T> mapObjectClass;

        public MapObjectHandler(Class<T> mapObjectClass) {
            this(mapObjectClass, "name");
        }

        public MapObjectHandler(Class<T> mapObjectClass, String contentField) {
            this.mapObjectClass = mapObjectClass;
            this.contentField = contentField;
            this.atts.put("id", "id");
        }

        public MapObjectHandler setContentField(String contentField) {
            this.contentField = contentField;
            return this;
        }

        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            mapObject = createMapObject();

            for (int i = 0; i < attributes.getLength(); i++) {
                String name = attributes.getQName(i);
                String value = attributes.getValue(i);

                String field = atts.get(name);
                if (field != null) {
                    mapObject.setString(field, value);
                }
            }
            setMapObject(qName, mapObject);
            objects.push(mapObject);
        }

        private T createMapObject() {
            if (this.mapObject != null) return this.mapObject;
            try {
                Constructor constructor = mapObjectClass.getConstructor(Map.class);
                return (T) constructor.newInstance(new HashMap());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        protected void setMapObject(String qName, MapObject mapObject) {
            try {
                MapObject parent = objects.peek();
                parent.setMapObject(qName, mapObject);
            } catch (EmptyStackException e) {
            }
        }

        public void characters(char[] chars, int i, int i1) throws SAXException {
            value.append(chars, i, i1);
        }

        public void endElement(String string, String string1, String string2) throws SAXException {
            objects.pop();
            if (contentField != null) {
                mapObject.setString(contentField, value.toString());
            }
        }

        protected Object clone() {
            return new MapObjectHandler(mapObjectClass, contentField);
        }
    }

    public class UserHandler extends MapObjectHandler {
        public UserHandler() {
            super(User.class);
            atts.clear();
            atts.put("username", "name");
            contentField = "fullname";
        }

        protected Object clone() {
            return new UserHandler();
        }
    }

    public class MapObjectListHandler extends MapObjectHandler {
        private String fieldName;

        public MapObjectListHandler(Class mapObjectClass, String contentField, String fieldName) {
            super(mapObjectClass, contentField);
            this.fieldName = fieldName;
        }

        public MapObjectListHandler(Class mapObjectClass) {
            super(mapObjectClass);
            this.fieldName = null;
        }

        public MapObjectListHandler(Class mapObjectClass, String contentField) {
            super(mapObjectClass, contentField);
            this.fieldName = null;
        }

        public MapObjectListHandler setFieldName(String fieldName) {
            this.fieldName = fieldName;
            return this;
        }

        protected void setMapObject(String qName, MapObject mapObject) {
            MapObject parent = objects.peek();
            List list = parent.getMapObjects(getFieldName(qName), mapObject.getClass());
            list.add(mapObject);
        }

        protected String getFieldName(String qName) {
            return (fieldName != null) ? fieldName : qName + "s";
        }

        protected Object clone() {
            return new MapObjectListHandler(mapObjectClass, contentField, fieldName);
        }
    }

    public class SubtaskHanlder extends MapObjectListHandler {
        public SubtaskHanlder() {
            super(IssueRef.class, "key", "subTasks");
        }

        @Override
        protected void setMapObject(String qName, MapObject mapObject) {
            Issue parent = (Issue) objects.peek();
            Issue child = (Issue) mapObject;
            parent.getSubTasks().add(child);
            child.setParentTask(parent);
        }
    }

    public class CommentHandler extends MapObjectListHandler {
        public CommentHandler() {
            super(Comment.class);
            atts.clear();
            atts.put("author", "username");
            atts.put("created", "timePerformed");
            contentField = "body";
        }

        protected Object clone() {
            return new CommentHandler();
        }

        public void endElement(String string, String string1, String string2) throws SAXException {
            String text = value.toString();
            text = text.replaceAll("^<p>|</p>$", "");
            value = new StringBuffer(text);
            super.endElement(string, string1, string2);
        }

    }
}
