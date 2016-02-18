package org.dcache.webapi.files;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;

import diskCacheV111.poolManager.PoolMonitorV5;
import diskCacheV111.util.CacheException;
import diskCacheV111.util.FsPath;

import diskCacheV111.util.PnfsHandler;
import org.dcache.auth.GidPrincipal;
import org.dcache.auth.PasswordCredential;
import org.dcache.auth.UidPrincipal;
import org.dcache.auth.UserNamePrincipal;
import org.dcache.cells.CellStub;
import org.dcache.chimera.ChimeraFsException;
import org.dcache.util.list.DirectoryEntry;
import org.dcache.util.list.DirectoryStream;
import org.dcache.util.list.ListDirectoryHandler;

import org.dcache.webapi.data_structure.FileAttributesObject;


import javax.json.JsonObject;
import javax.security.auth.Subject;
import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static org.dcache.namespace.FileAttribute.*;


@Path("/LIST")
public class DirectoryTree {
    @Context
    ServletContext ctx;
    public final static String DL = "org.dcache.webapi";
    public final static String CS = "org.dcache.webapi.CS";


    String username = "admin";
    String password = "dickerelch";
    /*
    Example: Display the content of the folder
    Example: Passing JSON object as an argument
    Request:
    http://localhost:2880/api/v1/LIST/public (/public/testFolder1)
    or
    http://localhost:2880/api/v1/LIST/ (root).

    And Body:
   {"token" : "123", "range" :["0","2"] }

   {"token" : "123", "range" :[ ] } - for all files


     Response:
             {
            "size": 512,
            "creationTime": "Thu Feb 04 12:26:58 CET 2016",
            "mtime": "Thu Feb 04 12:26:58 CET 2016",
            "fileType": "DIR",
            "fileName": "myNewFolder",
            "path": "/public/myNewFolder",
            "messages":
            [
            ]
        },
        {
            "size": -1,
            "creationTime": "Thu Feb 04 12:42:01 CET 2016",
            "mtime": "Thu Feb 04 12:42:01 CET 2016",
            "fileType": "REGULAR",
            "fileName": "myNewFoldertest",
            "path": "/public/myNewFoldertest",
            "messages":
            [
                "Attribute is not defined: SIZE"
            ]


    */
    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Path("/{value: [a-zA-Z0-9_/]*}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<FileAttributesObject> getFilesList(@PathParam("value") String value,
                                                   JsonObject request) throws InterruptedException, CacheException {
        /*
        *TODO to check which JSON object or  JsonObject should be used
         */
        List<FileAttributesObject> fileAttributesList = new ArrayList<>();

        try {
            FsPath userRoot = new FsPath(value);

            org.json.JSONObject json = new org.json.JSONObject(request.toString());
            org.json.JSONArray files = (org.json.JSONArray) json.get("range");

            String token = (String) json.get("token");
            if (("1233").equals(token)) {
                //TODO check
            }

            ListDirectoryHandler listDirectoryHandler = (ListDirectoryHandler) (ctx.getAttribute(DL));

            //TODO for the moment we have only fixed attributes. this behaviour should be changed
            DirectoryStream stream = listDirectoryHandler.list(getSubject(username, password), userRoot, null, Range.<Integer>all(),
                    EnumSet.copyOf(Sets.union(PoolMonitorV5.getRequiredAttributesForFileLocality(),
                            EnumSet.of(MODIFICATION_TIME, TYPE, SIZE, PNFSID, CREATION_TIME))));

            PnfsHandler handler = setPnfsHandler();

            int rangeCounter = 0;

            for (DirectoryEntry entry : stream) {

                String fileName = entry.getName();

                if (files.length() == 0) {
                    fileAttributesList.add(setFileAttributes(fileName, entry, handler));

                } else {

                    if (rangeCounter >= Integer.parseInt(files.get(0).toString())
                            && rangeCounter <= Integer.parseInt(files.get(1).toString())) {
                        fileAttributesList.add(setFileAttributes(fileName, entry, handler));
                    }
                }
                rangeCounter++;
            }
        } catch (Exception ex) {
            FileAttributesObject fileAttributes = new FileAttributesObject();
            fileAttributes.setMessages(ex.getMessage());
            fileAttributesList.add(fileAttributes);
            return fileAttributesList;
        }

        return fileAttributesList;
    }


    /*
    Example: Search for  specified files only
    Example: Passing JSON object as an argument
    Request:
    http://localhost:2880/api/v1/LIST/search/public/ (folder name)
    {"token": "123", "files" :["testFile1","testFile2"] }
    or
     {"token": "123", "files" :[] } - for all files
    */
    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Path("/search/{value: [a-zA-Z0-9_/]*}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<FileAttributesObject> getFilesByName(@PathParam("value") String value,
                                                     JsonObject request) throws InterruptedException, CacheException {
        List<FileAttributesObject> fileAttributesList = new ArrayList<>();

        try {
            FsPath userRoot = new FsPath(value);

            org.json.JSONObject json = new org.json.JSONObject(request.toString());
            org.json.JSONArray files = (org.json.JSONArray) json.get("files");
            String token = (String) json.get("token");
            if (("1233").equals(token)) {
                //TODO check
            }

            Collection<String> fileNames = new ArrayDeque<>();

            for (int i = 0; i < files.length(); i++) {
                String fileName = files.get(i).toString();
                fileNames.add(fileName);
            }


            ListDirectoryHandler listDirectoryHandler = (ListDirectoryHandler) (ctx.getAttribute(DL));


            DirectoryStream stream = listDirectoryHandler.list(getSubject(username, password), userRoot, null, Range.<Integer>all(),
                    EnumSet.copyOf(Sets.union(PoolMonitorV5.getRequiredAttributesForFileLocality(),
                            EnumSet.of(MODIFICATION_TIME, TYPE, SIZE, PNFSID, LOCATIONS))));


            PnfsHandler handler = setPnfsHandler();

            for (DirectoryEntry entry : stream) {

                String fileName = entry.getName();
                if (fileNames.isEmpty() || fileNames.contains(fileName)) {

                    fileAttributesList.add(setFileAttributes(fileName, entry, handler));
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

    /*
       Example: Move files/folders from one folder to another (USING PNFsID - right one)

       ex. 1 : move file "testFile7" from the "public" folder to  "myFolder"
       Request:
       http://localhost:2880/api/v1/LIST/move/public/testFile7
       {"token": "123", "destination": "public/myNewFolder" }

        ex. 2 :
       move "myFolder" to  "public/myNewFolder" folder

        http://localhost:2880/api/v1/LIST/move/public/myFolder
       {"token": "123", "destination": "public/myNewFolder" }

    */
    @Path("/move/{path: [a-zA-Z0-9_/]+}")
    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Produces(MediaType.APPLICATION_JSON)
    public String move(@PathParam("path") String path,
                       JsonObject request) throws InterruptedException, ChimeraFsException {
        try {
            org.json.JSONObject json = new org.json.JSONObject(request.toString());
            String destination = (String) json.get("destination");
            String token = (String) json.get("token");
            if (("1233").equals(token)) {
                //TODO check
            }

            PnfsHandler handler = setPnfsHandler();

            String destinationName = destination + "/" + path.substring(path.lastIndexOf("/"), path.length());
            handler.renameEntry(handler.getPnfsIdByPath(path), path, destinationName, true);

        } catch (CacheException e) {
            return e.getMessage();
        } catch (Exception e) {
            return "Please specify the right Json structure " + e.getMessage();
        }

        return "ok";
    }

    /*
    Rename  files or folders, this includes as well the move property
    the name of a file or a folder must be specified in the destination structure

    Request:
    http://localhost:2880/api/v1/LIST/rename/public/testfolder2

    {"token": "123", "destination": "public/testfolder4/testfolder5" }
     */
    @Path("/rename/{path: [a-zA-Z0-9_/]+}")
    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Produces(MediaType.APPLICATION_JSON)
    public String rename(@PathParam("path") String path,
                         JsonObject request) throws InterruptedException, ChimeraFsException {
        try {
            org.json.JSONObject json = new org.json.JSONObject(request.toString());
            String destination = (String) json.get("destination");
            String token = (String) json.get("token");
            if (("1233").equals(token)) {
                    //TODO check
            }

            PnfsHandler handler = setPnfsHandler();
            handler.renameEntry(handler.getPnfsIdByPath(path), path, destination, false);

        } catch (CacheException e) {
            return e.getMessage();
        } catch (Exception e) {
            return "Please specify the right Json structure " + e.getMessage();
        }

        return "ok";
    }

    /*
   Example: Create new folder.
   Request:
   http://localhost:2880/api/v1/LIST/create/public/
   {"token": "123",  "name": "newFolder" }
   */
    @Path("/create/{path: [a-zA-Z0-9_/]+}")
    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Produces(MediaType.APPLICATION_JSON)
    public String createPnfsDirectory(@PathParam("path") String path,
                                      JsonObject request) throws InterruptedException, ChimeraFsException {

        try {
            org.json.JSONObject json = new org.json.JSONObject(request.toString());
            String name = (String) json.get("name");
            String token = (String) json.get("token");
            if (("1233").equals(token)) {
                //TODO check
            }

            PnfsHandler handler = setPnfsHandler();

            String newPath = path + "/" + name;
            handler.createPnfsDirectory(newPath);

        } catch (CacheException e) {
            return e.getMessage();
        } catch (Exception e) {
            return "Please specify the right Json structure " + e.getMessage();
        }
        return "ok";
    }


    /*
   Example: Create new folder
   http://localhost:2880/api/v1/LIST/createEntry/public/myFolder
   {"token": "123", "name": "newFolder" }
   */
    @Path("/createEntry/{path: [a-zA-Z0-9_/]+}")
    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Produces(MediaType.APPLICATION_JSON)
    public String createPnfsEntry(@PathParam("path") String path,
                                  JsonObject request) throws InterruptedException, ChimeraFsException {

        org.json.JSONObject json = new org.json.JSONObject(request.toString());

        try {
            String name = (String) json.get("name");


            PnfsHandler handler = setPnfsHandler();

            String newPath = path + "/" + name;
            handler.createPnfsEntry(newPath);

        } catch (CacheException e) {
            return e.getMessage();
        } catch (Exception e) {
            return "Please specify the right Json structure " + e.getMessage();
        }
        return "ok";
    }


    public Subject getSubject(String userName, String password) {


        Subject subject = new Subject();
        PasswordCredential pass =
                new PasswordCredential(userName, String.valueOf(password));
        UserNamePrincipal principals = new UserNamePrincipal(userName);
        UidPrincipal uidPrincipal = new UidPrincipal("0");
        subject.getPrincipals().add(new GidPrincipal(0, true));
        subject.getPrincipals().add(principals);
        subject.getPrincipals().add(uidPrincipal);
        subject.getPrincipals().add(uidPrincipal);


        subject.getPublicCredentials().add(pass);
        subject.getPrivateCredentials().add(pass);
        subject.getPrincipals().add(uidPrincipal);

        return subject;

    }


    /*
    Set file attributes
     */

    public FileAttributesObject setFileAttributes(String fileName, DirectoryEntry entry, PnfsHandler handler) throws CacheException {


        FileAttributesObject fileAttributes = new FileAttributesObject();
        fileAttributes.setFileName(fileName);
        fileAttributes.setMtime(entry);
        fileAttributes.setCreationTime(entry);
        fileAttributes.setSize(entry);
        fileAttributes.setFileType(entry);
        fileAttributes.setPath(handler.getPathByPnfsId(entry.getFileAttributes().getPnfsId()).toString());
        return fileAttributes;
    }

    public PnfsHandler setPnfsHandler(){

        CellStub cellStub = (CellStub) (ctx.getAttribute(CS));
        PnfsHandler handler = new PnfsHandler(cellStub);
        handler.setSubject(getSubject(username, password));

        return handler;
    }


}