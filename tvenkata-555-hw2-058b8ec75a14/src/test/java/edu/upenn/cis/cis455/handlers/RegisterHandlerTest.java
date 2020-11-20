package edu.upenn.cis.cis455.handlers;

import edu.upenn.cis.cis455.TestException;
import edu.upenn.cis.cis455.crawler.handlers.RegisterHandler;
import edu.upenn.cis.cis455.storage.StorageInterfaceImpl;
import junit.framework.TestCase;
import org.apache.logging.log4j.Level;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import spark.Request;
import spark.Response;
import spark.Session;

public class RegisterHandlerTest extends TestCase {

    private StorageInterfaceImpl storageInterface;
    private RegisterHandler registerHandler;
    private Session session;
    private Request request;
    private Response response;

    @Before
    public void setUp() {
        org.apache.logging.log4j.core.config.Configurator.setLevel("edu.upenn.cis.cis455", Level.DEBUG);
        storageInterface = Mockito.mock(StorageInterfaceImpl.class);
        registerHandler = new RegisterHandler(storageInterface);
        session = Mockito.mock(Session.class);
        request = Mockito.mock(Request.class);
        response = Mockito.mock(Response.class);
        Mockito.when(request.session()).thenReturn(session);
    }

    @Test(expected = TestException.class)
    public void registerTest1() throws Exception {
        Mockito.when(request.queryParams("username")).thenReturn(null);
        Mockito.doThrow(TestException.class).when(response).redirect("/register.html?message=" + "username cant be empty");
        registerHandler.handle(request, response);
    }

    @Test(expected = TestException.class)
    public void registerTest2() throws Exception {
        Mockito.when(request.queryParams("username")).thenReturn("username");
        Mockito.when(request.queryParams("password")).thenReturn(null);
        Mockito.doThrow(TestException.class).when(response).redirect("/register.html?message=" + "password cant be empty");
        registerHandler.handle(request, response);
    }

    @Test(expected = TestException.class)
    public void registerTest3() throws Exception {
        Mockito.when(request.queryParams("username")).thenReturn("username");
        Mockito.when(request.queryParams("password")).thenReturn("password");
        Mockito.when(request.queryParams("firstname")).thenReturn(null);
        Mockito.doThrow(TestException.class).when(response).redirect("/register.html?message=" + "firstName cant be empty");
        registerHandler.handle(request, response);
    }


    @Test(expected = TestException.class)
    public void registerTest4() throws Exception {
        Mockito.when(request.queryParams("username")).thenReturn("username");
        Mockito.when(request.queryParams("password")).thenReturn("password");
        Mockito.when(request.queryParams("firstname")).thenReturn("firstname");
        Mockito.when(request.queryParams("lastname")).thenReturn(null);
        Mockito.doThrow(TestException.class).when(response).redirect("/register.html?message=" + "lastName cant be empty");
        registerHandler.handle(request, response);
    }

    @Test()
    public void registerTest5() throws Exception {
        Mockito.when(request.queryParams("username")).thenReturn("username");
        Mockito.when(request.queryParams("password")).thenReturn("password");
        Mockito.when(request.queryParams("firstname")).thenReturn("firstname");
        Mockito.when(request.queryParams("lastname")).thenReturn("lastname");
        Mockito.when(storageInterface.addUser("username", "password", "firstname",
                "lastname")).thenReturn(1);
        Assert.assertEquals(registerHandler.handle(request, response), "User added successfully");
    }

    @Test(expected = TestException.class)
    public void registerTest6() throws Exception {
        Mockito.when(request.queryParams("username")).thenReturn("username");
        Mockito.when(request.queryParams("password")).thenReturn("password");
        Mockito.when(request.queryParams("firstname")).thenReturn("firstname");
        Mockito.when(request.queryParams("lastname")).thenReturn("lastname");
        Mockito.when(storageInterface.addUser("username", "password", "firstname",
                "lastname")).thenThrow(TestException.class);
        registerHandler.handle(request, response);
    }

    @Test
    public void testRegisterTest1() throws Exception {
        try{
            Mockito.when(request.queryParams("username")).thenReturn(null);
            Mockito.doThrow(TestException.class).when(response).redirect("/register.html?message=" + "username cant be empty");
            registerHandler.handle(request, response);
            fail();
        } catch (Exception e) {

        }

    }

    @Test
    public void testRegisterTest2() throws Exception {
        try{
            Mockito.when(request.queryParams("username")).thenReturn("username");
            Mockito.when(request.queryParams("password")).thenReturn(null);
            Mockito.doThrow(TestException.class).when(response).redirect("/register.html?message=" + "password cant be empty");
            registerHandler.handle(request, response);
            fail();
        } catch (Exception e) {

        }

    }

    @Test
    public void testRegisterTest3() throws Exception {
        try{
            Mockito.when(request.queryParams("username")).thenReturn("username");
            Mockito.when(request.queryParams("password")).thenReturn("password");
            Mockito.when(request.queryParams("firstname")).thenReturn(null);
            Mockito.doThrow(TestException.class).when(response).redirect("/register.html?message=" + "firstName cant be empty");
            registerHandler.handle(request, response);
            fail();
        } catch (Exception e) {

        }

    }


    @Test
    public void testRegisterTest4() throws Exception {
        try{
            Mockito.when(request.queryParams("username")).thenReturn("username");
            Mockito.when(request.queryParams("password")).thenReturn("password");
            Mockito.when(request.queryParams("firstname")).thenReturn("firstname");
            Mockito.when(request.queryParams("lastname")).thenReturn(null);
            Mockito.doThrow(TestException.class).when(response).redirect("/register.html?message=" + "lastName cant be empty");
            registerHandler.handle(request, response);
            fail();
        } catch (Exception e) {

        }

    }

    @Test()
    public void testRegisterTest5() throws Exception {
        Mockito.when(request.queryParams("username")).thenReturn("username");
        Mockito.when(request.queryParams("password")).thenReturn("password");
        Mockito.when(request.queryParams("firstname")).thenReturn("firstname");
        Mockito.when(request.queryParams("lastname")).thenReturn("lastname");
        Mockito.when(storageInterface.addUser("username", "password", "firstname",
                "lastname")).thenReturn(1);
        Assert.assertEquals(registerHandler.handle(request, response), "User added successfully");
    }

    @Test
    public void testRegisterTest6() throws Exception {
        try{
            Mockito.when(request.queryParams("username")).thenReturn("username");
            Mockito.when(request.queryParams("password")).thenReturn("password");
            Mockito.when(request.queryParams("firstname")).thenReturn("firstname");
            Mockito.when(request.queryParams("lastname")).thenReturn("lastname");
            Mockito.when(storageInterface.addUser("username", "password", "firstname",
                    "lastname")).thenThrow(TestException.class);
            registerHandler.handle(request, response);
            fail();
        } catch (Exception e) {

        }

    }




}
