package org.dcache.restful.controllers;

import org.json.JSONObject;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.Objects;

@Path("/DELETE")
public class DeleteFile
{
    /**
     * {"filePath":""}
     *
     * {"filePaths":[...]}
     **/
    @POST
    @Path("/{id}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response deleteFile(@PathParam("id") String id, String requestPayLoad)
    {
        JSONObject json = new JSONObject();

        //String json = "{\"name\":\""+id+"\"}";
        if (Objects.equals(id, "home")){
            id = "/";
        }

        json.put("message", "done");
        return Response.ok(json.toString()).build();
    }
}
