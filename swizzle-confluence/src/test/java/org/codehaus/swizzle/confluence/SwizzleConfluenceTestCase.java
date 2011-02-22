package org.codehaus.swizzle.confluence;

import junit.framework.TestCase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public abstract class SwizzleConfluenceTestCase
    extends TestCase {

  private Confluence confluence;
  private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-hhmmss");

  protected void setUp() throws Exception {
    confluence = new Confluence("http://docs.codehaus.org/rpc/xmlrpc");
    confluence.login("swizzle", "swizzle");
  }

  protected Confluence getConfluence()
      throws Exception {
    return confluence;
  }

  protected Page getTestPage()
      throws Exception {
    return confluence.getPage("SWIZZLE", "UnitTest Page");

  }

  protected Page getNewTestPage()
      throws Exception {
    Page newPage = new Page(new HashMap());
    newPage.setSpace("SWIZZLE");
    newPage.setTitle("Test - "+sdf.format(Calendar.getInstance().getTime()));
    newPage.setParentId(getTestPage().getId());
    newPage.setContent("This is a test");
    confluence.storePage(newPage);
    // Get the new object from the server to ensure to have the page ID
    System.out.println("Page used for tests : ["+newPage.getSpace()+"]:["+newPage.getTitle()+"]");
    return confluence.getPage(newPage.getSpace(),newPage.getTitle());

  }

  protected void tearDown() throws Exception {
    confluence.logout();
  }

}
