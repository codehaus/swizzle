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

import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.HashMap;
import java.util.Collections;
import java.util.TreeMap;
import java.util.LinkedHashMap;
import java.util.Collection;
import java.util.Iterator;

/**
 * @version $Revision$ $Date$
 */
public class Jira {
    private static Map cacheMetadata = new HashMap();

    static {
        cacheMetadata.put(IssueType.class, new String[]{"id","name"});
        cacheMetadata.put(Component.class, new String[]{"id","name"});
        cacheMetadata.put(Priority.class, new String[]{"id","name"});
        cacheMetadata.put(Resolution.class, new String[]{"id","name"});
        cacheMetadata.put(Version.class, new String[]{"id","name"});
        cacheMetadata.put(Status.class, new String[]{"id","name"});
        cacheMetadata.put(Issue.class, new String[]{"id","key"});
        cacheMetadata.put(Project.class, new String[]{"id","key"});
    }

    private final XmlRpcClient client;
    private String token;
    private HashMap cache;
    private boolean autofill = true;
    private Map issueFillers = new LinkedHashMap();

    public Jira(String endpoint) throws MalformedURLException {
        if (endpoint.endsWith("/")) {
            endpoint = endpoint.substring(0, endpoint.length() - 1);
        }

        if (! endpoint.endsWith("/rpc/xmlrpc")) {
            endpoint += "/rpc/xmlrpc";
        }

        this.client = new XmlRpcClient(endpoint);
        this.cache = new HashMap();
        BasicIssueFiller basicIssueFiller = new BasicIssueFiller(this);
        basicIssueFiller.setEnabled(true);
        issueFillers.put("issue", basicIssueFiller);
        issueFillers.put("project", new ProjectFiller(this));
    }

    /**
     * Valid schemes are "issue", "project", "voters"
     * "issues" is enabled by default
     * @param scheme
     * @param enabled
     */
    public void autofill(String scheme, boolean enabled){
        if (scheme.equals("voters") && !issueFillers.containsKey("voters")){
            try {
                ClassLoader classLoader = this.getClass().getClassLoader();
                Class clazz = classLoader.loadClass("org.codehaus.swizzle.jira.VotersFiller");
                Constructor constructor = clazz.getConstructor(new Class[]{Jira.class});
                IssueFiller issueFiller = (IssueFiller) constructor.newInstance(new Object[]{this});
                issueFillers.put("voters", issueFiller);
            } catch (ClassNotFoundException e) {
                System.err.println("Autofilling votes requires the swizzle-stream library.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (scheme.equals("subtasks") && !issueFillers.containsKey("subtasks")){
            try {
                ClassLoader classLoader = this.getClass().getClassLoader();
                Class clazz = classLoader.loadClass("org.codehaus.swizzle.jira.SubTasksFiller");
                Constructor constructor = clazz.getConstructor(new Class[]{Jira.class});
                IssueFiller issueFiller = (IssueFiller) constructor.newInstance(new Object[]{this});
                issueFillers.put("subtasks", issueFiller);
            } catch (ClassNotFoundException e) {
                System.err.println("Autofilling sub-tasks requires the swizzle-stream library.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        IssueFiller filler = (IssueFiller) issueFillers.get(scheme);
        if (filler == null){
            throw new UnsupportedOperationException("Autofill Scheme not supported: "+scheme);
        }
        filler.setEnabled(enabled);
    }

    /**
     * Logs the user into JIRA
     */
    public void login(String username, String password) throws Exception {
        token = (String) call("login", username, password);
    }

    /**
     * remove this token from the list of logged in tokens.
     */
    public boolean logout() throws Exception {
        cache.clear();
        Boolean value = (Boolean) call("logout");
        return value.booleanValue();
    }

    /**
     * Adds a comment to an issue
     * TODO: If someone adds a comment to an issue, we should account for that in our caching
     */
    public boolean addComment(String issueKey, String comment) throws Exception {
        Boolean value = (Boolean) call("getComments", issueKey, comment);
        return value.booleanValue();
    }

    /**
     * Creates an issue in JIRA
     * TODO: If someone creates an issue, we should account for that in our caching
     */
    public Issue createIssue(Issue issue) throws Exception {
        Hashtable data = (Hashtable) call("createIssue", issue.toHashtable());
        return (autofill)? fill(new Issue(data)):new Issue(data);
    }

    /**
     * Updates an issue in JIRA from a Hashtable object
     * TODO: If someone updates an issue, we should account for that in our caching
     */
    public Issue updateIssue(String issueKey, Issue issue) throws Exception {
        Hashtable data = (Hashtable) call("updateIssue", issueKey, issue.toHashtable());
        return (autofill)? fill(new Issue(data)):new Issue(data);
    }

    /**
     * List<{@link Comment}>:  Returns all comments associated with the issue
     */
    public List getComments(String issueKey) {
        return cachedList(new Call("getComments", issueKey), Comment.class);
    }


    /**
     * Gets an issue from a given issue key.
     */
    public Issue getIssue(String issueKey) {
        return (Issue) cachedObject(new Call("getIssue", issueKey), Issue.class);
    }

    /**
     * List<{@link Issue}>:  Executes a saved filter
     */
    public List getIssuesFromFilter(Filter filter) throws Exception {
        return getIssuesFromFilter(filter.getId());
    }

    /**
     * List<{@link Issue}>:  Executes a saved filter
     */
    public List getIssuesFromFilter(String filterId) throws Exception {
        Vector vector = (Vector) call("getIssuesFromFilter", filterId);
        return toList(vector, Issue.class);
    }

    /**
     * List<{@link Issue}>:  Find issues using a free text search
     */
    public List getIssuesFromTextSearch(String searchTerms) throws Exception {
        Vector vector = (Vector) call("getIssuesFromTextSearch", searchTerms);
        return toList(vector, Issue.class);
    }

    /**
     * List<{@link Issue}>:  Find issues using a free text search, limited to certain projects
     */
    public List getIssuesFromTextSearchWithProject(Vector projectKeys, String searchTerms, int maxNumResults) throws Exception {
        Vector vector = (Vector) call("getIssuesFromTextSearchWithProject", projectKeys, searchTerms, new Integer(maxNumResults));
        return toList(vector, Issue.class);
    }

    /**
     * List<{@link IssueType}>:  Returns all visible issue types in the system
     */
    public List getIssueTypes() {
        return cachedList(new Call("getIssueTypes"), IssueType.class);
    }

    public IssueType getIssueType(String name) {
        Map objects = cachedMap(new Call("getIssueTypes"), IssueType.class, "name");
        return (IssueType) objects.get(name);
    }

    public IssueType getIssueType(int id) {
        Map objects = cachedMap(new Call("getIssueTypes"), IssueType.class, "id");
        return (IssueType) objects.get(id+"");
    }


    /**
     * List<{@link Priority}>:  Returns all priorities in the system
     */
    public List getPriorities() {
        return cachedList(new Call("getPriorities"), Priority.class);
    }

    public Priority getPriority(String name) {
        Map objects = cachedMap(new Call("getPriorities"), Priority.class, "name");
        return (Priority) objects.get(name);
    }

    public Priority getPriority(int id) {
        Map objects = cachedMap(new Call("getPriorities"), Priority.class, "id");
        return (Priority) objects.get(id + "");
    }

    /**
     * List<{@link Project}>:  Returns a list of projects available to the user
     */
    public List getProjects() {
        return cachedList(new Call("getProjects"), Project.class);
    }

    public Project getProject(String key) {
        Map objects = cachedMap(new Call("getProjects"), Project.class, "key");
        return (Project) objects.get(key);
    }

    public Project getProject(int id) {
        Map objects = cachedMap(new Call("getProjects"), Project.class, "id");
        return (Project) objects.get(id + "");
    }

    /**
     * List<{@link Resolution}>:  Returns all resolutions in the system
     */
    public List getResolutions() {
        return cachedList(new Call("getResolutions"), Resolution.class);
    }

    public Resolution getResolution(String name) {
        Map objects = cachedMap(new Call("getResolutions"), Resolution.class, "name");
        return (Resolution) objects.get(name);
    }

    public Resolution getResolution(int id) {
        Map objects = cachedMap(new Call("getResolutions"), Resolution.class, "id");
        return (Resolution) objects.get(id + "");
    }

    /**
     * List<{@link Status}>:  Returns all statuses in the system
     */
    public List getStatuses() {
        return cachedList(new Call("getStatuses"), Status.class);
    }

    public Status getStatus(String name) {
        Map objects = cachedMap(new Call("getStatuses"), Status.class, "name");
        return (Status) objects.get(name);
    }

    public Status getStatus(int id) {
        Map objects = cachedMap(new Call("getStatuses"), Status.class, "id");
        return (Status) objects.get(id + "");
    }

    /**
     * List<{@link Filter}>:  Gets all saved filters available for the currently logged in user
     */
    public List getSavedFilters() {
        return cachedList(new Call("getSavedFilters"), Filter.class);
    }

    /**
     * Returns the Server information such as baseUrl, version, edition, buildDate, buildNumber.
     */
    public ServerInfo getServerInfo() {
        return (ServerInfo) cachedObject(new Call("getServerInfo"), ServerInfo.class);
    }

    /**
     * List<{@link IssueType}>:  Returns all visible subtask issue types in the system
     *
     * @return list of {@link IssueType}
     */
    public List getSubTaskIssueTypes() {
        return cachedList(new Call("getSubTaskIssueTypes"), IssueType.class);
    }

    public IssueType getSubTaskIssueType(String name) {
        Map objects = cachedMap(new Call("getSubTaskIssueTypes"), IssueType.class, "name");
        return (IssueType) objects.get(name);
    }

    public IssueType getSubTaskIssueType(int id) {
        Map objects = cachedMap(new Call("getSubTaskIssueTypes"), IssueType.class, "id");
        return (IssueType) objects.get(id + "");
    }


    /**
     * Returns a user's information given a username
     */
    public User getUser(String username) {
        return (User) cachedObject(new Call("getUser", username), User.class);
    }

    // ----  PROJECT related data ---- /////////////////////////////////////////////////////

    /**
     * List<{@link Component}>:  Returns all components available in the specified project
     */
    public List getComponents(String projectKey) {
        return cachedList(new Call("getComponents", projectKey), Component.class);
    }

    public Component getComponent(String projectKey, String name) {
        Map components = cachedMap(new Call("getComponents", projectKey), Component.class, "name");
        return (Component) components.get(name);
    }

    public Component getComponent(String projectKey, int id) {
        Map components = cachedMap(new Call("getComponents", projectKey), Component.class, "id");
        return (Component) components.get(id+"");
    }

    /**
     * List<{@link Version}>:  Returns all versions available in the specified project
     */
    public List getVersions(String projectKey) {
        return cachedList(new Call("getVersions", projectKey), Version.class);
    }

    public Version getVersion(String projectKey, String name) {
        Map versions = cachedMap(new Call("getVersions", projectKey), Version.class, "name");
        return (Version) versions.get(name);
    }

    public Version getVersion(String projectKey, int id) {
        Map versions = cachedMap(new Call("getVersions", projectKey), Version.class, "id");
        return (Version) versions.get(id+"");
    }

    // ----  PROJECT related data ---- /////////////////////////////////////////////////////

    private List toList(Vector vector, Class type) {
        List list = new MapObjectList(vector.size());

        try {
            Constructor constructor = type.getConstructor(new Class[]{Map.class});
            for (int i = 0; i < vector.size(); i++) {
                Map data = (Map) vector.elementAt(i);
                Object object = constructor.newInstance(new Object[]{data});
                list.add(object);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return list;
    }

    private Object toObject(Hashtable data, Class type) {
        try {
            Constructor constructor = type.getConstructor(new Class[]{Map.class});
            return constructor.newInstance(new Object[]{data});
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object call(String command) throws Exception {
        Object[] args = {};
        return call(command, args);
    }

    private Object call(String command, Object arg1) throws Exception {
        Object[] args = {arg1};
        return call(command, args);
    }

    private Object call(String command, Object arg1, Object arg2) throws Exception {
        Object[] args = {arg1, arg2};
        return call(command, args);
    }

    private Object call(String command, Object arg1, Object arg2, Object arg3) throws Exception {
        Object[] args = {arg1, arg2, arg3};
        return call(command, args);
    }

    private Object call(String command, Object[] args) throws XmlRpcException, IOException {
        Vector vector = new Vector();
        if (token != null) vector.add(token);
        vector.addAll(Arrays.asList(args));
        return client.execute("jira1." + command, vector);
    }


    public Issue fill(Issue issue){
        Collection collection = issueFillers.values();
        for (Iterator iterator = collection.iterator(); iterator.hasNext();) {
            IssueFiller issueFiller = (IssueFiller) iterator.next();
            issueFiller.fill(issue);
        }
        return issue;
    }
    public static class Call {
        public final String command;
        public final Object[] args;

        public Call(String command){
            Object[] args = {};
            this.command = command;
            this.args = args;
        }

        public Call(String command, Object arg1){
            Object[] args = {arg1};
            this.command = command;
            this.args = args;
        }

        public Call(String command, Object arg1, Object arg2){
            Object[] args = {arg1, arg2};
            this.command = command;
            this.args = args;
        }

        public Call(String command, Object arg1, Object arg2, Object arg3){
            Object[] args = {arg1, arg2, arg3};
            this.command = command;
            this.args = args;
        }

        public Call(String command, Object[] args) {
            this.command = command;
            this.args = args;
        }


        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final Call call = (Call) o;

            // Probably incorrect - comparing Object[] arrays with Arrays.equals
            if (!Arrays.equals(args, call.args)) return false;
            if (!command.equals(call.command)) return false;

            return true;
        }

        public int hashCode() {
            return command.hashCode();
        }
    }

    private Map callcache = new HashMap();

    private List cachedList(Call call, Class type) {
        Object result = cache(call, type);

        Map indexes = (Map) result;
        return (List) indexes.get(List.class);
    }

    private Map cachedMap(Call call, Class type, String field) {
        Object result = cache(call, type);

        Map indexes = (Map) result;
        return (Map) indexes.get(field);
    }

    private Object cachedObject(Call call, Class type) {
        return cache(call, type);
    }

    private Object cache(Call call, Class type) {
        Object object = callcache.get(call);
        if (object != null){
            return object;
        }

        Object result = exec(call);
        if (result instanceof Vector) {
            List list = toList((Vector) result, type);
            Map indexes = new HashMap();
            String[] uniqueFields = (String[]) cacheMetadata.get(type);
            for (int i = 0; uniqueFields != null && i < uniqueFields.length; i++) {
                Map index = new HashMap();
                String field = uniqueFields[i];
                for (int j = 0; j < list.size(); j++) {
                    MapObject mapObject = (MapObject) list.get(j);
                    index.put(mapObject.getString(field), mapObject);
                }
                indexes.put(field, index);
            }
            indexes.put(List.class, new MapObjectList(list));
//            indexes.put(List.class, Collections.unmodifiableList(list));
            if (autofill && type == Issue.class){
                for (int i = 0; i < list.size(); i++) {
                    Issue issue = (Issue) list.get(i);
                    fill(issue);
                }
            }
            result = indexes;
        } else if (result instanceof Hashtable){
            result = toObject((Hashtable)result, type);
            if (autofill && type == Issue.class){
                fill((Issue)result);
            }
        }

        callcache.put(call, result);
        return result;
    }


    private Object exec(Call call) {
        try {
            Vector vector = new Vector();
            if (token != null) vector.add(token);
            vector.addAll(Arrays.asList(call.args));
            return client.execute("jira1." + call.command, vector);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
