package ru.terra.btobd.server.controller;

import com.sun.jersey.api.core.HttpContext;
import ru.terra.btobd.server.constants.URLConstants;
import ru.terra.server.controller.AbstractResource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 * Date: 17.12.14
 * Time: 11:32
 */
@Path(URLConstants.JabberWebClient.JWC)
public class JabberWebClientController extends AbstractResource {
    @Path(URLConstants.JabberWebClient.MAIN)
    @GET
    @Produces({"text/html"})
    public Response getMain(@Context HttpContext hc) {
//        if (isAuthorized(hc))
            return returnHtmlFile("jwc/jwc.html");
//        else
//            return Response.noContent().build();
    }

    @Path("css/{path}")
    @GET
    @Produces("text/css")
    public Response getCss(@Context HttpContext hc, @PathParam("path") String path) {
        return AbstractResource.getFile("jwc/css/" + path);
    }

}
