package org.dcache.webapi.data_structure;


import org.dcache.util.list.DirectoryEntry;

import java.util.*;

/**
 * Created by sahakya on 12/21/15.
 */
public class FileAttributesObject {

    /**
     * NFSv4 Access control list.
     */
    private String acl;

    /**
     * file's size
     */
    private String size;

    /**
     * file's attribute change time
     */
    private long ctime;

    /**
     * file's creation time
     */
    private String creationTime;

    /**
     * file's last access time
     */
    private long atime;

    /**
     * file's last modification time
     */
    private String mtime;

    /**
     * file's known checksums
     */
    private Set<String> checksums;

    /**
     * file's owner's id
     */
    private int owner;

    /**
     * file's group id
     */
    private int group;

    /**
     * POSIX.1 file mode
     */
    private int mode;

    /**
     * file's access latency ( e.g. ONLINE/NEARLINE )
     */
    private String accessLatency;

    /**
     * file's retention policy ( e.g. CUSTODIAL/REPLICA )
     */
    private String retentionPolicy;

    /**
     * type of the file ( e.g. REG, DIR, LINK, SPECIAL )
     */
    private String fileType;

    /**
     * File locations within dCache.
     */
    private Collection<String> locations;

    /**
     * Key value map of flags associated with the file.
     */
    private Map<String, String> flags;

    /**
     * The unique PNFS ID of a file.
     */
    private String pnfsId;

    /**
     * The storage info of a file.
     */
    private String storageInfo;

    /**
     * The storage class of a file.
     */
    private String storageClass;

    /**
     * The HSM of a file.
     */
    private String hsm;

    /**
     * The cache class of a file.
     */
    private String cacheClass;

    /**
     * The name of a file.
     */
    private String fileName;

    /**
     * The current parent directory of a file.
     */
    private String sourcePath;

    /**
     * The new directory of a file, where the file will be moved.
     */
    private String newPath;


    /**
     * The file path.
     */
    private String path;

    /**
     * Used to store error messages for  broken files.
     */
    private List<String> messages = new ArrayList<>();


    public String getAcl() {
        return acl;
    }

    public void setAcl(String acl) {
        this.acl = acl;
    }

    public String getSize() {
        return size;
    }

    public void setSize(DirectoryEntry entry) {
        try {
            this.size = String.valueOf( entry.getFileAttributes().getSize());

        } catch (IllegalStateException ex) {
            this.size = ex.getMessage();
            setMessages(ex.getMessage());

        }
    }

    public long getCtime() {
        return ctime;
    }

    public void setCtime(long ctime) {
        this.ctime = ctime;
    }

    public String getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(DirectoryEntry entry) {
        try {
            this.creationTime = new Date(entry.getFileAttributes().getCreationTime()).toString();

        } catch (IllegalStateException ex) {
            this.creationTime = ex.getMessage();
            setMessages(ex.getMessage());

        }
    }

    public long getAtime() {
        return atime;
    }

    public void setAtime(long atime) {
        this.atime = atime;
    }

    public String getMtime() {
        return this.mtime;
    }

    public void setMtime(DirectoryEntry entry) {

        try {
            this.mtime = new Date(entry.getFileAttributes().getModificationTime()).toString();

        } catch (IllegalStateException ex) {
            this.mtime = ex.getMessage();
            setMessages(ex.getMessage());

        }
    }

    public Set<String> getChecksums() {
        return checksums;
    }

    public void setChecksums(Set<String> checksums) {
        this.checksums = checksums;
    }

    public int getOwner() {
        return owner;
    }

    public void setOwner(int owner) {
        this.owner = owner;
    }

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public String getAccessLatency() {
        return accessLatency;
    }

    public void setAccessLatency(String accessLatency) {
        this.accessLatency = accessLatency;
    }

    public String getRetentionPolicy() {
        return retentionPolicy;
    }

    public void setRetentionPolicy(String retentionPolicy) {
        this.retentionPolicy = retentionPolicy;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(DirectoryEntry entry) {
        try {
            this.fileType = entry.getFileAttributes().getFileType().toString();

        } catch (IllegalStateException ex) {
            this.fileType = ex.getMessage();
            setMessages(ex.getMessage());

        }
    }

    public Collection<String> getLocations() {
        return locations;
    }

    public void setLocations(Collection<String> locations) {
        this.locations = locations;
    }

    public Map<String, String> getFlags() {
        return flags;
    }

    public void setFlags(Map<String, String> flags) {
        this.flags = flags;
    }

    public String getPnfsId() {
        return pnfsId;
    }

    public void setPnfsId(String pnfsId) {
        this.pnfsId = pnfsId;
    }

    public String getStorageInfo() {
        return storageInfo;
    }

    public void setStorageInfo(String storageInfo) {
        this.storageInfo = storageInfo;
    }

    public String getStorageClass() {
        return storageClass;
    }

    public void setStorageClass(String storageClass) {
        this.storageClass = storageClass;
    }

    public String getHsm() {
        return hsm;
    }

    public void setHsm(String hsm) {
        this.hsm = hsm;
    }

    public String getCacheClass() {
        return cacheClass;
    }

    public void setCacheClass(String cacheClass) {
        this.cacheClass = cacheClass;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getNewPath() {
        return newPath;
    }

    public void setNewPath(String newPath) {
        this.newPath = newPath;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(String messages) {
        this.messages.add(messages);
    }
}
