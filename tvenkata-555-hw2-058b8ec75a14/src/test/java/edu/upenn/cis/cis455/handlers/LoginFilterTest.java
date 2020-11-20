package edu.upenn.cis.cis455.handlers;

import edu.upenn.cis.cis455.crawler.handlers.LoginFilter;
import edu.upenn.cis.cis455.storage.StorageInterfaceImpl;
import junit.framework.TestCase;
import org.apache.logging.log4j.Level;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import spark.Request;
import spark.Response;
import spark.Session;

public class LoginFilterTest extends TestCase {

    private StorageInterfaceImpl storageInterface;
    private LoginFilter loginFilter;
    private Session session;
    private Request request;
    private Response response;

    @Before
    public void setUp() {
        org.apache.logging.log4j.core.config.Configurator.setLevel("edu.upenn.cis.cis455", Level.DEBUG);
        storageInterface = Mockito.mock(StorageInterfaceImpl.class);
        loginFilter = new LoginFilter(storageInterface);
        session = Mockito.mock(Session.class);
        request = Mockito.mock(Request.class);
        response = Mockito.mock(Response.class);
        Mockito.when(request.session()).thenReturn(session);
    }

    @Test(expected = Exception.class)
    public void loginFormRequest1() throws Exception {
        Mockito.when(request.pathInfo()).thenReturn("/login-form.html");
        Mockito.when(request.session(false)).thenReturn(null);
        Mockito.doThrow(new Exception()).when(response).redirect("/login-form.html");
        loginFilter.handle(request, response);
    }

    @Test(expected = Exception.class)
    public void loginFormRequest2() throws Exception {
        Mockito.when(request.pathInfo()).thenReturn("/login-form.html");
        Mockito.when(request.session(false)).thenReturn(session);
        Mockito.when(session.attribute("user")).thenReturn("user");
        Mockito.doThrow(new Exception()).when(request).attribute("user", "user");
        loginFilter.handle(request, response);
    }

    @Test
    public void loginFormRequest3() throws Exception {
        Mockito.when(request.pathInfo()).thenReturn("/login.html");
        loginFilter.handle(request, response);
    }

    @Test(expected = Exception.class)
    public void testLoginFormRequest1() throws Exception {
        try{
            Mockito.when(request.pathInfo()).thenReturn("/login-form.html");
            Mockito.when(request.session(false)).thenReturn(null);
            Mockito.doThrow(new Exception()).when(response).redirect("/login-form.html");
            loginFilter.handle(request, response);
            fail();
        } catch (Exception e) {
            // success
        }

    }

    @Test(expected = Exception.class)
    public void testLoginFormRequest2() throws Exception {
        try{
            Mockito.when(request.pathInfo()).thenReturn("/login-form.html");
            Mockito.when(request.session(false)).thenReturn(session);
            Mockito.when(session.attribute("user")).thenReturn("user");
            Mockito.doThrow(new Exception()).when(request).attribute("user", "user");
            loginFilter.handle(request, response);
            fail();
        } catch (Exception e) {

        }

    }

}
