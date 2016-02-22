package org.dcache.restful.controllers;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import org.json.JSONArray;
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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import diskCacheV111.util.CacheException;
import diskCacheV111.util.FsPath;
import diskCacheV111.util.PnfsHandler;

import org.dcache.namespace.FileAttribute;
import org.dcache.restful.interceptor.Compress;
import org.dcache.restful.resources.FileAttributesObject;
import org.dcache.restful.services.InitializationService;
import org.dcache.util.list.DirectoryEntry;
import org.dcache.util.list.DirectoryStream;
import org.dcache.util.list.ListDirectoryHandler;


@Path("/LIST")
public class ListFile
{
    @Context
    ServletContext ctx;

    /**
     * The method offer to list the content of a directory or return metadata of
     * a specified file.
     * @return json
     * @method POST
     * @Resources URL (default)
     *            http://localhost:2880/restapi/LIST/:urlPath
     * @param urlPath (Optional)
     *        The full path of the directory to list its content or the parent path
     *        of the file to view its metadata. Default to root directory
     * @param requestPayload
     *        In the body of the request (or the request payload) these following
     *        parameter can be set:
     *          1. range (Optional): Accept only Array. Set the range at
     *              e.g. "range": [0, 10]. Default set to all
     *          2. (a) fileName (Optional):
     *             (b)fileNames (Optional): Accept only Array.
     *          3. metadata (Optional): Accept Boolean type. If set to true all available file attributes will
     *              be display. Default to false.
     *
     *       requestPayload must be provided in JSON. If none of the parameters are set, the request
     *       body must be specified as "{}" without the quotation mark otherwise an error will be
     *       thrown. See examples below for more detail.
     *
     *
     * EXAMPLES
     * (1). Return all the files in the user root directory
     *          @Request
     *              Header:
     *                  Accept: application/json
     *                  Content-Type: application/json
     *              Method:
     *                  POST
     *              URL:
     *                  http:localhost:2880/restapi/LIST/
     *              Request Payload:
     *                  {}
     *
     *          @Response
     *              {"List":
     *                [
     *                  {
     *                      "size":"512",
     *                      "creationTime":"Mon Mar 21 12:10:22 CET 2016",
     *                      "mtime":"Mon Mar 21 12:10:22 CET 2016",
     *                      "fileType":"DIR",
     *                      "fileName":"disk",
     *                      "path":"/disk"
     *                  },
     *                  {
     *                      "size":"512",
     *                      "creationTime":"Mon Mar 21 12:10:08 CET 2016",
     *                      "mtime":"Mon Mar 21 12:10:08 CET 2016",
     *                      "fileType":"DIR",
     *                      "fileName":"lost+found",
     *                      "path":"/lost+found"
     *                  },
     *                  {
     *                      "size":"512",
     *                      "creationTime":"Mon Mar 21 12:10:22 CET 2016",
     *                      "mtime":"Mon Mar 21 12:10:22 CET 2016",
     *                      "fileType":"DIR",
     *                      "fileName":"private",
     *                      "path":"/private"
     *                  }
     *                  ...
     *               ]
     *             }
     *
     * (2). Get all available file attributes (or metadata) of a file. Say there is a file called "certificate.txt"
     * inside a directory called "private" which is in the root directory and we want to get all the attributes of
     * this file.
     *      @Request
     *          Header:
     *              Accept: application/json
     *              Content-Type: application/json
     *          Method:
     *              POST
     *          URL:
     *              http:localhost:2880/restapi/LIST/private
     *          Request Payload:
     *              {"filename":"certificate.txt", "metadata":true}
     *            OR
     *              {"filenames":["certificate.txt"], "metadata":true}
     *
     *      @Response
     *          {"List":
     *              [
     *                  {
     *                      "size":"10384",
     *                      "creationTime":"Mon Mar 21 12:10:22 CET 2016",
     *                      "mtime":"Mon Mar 21 12:10:22 CET 2016",
     *                      "fileType":"REGULAR",
     *                      "fileName":"certificate.txt",
     *                      "sourcePath":"/private",
     *                      ...
     *                      "path":"/private/certificate.txt"
     *                  }
     *              ]
     *          }
     *  This is also applicable to more than one file. To get the file attributes of more than one files,
     *  the names of the files must be specified in parameter "fileNames". Note, "fileNames" is an array
     *  type, see above description.
     *
     * (3).
    */
    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Path("/{value: [a-zA-Z0-9_/]*}")
    @Produces(MediaType.APPLICATION_JSON)
    @Compress
    public List<FileAttributesObject> getFilesList(@PathParam("value") String urlPath, String requestPayload)
            throws InterruptedException, CacheException
    {

        List<FileAttributesObject> fileAttributesList = new ArrayList<>();

        try {
            FsPath path = new FsPath(urlPath);
            Range<Integer> range;
            String fileName;
            Collection<String> fileNames = new ArrayDeque<>();
            Set<FileAttribute> attributes = EnumSet.allOf(FileAttribute.class);
            boolean metaData;

            JSONObject reqPayload = new JSONObject(requestPayload);

            try {
                JSONArray listFilesByRange = (JSONArray) reqPayload.get("range");

                if (listFilesByRange.length() == 0 || (listFilesByRange.length() == 1
                        && Objects.equals(listFilesByRange.get(0).toString(), "*"))) {
                    range = Range.all();
                } else {
                    Integer lower = Integer.parseInt(listFilesByRange.get(0).toString());
                    Integer upper = Integer.parseInt(listFilesByRange.get(1).toString());

                    range = Range.range(lower, BoundType.CLOSED, upper, BoundType.CLOSED);
                }
            } catch (JSONException ex) {
                range = Range.all();
            }

            try {
                metaData = reqPayload.getBoolean("metadata");
            } catch (JSONException ex) {
                metaData = false;
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
                } catch (JSONException e) {
                    //Do Nothing
                }
            }

            ListDirectoryHandler listDirectoryHandler = InitializationService.getListDirectoryHandler(ctx);

            DirectoryStream stream = listDirectoryHandler.list(
                    InitializationService.getSubject(),
                    InitializationService.getRestriction(),
                    path,
                    null,
                    range,
                    attributes);

            PnfsHandler handler = InitializationService.getPnfsHandler(ctx);

            for (DirectoryEntry entry : stream) {

                String fName = entry.getName();

                if (fileNames.isEmpty() || fileNames.contains(fName)) {
                    fileAttributesList.add(setFileAttributes(fName, entry, handler, metaData));
                }

            }
        } catch (Exception ex) {
            FileAttributesObject fileAttributes = new FileAttributesObject();
            fileAttributes.setMessages(ex.getMessage());
            fileAttributesList.add(fileAttributes);
            return fileAttributesList;
        }

        return fileAttributesList;
    }



    private FileAttributesObject setFileAttributes(String fileName, DirectoryEntry entry, PnfsHandler handler,
                                                   boolean moreAttributes) throws CacheException
    {
        FileAttributesObject fileAttributes = new FileAttributesObject();
        fileAttributes.setFileName(fileName);

        if (moreAttributes) {
            //TODO: Set all file attributes
            fileAttributes.setMtime(entry);
            fileAttributes.setCreationTime(entry);
            fileAttributes.setSize(entry);
            fileAttributes.setFileType(entry);
            //handler.getCacheLocations(entry.getFileAttributes().getPnfsId())

            fileAttributes.setSourcePath(FsPath.getParent(fileAttributes.getPath()));
        } else {
            fileAttributes.setMtime(entry);
            fileAttributes.setCreationTime(entry);
            fileAttributes.setSize(entry);
            fileAttributes.setFileType(entry);
        }

        fileAttributes.setPath(handler.getPathByPnfsId(entry.getFileAttributes().getPnfsId()).toString());

        return fileAttributes;
    }
}