package ru.terra.btobd.server.controller;

import com.sun.jersey.api.core.HttpContext;
import org.apache.log4j.Logger;
import ru.terra.btobd.server.constants.URLConstants;
import ru.terra.server.controller.AbstractResource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 * Date: 17.11.14
 * Time: 13:32
 */
@Path(URLConstants.UI.UI)
public class UIController extends AbstractResource {
    private Logger logger = Logger.getLogger(this.getClass());

    @Path(URLConstants.UI.MAIN)
    @GET
    @Produces({"text/html"})
    public Response getMain(@Context HttpContext hc) {
        return returnHtmlFile("html/main.html");
    }

    @Path(URLConstants.UI.CHAT)
    @GET
    @Produces({"text/html"})
    public Response getChat(@Context HttpContext hc) {
        return returnHtmlFile("html/chat.html");
    }
}
