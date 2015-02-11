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
 * Date: 26.06.14
 * Time: 15:56
 */
@Path(URLConstants.Resources.RESOURCES)
public class ResourceController extends AbstractResource {

    @Path("/js/{path}")
    @GET
    public Response getJS(@Context HttpContext hc, @PathParam("path") String path) {
        return AbstractResource.getFile("resources/js/" + path);
    }

    @Path("/js/images/{path}")
    @GET
    public Response getImages(@Context HttpContext hc, @PathParam("path") String path) {
        return AbstractResource.getFile("resources/js/images/" + path);
    }

    @Path("/css/{path}")
    @GET
    @Produces("text/css")
    public Response getCSS(@Context HttpContext hc, @PathParam("path") String path) {
        return AbstractResource.getFile("resources/css/" + path);
    }
}
