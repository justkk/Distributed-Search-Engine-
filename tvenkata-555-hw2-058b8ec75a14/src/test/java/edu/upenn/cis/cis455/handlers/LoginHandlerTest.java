package edu.upenn.cis.cis455.handlers;

import edu.upenn.cis.cis455.crawler.handlers.LoginHandler;
import edu.upenn.cis.cis455.storage.StorageInterfaceImpl;
import junit.framework.TestCase;
import org.apache.logging.log4j.Level;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import spark.Request;
import spark.Response;
import spark.Session;

public class LoginHandlerTest extends TestCase {

    private StorageInterfaceImpl storageInterface;
    private LoginHandler loginHandler;
    private Session session;
    private Request request;
    private Response response;

    @Before
    public void setUp() {
        org.apache.logging.log4j.core.config.Configurator.setLevel("edu.upenn.cis.cis455", Level.DEBUG);
        storageInterface = Mockito.mock(StorageInterfaceImpl.class);
        loginHandler = new LoginHandler(storageInterface);
        session = Mockito.mock(Session.class);
        request = Mockito.mock(Request.class);
        response = Mockito.mock(Response.class);
        Mockito.when(request.session()).thenReturn(session);
    }

    @Test(expected = Exception.class)
    public void LoginTest1() {
        Mockito.when(request.queryParams("username")).thenReturn("username");
        Mockito.when(request.queryParams("password")).thenReturn("password");
        Mockito.when(storageInterface.getSessionForUser("username", "password")).thenReturn(true);
        Mockito.doThrow(new Exception()).when(response).redirect("/index.html");
        loginHandler.handle(request, response);

    }

    @Test(expected = Exception.class)
    public void LoginTest2() {
        Mockito.when(request.queryParams("username")).thenReturn("username");
        Mockito.when(request.queryParams("password")).thenReturn("password");
        Mockito.when(storageInterface.getSessionForUser("username", "password")).thenReturn(false);
        Mockito.doThrow(new Exception()).when(response).redirect("/login-form.html?message=Invalid credentials");
        loginHandler.handle(request, response);
    }

    @Test
    public void testLoginTest1() {
        try{
            Mockito.when(request.queryParams("username")).thenReturn("username");
            Mockito.when(request.queryParams("password")).thenReturn("password");
            Mockito.when(storageInterface.getSessionForUser("username", "password")).thenReturn(true);
            Mockito.doThrow(new Exception()).when(response).redirect("/index.html");
            loginHandler.handle(request, response);
            fail();
        } catch (Exception e) {

        }


    }

    @Test
    public void testLoginTest2() {
        try{
            Mockito.when(request.queryParams("username")).thenReturn("username");
            Mockito.when(request.queryParams("password")).thenReturn("password");
            Mockito.when(storageInterface.getSessionForUser("username", "password")).thenReturn(false);
            Mockito.doThrow(new Exception()).when(response).redirect("/login-form.html?message=Invalid credentials");
            loginHandler.handle(request, response);
            fail();
        } catch (Exception e) {

        }

    }


}
