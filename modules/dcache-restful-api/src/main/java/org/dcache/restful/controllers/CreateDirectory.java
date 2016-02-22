package org.dcache.restful.controllers;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.json.JSONObject;

import javax.json.JsonObject;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import diskCacheV111.util.CacheException;
import diskCacheV111.util.PnfsHandler;

import org.dcache.chimera.ChimeraFsException;
import org.dcache.restful.services.InitializationService;

@Path("/CREATE")
public class CreateDirectory
{
    @Context
    ServletContext ctx;

    /**
        Example: Create new folder.
        Request:
     POST
            http://localhost:2880/restapi/CREATE/public/
            {"directoryName": "newFolder" }
    */
    @Path("/create/{path: [a-zA-Z0-9_/]+}")
    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createDirectory(@PathParam("path") String path,
                                        String requestPayload) throws InterruptedException, ChimeraFsException
    {

        JSONObject json = new JSONObject();
        try {

            JSONObject reqPayload = new JSONObject(requestPayload);

            String directoryName = (String) reqPayload.get("directoryName");



            PnfsHandler handler = InitializationService.getPnfsHandler(ctx);

            String newPath = path + "/" + directoryName;


            handler.createPnfsDirectory(newPath);

        } catch (CacheException e) {
            json.put("error", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(json.toString()).build();
        } catch (Exception e) {
            json.put("error", e.getMessage() + ". Please specify the right JSON structure.");
            return Response.status(Response.Status.NOT_ACCEPTABLE)
                    .entity(json.toString()).build();
        }

        json.put("message", "created");
        return Response.ok(json.toString()).build();



    }


    /**
        Example: Create a new broken file
            http://localhost:2880/api/v1/LIST/createEntry/public/myFolder
            {"name": "newFolder" }
   */
    @Path("/broken/{path: [a-zA-Z0-9_/]+}")
    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Produces(MediaType.APPLICATION_JSON)
    public String createPnfsEntry(@PathParam("path") String path,
                                  JsonObject request) throws InterruptedException, ChimeraFsException
    {

//        org.json.JSONObject json = new org.json.JSONObject(request.toString());
//
//        try {
//            String name = (String) json.get("name");
//
//
//            PnfsHandler handler = setPnfsHandler();
//
//            String newPath = path + "/" + name;
//            handler.createPnfsEntry(newPath);
//
//        } catch (CacheException e) {
//            return e.getMessage();
//        } catch (Exception e) {
//            return "Please specify the right Json structure " + e.getMessage();
//        }
        return "ok";
    }
}
