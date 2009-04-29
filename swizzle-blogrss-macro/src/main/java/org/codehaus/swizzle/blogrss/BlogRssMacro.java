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
package org.codehaus.swizzle.blogrss;

import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.renderer.v2.macros.BaseHttpRetrievalMacro;
import com.atlassian.confluence.util.http.HttpResponse;
import com.atlassian.confluence.util.io.IOUtils;
import com.atlassian.confluence.util.velocity.VelocityUtils;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.RenderUtils;
import com.atlassian.renderer.v2.macro.MacroException;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndLink;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class BlogRssMacro extends BaseHttpRetrievalMacro {

    private Logger log = Logger.getLogger(this.getClass());


    private byte[] readResponse(HttpResponse response) throws IOException {
        InputStream in;
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        in = response.getResponse();

        int read;
        byte[] bytes = new byte[1024];
        while ((read = in.read(bytes)) != -1) {
            bytesOut.write(bytes, 0, read);
        }

        return bytesOut.toByteArray();
    }

    public String successfulResponse(Map parameters, RenderContext renderContext, String url, HttpResponse response) throws MacroException {

        Integer maxItems = new Integer(param(parameters, "max", 1, "5"));

        String s = param(parameters, "truncate", 3, "450");
        int truncate;

        if ("false".equalsIgnoreCase(s)){
            truncate = 0;
        } else {
            truncate = new Integer(s);
        }

        SyndFeed feed;

        byte[] webContent = null;
        try {
            webContent = readResponse(response);
            feed = parseRSSFeed(url, webContent);
        }
        catch (Exception e) {
            if (webContent != null && response.getContentType().indexOf("html") != -1) {
                throw new MacroException("The RSS macro is retrieving an HTML page.");

            }
            throw new MacroException("Error parsing RSS feed: " + e.toString());

        }

        String blogUrl = null;
        List links = feed.getLinks();
        if (links != null) for (Iterator iterator = links.iterator(); iterator.hasNext();) {
            Object o = iterator.next();
            if (o instanceof SyndLink) {
                SyndLink link = (SyndLink) o;
                if ("alternate".equals(link.getRel())) {
                    blogUrl = link.getHref();
                }
            }
        }

        List entries = feed.getEntries();

        for (Iterator iterator = entries.iterator(); iterator.hasNext();) {
            SyndEntry entry = (SyndEntry) iterator.next();

            log.error("blogrss: entry " + entry.getTitle() + " " + entry);
            SyndContent description = entry.getDescription();
            if (description == null) {
                List contents = entry.getContents();
                if (contents == null) {
                    log.error("blogrss: contents are null");
                    continue;
                }
                
                for (Object o : contents) {
                    if (o instanceof SyndContent) {
                        SyndContent content = (SyndContent) o;
                        truncate(truncate, content, entry.getLink());
                        entry.setDescription(content);
                        break;
                    }
                }
            } else {
                truncate(truncate, description, entry.getLink());
                entry.setDescription(description);
            }
        }

        Map contextMap = MacroUtils.defaultVelocityContext();
        contextMap.put("url", url);
        contextMap.put("blogUrl", blogUrl);
        contextMap.put("urlHash", new Integer(url.hashCode()));
        contextMap.put("feed", feed);
        contextMap.put("max", maxItems);
        contextMap.put("linkDate", new SimpleDateFormat("yyyyMMdd")); // 20091105
        contextMap.put("shortDate", new SimpleDateFormat("dd MMM @ h:mm a")); // 05 Nov @ 10:46 AM
        contextMap.put("longDate", new SimpleDateFormat("EEEE, MMMM d, yyyy")); // Wednesday, November 5, 2008

        try {
            return VelocityUtils.getRenderedTemplate("swizzle/blogrss/templates/blogrss.vm", contextMap);
        } catch (Exception e) {
            log.error("Error while trying to assemble the RSS result!", e);
            throw new MacroException(e.getMessage());
        }
    }

    private void truncate(int truncate, SyndContent description, String url) {
        if (truncate > 0) {
            String s = description.getValue();
            String content = abbreviate(truncate, s, url);
            if (content.endsWith("...")) content += " <a href=\""+ url +"\"><i>(more)</i></a>";
            description.setValue(content);
        }
    }

    private String abbreviate(int truncate, String s, String url) {
        int count = 0;
        if (s.length() <= truncate) return s;

        StringBuilder sb = new StringBuilder(truncate*2);

        boolean skipping = false;

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            sb.append(c);
            
            if ('<' == c){
                skipping = true;
            }
            if (skipping && '>' == c) {
                skipping = false;
            }

            if (!skipping) count++;

            if (count> truncate) {
                sb.append("... <a href=\"").append(url).append("\"><i>(more)</i></a>");
                break;
            }
        }
        return sb.toString();
    }

    private String param(Map parameters, String name, int i, String defaultValue) {
        String m = RenderUtils.getParameter(parameters, name, i);
        return (m != null && !"".equals(m)) ? m : defaultValue;
    }

    private SyndFeed parseRSSFeed(String url, byte[] webContent) throws IOException {

        ByteArrayInputStream bufferedIn = null;
        try {
            bufferedIn = new ByteArrayInputStream(webContent);

            SyndFeedInput input = new SyndFeedInput();
            return input.build(new XmlReader(bufferedIn));
        } catch (FeedException e) {
            log.error("Error while trying to assemble the RSS result!", e);
            throw new IOException(e.getMessage());
        } finally {
            IOUtils.close(bufferedIn);
        }
    }
}
