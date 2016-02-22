package org.dcache.restful.controllers;

import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.Objects;

import diskCacheV111.util.CacheException;
import diskCacheV111.util.PnfsHandler;

import org.dcache.chimera.ChimeraFsException;
import org.dcache.restful.services.InitializationService;

@Path("/RENAME")
public class RenameFile
{
    @Context
    ServletContext ctx;
    /**
     Rename  file or folder

     Request:
     http://localhost:2880/restapi/RENAME/public/

     {"currentName": "testfolder2", "newName": "testfolder5"}
     */
    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Path("/{path: [a-zA-Z0-9_/]*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response renameFile(@PathParam("path") String path,
                           String requestPayload) throws InterruptedException, ChimeraFsException
    {
        JSONObject json = new JSONObject();
        String newName;
        String currentName;
        try {

            if (Objects.equals(path, "")){
                path = "/";
            }

            if (!path.startsWith("/")) {
                path = "/"+ path;
            }

            if (!Objects.equals(path.substring(path.length() - 1), "/")){
                path = path + "/";
            }

            JSONObject reqPayload = new JSONObject(requestPayload);
            try {
                newName = (String) reqPayload.get("newName");

            } catch (JSONException ex){
                json.put("error", ex.getMessage());
                return Response.status(Response.Status.NOT_ACCEPTABLE)
                        .entity(json.toString()).build();
            }

            try {
                currentName = (String) reqPayload.get("currentName");
            } catch (JSONException ex) {
                json.put("error", ex.getMessage());
                return Response.status(Response.Status.NOT_ACCEPTABLE)
                        .entity(json.toString()).build();
            }

            String source = path + currentName;


            String target = path + newName;


            PnfsHandler handler  = InitializationService.getPnfsHandler(ctx);
            handler.renameEntry(handler.getPnfsIdByPath(source), source, target, false);

        } catch (CacheException e) {
            json.put("error", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(json.toString()).build();
        } catch (Exception e) {
            json.put("error", e.getMessage() + ". Please specify the right JSON structure.");
            return Response.status(Response.Status.NOT_ACCEPTABLE)
                    .entity(json.toString()).build();
        }

        json.put("message", "done");
        return Response.ok(json.toString()).build();
    }
}
