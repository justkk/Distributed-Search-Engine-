package edu.upenn.cis.cis455.handlers;

import edu.upenn.cis.cis455.crawler.handlers.HelloHandler;
import edu.upenn.cis.cis455.model.User;
import edu.upenn.cis.cis455.model.UserInfoManager;
import edu.upenn.cis.cis455.storage.StorageInterfaceImpl;
import edu.upenn.cis.cis455.storage.managers.SubcriptionManger;
import edu.upenn.cis.cis455.storage.managers.UserChannelDataManager;
import junit.framework.TestCase;
import org.apache.logging.log4j.Level;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import spark.HaltException;
import spark.Request;
import spark.Response;
import spark.Session;

import java.util.ArrayList;

public class HelloHandlerTest extends TestCase {

    private StorageInterfaceImpl storageInterface;
    private HelloHandler helloHandler;
    private Session session;
    private Request request;
    private Response response;
    private UserInfoManager userInfoManager;
    private UserChannelDataManager userChannelDataManager;
    private SubcriptionManger subcriptionManger;
    private User user;


    @Before
    public void setUp() {
        org.apache.logging.log4j.core.config.Configurator.setLevel("edu.upenn.cis.cis455", Level.DEBUG);
        storageInterface = Mockito.mock(StorageInterfaceImpl.class);
        helloHandler = new HelloHandler(storageInterface);
        session = Mockito.mock(Session.class);
        request = Mockito.mock(Request.class);
        response = Mockito.mock(Response.class);
        userInfoManager = Mockito.mock(UserInfoManager.class);
        userChannelDataManager = Mockito.mock(UserChannelDataManager.class);
        subcriptionManger = Mockito.mock(SubcriptionManger.class);
        user = Mockito.mock(User.class);
        Mockito.when(request.session()).thenReturn(session);
        Mockito.when(storageInterface.getUserInfoManager()).thenReturn(userInfoManager);
        Mockito.when(storageInterface.getUserChannelDataManager()).thenReturn(userChannelDataManager);
        Mockito.when(storageInterface.getSubcriptionManger()).thenReturn(subcriptionManger);
        Mockito.when(userChannelDataManager.getAllChannels()).thenReturn(new ArrayList<>());
        Mockito.when(subcriptionManger.getSubList(Matchers.anyInt())).thenReturn(new ArrayList<>());
    }

    @Test(expected = HaltException.class)
    public void invalidUserName() throws Exception {
        Mockito.when(session.attribute("user")).thenReturn(null);
        helloHandler.handle(request, response);
    }

    @Test(expected = HaltException.class)
    public void invalidUser() throws Exception {
        Mockito.when(session.attribute("user")).thenReturn("test");
        Mockito.when(userInfoManager.getUserFromUserName("test")).thenReturn(null);
        helloHandler.handle(request, response);
    }

    @Test()
    public void validUser() throws Exception {
        Mockito.when(session.attribute("user")).thenReturn("test");
        Mockito.when(userInfoManager.getUserFromUserName("test")).thenReturn(user);
        Mockito.when(user.getFirstName()).thenReturn("");
        Mockito.when(user.getLastName()).thenReturn("");
        String output = (String) helloHandler.handle(request, response);
        String body = "<html><body>" + "Hello " + " " + "</body></html>";
        //Assert.assertEquals(body, output);
    }

    @Test
    public void testInvalidUserName() throws Exception {
        try {
            Mockito.when(session.attribute("user")).thenReturn(null);
            helloHandler.handle(request, response);
            fail();
        } catch (HaltException e) {
            // success
        }

    }

    @Test
    public void testInvalidUser() throws Exception {
        try {
            Mockito.when(session.attribute("user")).thenReturn("test");
            Mockito.when(userInfoManager.getUserFromUserName("test")).thenReturn(null);
            helloHandler.handle(request, response);
            fail();
        } catch (HaltException e) {
            // success
        }

    }

    @Test
    public void testIvalidUser() throws Exception {
        Mockito.when(session.attribute("user")).thenReturn("test");
        Mockito.when(userInfoManager.getUserFromUserName("test")).thenReturn(user);
        Mockito.when(user.getFirstName()).thenReturn("");
        Mockito.when(user.getLastName()).thenReturn("");
        String output = (String) helloHandler.handle(request, response);
        String body = "<html><body>" + "Hello " + " " + "</body></html>";
        //Assert.assertEquals(body, output);
    }
}
