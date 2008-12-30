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

import org.codehaus.swizzle.jirareport.Main;

import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.renderer.PageContext;
import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.SubRenderer;
import com.atlassian.renderer.v2.macro.MacroException;
import com.atlassian.renderer.v2.macro.basic.AbstractPanelMacro;
import org.apache.velocity.VelocityContext;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;


/**
 * @version $Revision$ $Date$
 */
public class SwizzleJiraMacro extends AbstractPanelMacro {

    private WeakHashMap cache = new WeakHashMap();
    private long timeToLive = (60 * 60 * 1000);  // 1 hour

    public static class Key {
        private static MessageDigest md;

        static {
            try {
                md = MessageDigest.getInstance("SHA");
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e);
            }
        }

        private final byte[] digest;
        private final int hash;

        public Key(String body) {
            hash = body.hashCode();
            digest = md.digest(body.getBytes());
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final Key key = (Key) o;

            return MessageDigest.isEqual(digest, key.digest);
        }

        public int hashCode() {
            return hash;
        }
    }

    public static class Entry {
        private final String content;
        private final long created;

        public Entry(String content) {
            this.content = content;
            this.created = System.currentTimeMillis();
        }

        public boolean isTimedOut(long timeOut) {
            return created + timeOut < System.currentTimeMillis();
        }

        public String getContent() {
            return content;
        }

        public long getCreated() {
            return created;
        }
    }

    public static class PageCache {
        private final String name;
        private final int version;
        private final WeakHashMap cache = new WeakHashMap();

        public PageCache(String name, int version) {
            this.name = name;
            this.version = version;
        }

        public String getName() {
            return name;
        }

        public int getVersion() {
            return version;
        }

        public WeakHashMap getCache() {
            return cache;
        }
    }

    private synchronized WeakHashMap getCache(ContentEntityObject page) {
        String name = page.getNameForComparison();
        PageCache pageCache = (PageCache) cache.get(name);
        if (pageCache == null) {
            pageCache = new PageCache(name, page.getVersion());
            cache.put(name, pageCache);
        } else if (pageCache.getVersion() < page.getVersion()) {
            pageCache.getCache().clear();
            pageCache = new PageCache(name, page.getVersion());
            cache.put(name, pageCache);
        }
        return pageCache.getCache();
    }

    public String execute(Map params, String body, com.atlassian.renderer.RenderContext renderContext) throws MacroException {
        PageContext pageContext = (PageContext) renderContext;
        ContentEntityObject entity = pageContext.getEntity();

        // One cache per page, one synchronized block to get it
        // this would affect read/writes on all pages
        WeakHashMap cache = getCache(entity);

        // Now that we have the cache for just this page,
        // synchronized on it and do our work.
        synchronized (cache) {
            Key key = new Key(body);
            Entry entry = (Entry) cache.get(key);

            if (entry != null && !entry.isTimedOut(timeToLive)) {
                return entry.getContent();
            }

            String content = generateContent(params, body, renderContext);

            cache.put(key, new Entry(content));

            return content;
        }
    }

    private String generateContent(Map params, String body, com.atlassian.renderer.RenderContext renderContext) throws MacroException {
        URL templateUrl = null;
        try {
            String url = (String) params.get("template");
            if (url != null) {
                templateUrl = new URL(url);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return "Invalid template url: " + e.getMessage();
        }

        File tempFile = null;
        String content = body;
        String template;
        if (templateUrl != null) {
            template = templateUrl.toExternalForm();
        } else {
            try {
                tempFile = File.createTempFile("swizzlejira", ".vm");
                FileWriter fileWriter = new FileWriter(tempFile);
                fileWriter.write(content);
                fileWriter.flush();
                fileWriter.close();
                template = tempFile.getAbsolutePath();
            } catch (IOException e) {
                throw new MacroException("Unable to save template content to temp file.", e);
            }
        }
        PrintStream out = null;
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            out = new PrintStream(baos);

            Map defaults = MacroUtils.defaultVelocityContext();
            for (Iterator iterator = params.entrySet().iterator(); iterator.hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
                defaults.put(entry.getKey(), entry.getValue());
            }

            VelocityContext context = new VelocityContext(defaults);
            context.put("as", new IssuesUtil());
            Main.generate(context, template, out);
        } catch (Throwable e) {
            if (e instanceof InvocationTargetException) {
                InvocationTargetException ite = (InvocationTargetException) e;
                e = ite.getCause();
            }

            CharArrayWriter trace = new CharArrayWriter();
            PrintWriter writer = new PrintWriter(trace);
            writer.println("Template: " + template);
            e.printStackTrace(writer);
            writer.flush();
            writer.close();
            String error = new String(trace.toCharArray());
            return error.replaceAll("\n", "<br>");
        }

        try {
            out.flush();
            out.close();
            tempFile.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }


        SubRenderer subRenderer = getSubRenderer();
        String velocityOutput = new String(baos.toByteArray());
        return subRenderer.render(velocityOutput, renderContext);
    }

    public String getName() {
        return "swizzlejira";
    }

    protected String getPanelContentCSSClass() {
        return "jirareportContent";
    }

    protected String getPanelCSSClass() {
        return "jirareport";
    }

    protected String getPanelHeaderCSSClass() {
        return "jirareportHeader";
    }

    public RenderMode getBodyRenderMode() {
        return RenderMode.allow(RenderMode.F_NONE);
    }

    public boolean suppressMacroRenderingDuringWysiwyg() {
        return false;
    }

    protected SubRenderer getSubRenderer() {
        return super.getSubRenderer();
    }

}
