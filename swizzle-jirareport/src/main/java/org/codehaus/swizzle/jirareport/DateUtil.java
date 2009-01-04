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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @version $Revision$ $Date$
 */
public class DateUtil {
    private SimpleDateFormat dateFormat;
    private Date now = new Date();

    public DateUtil(String format) throws Exception {
        dateFormat = new SimpleDateFormat(format);
    }

    public String format(String format) {
        dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(now);
    }

    public String format(Date date) {
        return dateFormat.format(date);
    }

    public String format(Date date, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(date);
    }

    public String format(Date date, String format, String timeZone) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        dateFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
        return dateFormat.format(date);
    }

    public String as(String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(now);
    }

    public String toString() {
        return dateFormat.format(now);
    }

    // need something to return a date

    // dayAgo()
    // dayFrom(Date)
    // daysAgo(int)
    // daysFrom(int, Date)
    // weekAgo()
    // weekFrom(Date)
    // weeksAgo(int)
    // weeksFrom(int, Date)
    // monthAgo()
    // monthFrom(Date)
    // monthsAgo(int)
    // monthsFrom(int, Date)
    // yearAgo()
    // yearFrom(Date)
    // yearsAgo(int)
    // yearsFrom(int, Date)

    // from, to, between
}
