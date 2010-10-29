package org.codehaus.swizzle.jira;

import junit.framework.TestCase;

public abstract class SwizzleJiraTestCase
    extends TestCase
{
    protected Jira getJira()
        throws Exception
    {
        Jira jira = new Jira( "http://jira.codehaus.org/rpc/xmlrpc" );
        jira.login( "swizzle", "swizzle" );
        return jira;
    }
}
