package org.dcache.restful.controllers;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.ArrayDeque;
import java.util.Collection;

import diskCacheV111.util.CacheException;
import diskCacheV111.util.PnfsHandler;

import org.dcache.chimera.ChimeraFsException;
import org.dcache.restful.services.InitializationService;

@Path("/MOVE")
public class MoveFile
{
    @Context
    ServletContext ctx;

    /**
       Example: Move files/folders from one folder to another (USING PNFsID - right one)

       ex. 1 : move file "testFile7" from the "public" folder to  "myFolder"
       Request:
       http://localhost:2880/api/v1/MOVE/
       {"fileName":"testFile7", "destinationPath": "public/myNewFolder" }

        ex. 2 :
       move "myFolder" which is in "/private/" folder to  "/public/" folder

        http://localhost:2880/api/v1/UTIL/move/public
       {"fileName":"myFolder", "sourceFullPath":"/private/", "destinationFullPath": "/public/" }

     ex. 3 :
     move list of files which are in "/private/" folder to  "/public/" folder

     http://localhost:2880/api/v1/UTIL/move/public
     {"fileNames":["myFolder", ...], "sourceFullPath":"/private/", "destinationFullPath": "/public/" }

    */
    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Produces(MediaType.APPLICATION_JSON)
    public Response moveFile(String requestPayload) throws InterruptedException, ChimeraFsException
    {
        JSONObject json = new JSONObject();
        String fileName;
        String sourcePath;
        String destinationPath;
        String target;
        String source;
        Collection<String> fileNames = new ArrayDeque<>();

        try {

            JSONObject reqPayload = new JSONObject(requestPayload);

            try {
                sourcePath = (String) reqPayload.get("sourceFullPath");
            } catch (JSONException ex) {
                json.put("error", ex.getMessage());
                return Response.status(Response.Status.NOT_ACCEPTABLE)
                        .entity(json.toString()).build();
            }

            try {
                destinationPath = (String) reqPayload.get("destinationFullPath");
            } catch (JSONException ex) {
                json.put("error", ex.getMessage());
                return Response.status(Response.Status.NOT_ACCEPTABLE)
                        .entity(json.toString()).build();
            }

            try {
                fileName = (String) reqPayload.get("fileName");
                fileNames.add(fileName);
            } catch (JSONException ex) {
                try {
                    JSONArray listFilesByNames = (JSONArray) reqPayload.get("fileNames");
                    if (listFilesByNames.length() > 0){
                        for (int i = 0; i < listFilesByNames.length(); i++) {
                            fileName = listFilesByNames.get(i).toString();
                            fileNames.add(fileName);
                        }
                    }
                } catch (JSONException e){
                    json.put("error", e.getMessage() + ". You need to specify either filename or filnames.");
                    return Response.status(Response.Status.NOT_ACCEPTABLE)
                            .entity(json.toString()).build();
                }
            }

            PnfsHandler handler = InitializationService.getPnfsHandler(ctx);

            //TODO: check if the list of files specified exist before start to move OR continue to move the file and
            //Report any files that doesn't exits
            for (String fn : fileNames) {
                target = destinationPath + "/" + fn;
                source = sourcePath + "/" + fn;

                handler.renameEntry(handler.getPnfsIdByPath(source), source, target, true);
            }

        } catch (CacheException e) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_ACCEPTABLE)
                    .entity("Please specify the right Json structure " + e.getMessage()).build();
        }

        json.put("message", "done");
        return Response.status(Response.Status.MOVED_PERMANENTLY)
                .entity(json.toString()).build();
    }

}
