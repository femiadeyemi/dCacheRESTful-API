//______________________________________________________________________________
//
// $Id$
// $Author$
//
//______________________________________________________________________________
/*
COPYRIGHT STATUS:
Dec 1st 2001, Fermi National Accelerator Laboratory (FNAL) documents and
software are sponsored by the U.S. Department of Energy under Contract
No. DE-AC02-76CH03000. Therefore, the U.S. Government retains a
world-wide non-exclusive, royalty-free license to publish or reproduce
these documents and software for U.S. Government purposes.  All
documents and software available from this server are protected under
the U.S. and Foreign Copyright Laws, and FNAL reserves all rights.


 Distribution of the software available from this server is free of
 charge subject to the user following the terms of the Fermitools
 Software Legal Information.

 Redistribution and/or modification of the software shall be accompanied
 by the Fermitools Software Legal Information  (including the copyright
 notice).

 The user is asked to feed back problems, benefits, and/or suggestions
 about the software to the Fermilab Software Providers.


Neither the name of Fermilab, the  URA, nor the names of the
contributors may be used to endorse or promote products derived from
this software without specific prior written permission.

DISCLAIMER OF LIABILITY (BSD):

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED  WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED  WARRANTIES OF MERCHANTABILITY AND FITNESS
FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL FERMILAB,
OR THE URA, OR THE U.S. DEPARTMENT of ENERGY, OR CONTRIBUTORS BE LIABLE
FOR  ANY  DIRECT, INDIRECT,  INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
OF SUBSTITUTE  GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY  OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT  OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE  POSSIBILITY OF SUCH DAMAGE.


Liabilities of the Government:

This software is provided by URA, independent from its Prime Contract
with the U.S. Department of Energy. URA is acting independently from
the Government and in its own private capacity and is not acting on
behalf of the U.S. Government, nor as its contractor nor its agent.
Correspondingly, it is understood and agreed that the U.S. Government
has no connection to this software and in no manner whatsoever shall
be liable for nor assume any responsibility or obligation for any claim,
cost, or damages arising out of or resulting from the use of the
software available from this server.


Export Control:

All documents and software available from this server are subject to
U.S. export control laws.  Anyone downloading information from this
server is obligated to secure any necessary Government licenses before
exporting documents or software obtained from this server.
 */

package org.dcache.srm.server;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.common.net.InetAddresses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.Set;

import org.dcache.commons.stats.RequestCounters;
import org.dcache.commons.stats.RequestExecutionTimeGauges;
import org.dcache.srm.AbstractStorageElement;
import org.dcache.srm.SRM;
import org.dcache.srm.SRMAuthenticationException;
import org.dcache.srm.SRMAuthorizationException;
import org.dcache.srm.SRMInternalErrorException;
import org.dcache.srm.SRMUser;
import org.dcache.srm.handler.CredentialAwareHandler;
import org.dcache.srm.util.Axis;
import org.dcache.srm.util.Configuration;
import org.dcache.srm.util.JDC;
import org.dcache.srm.v2_2.ArrayOfTExtraInfo;
import org.dcache.srm.v2_2.ISRM;
import org.dcache.srm.v2_2.SrmAbortFilesRequest;
import org.dcache.srm.v2_2.SrmAbortFilesResponse;
import org.dcache.srm.v2_2.SrmAbortRequestRequest;
import org.dcache.srm.v2_2.SrmAbortRequestResponse;
import org.dcache.srm.v2_2.SrmBringOnlineRequest;
import org.dcache.srm.v2_2.SrmBringOnlineResponse;
import org.dcache.srm.v2_2.SrmChangeSpaceForFilesRequest;
import org.dcache.srm.v2_2.SrmChangeSpaceForFilesResponse;
import org.dcache.srm.v2_2.SrmCheckPermissionRequest;
import org.dcache.srm.v2_2.SrmCheckPermissionResponse;
import org.dcache.srm.v2_2.SrmCopyRequest;
import org.dcache.srm.v2_2.SrmCopyResponse;
import org.dcache.srm.v2_2.SrmExtendFileLifeTimeInSpaceRequest;
import org.dcache.srm.v2_2.SrmExtendFileLifeTimeInSpaceResponse;
import org.dcache.srm.v2_2.SrmExtendFileLifeTimeRequest;
import org.dcache.srm.v2_2.SrmExtendFileLifeTimeResponse;
import org.dcache.srm.v2_2.SrmGetPermissionRequest;
import org.dcache.srm.v2_2.SrmGetPermissionResponse;
import org.dcache.srm.v2_2.SrmGetRequestSummaryRequest;
import org.dcache.srm.v2_2.SrmGetRequestSummaryResponse;
import org.dcache.srm.v2_2.SrmGetRequestTokensRequest;
import org.dcache.srm.v2_2.SrmGetRequestTokensResponse;
import org.dcache.srm.v2_2.SrmGetSpaceMetaDataRequest;
import org.dcache.srm.v2_2.SrmGetSpaceMetaDataResponse;
import org.dcache.srm.v2_2.SrmGetSpaceTokensRequest;
import org.dcache.srm.v2_2.SrmGetSpaceTokensResponse;
import org.dcache.srm.v2_2.SrmGetTransferProtocolsRequest;
import org.dcache.srm.v2_2.SrmGetTransferProtocolsResponse;
import org.dcache.srm.v2_2.SrmLsRequest;
import org.dcache.srm.v2_2.SrmLsResponse;
import org.dcache.srm.v2_2.SrmMkdirRequest;
import org.dcache.srm.v2_2.SrmMkdirResponse;
import org.dcache.srm.v2_2.SrmMvRequest;
import org.dcache.srm.v2_2.SrmMvResponse;
import org.dcache.srm.v2_2.SrmPingRequest;
import org.dcache.srm.v2_2.SrmPingResponse;
import org.dcache.srm.v2_2.SrmPrepareToGetRequest;
import org.dcache.srm.v2_2.SrmPrepareToGetResponse;
import org.dcache.srm.v2_2.SrmPrepareToPutRequest;
import org.dcache.srm.v2_2.SrmPrepareToPutResponse;
import org.dcache.srm.v2_2.SrmPurgeFromSpaceRequest;
import org.dcache.srm.v2_2.SrmPurgeFromSpaceResponse;
import org.dcache.srm.v2_2.SrmPutDoneRequest;
import org.dcache.srm.v2_2.SrmPutDoneResponse;
import org.dcache.srm.v2_2.SrmReleaseFilesRequest;
import org.dcache.srm.v2_2.SrmReleaseFilesResponse;
import org.dcache.srm.v2_2.SrmReleaseSpaceRequest;
import org.dcache.srm.v2_2.SrmReleaseSpaceResponse;
import org.dcache.srm.v2_2.SrmReserveSpaceRequest;
import org.dcache.srm.v2_2.SrmReserveSpaceResponse;
import org.dcache.srm.v2_2.SrmResumeRequestRequest;
import org.dcache.srm.v2_2.SrmResumeRequestResponse;
import org.dcache.srm.v2_2.SrmRmRequest;
import org.dcache.srm.v2_2.SrmRmResponse;
import org.dcache.srm.v2_2.SrmRmdirRequest;
import org.dcache.srm.v2_2.SrmRmdirResponse;
import org.dcache.srm.v2_2.SrmSetPermissionRequest;
import org.dcache.srm.v2_2.SrmSetPermissionResponse;
import org.dcache.srm.v2_2.SrmStatusOfBringOnlineRequestRequest;
import org.dcache.srm.v2_2.SrmStatusOfBringOnlineRequestResponse;
import org.dcache.srm.v2_2.SrmStatusOfChangeSpaceForFilesRequestRequest;
import org.dcache.srm.v2_2.SrmStatusOfChangeSpaceForFilesRequestResponse;
import org.dcache.srm.v2_2.SrmStatusOfCopyRequestRequest;
import org.dcache.srm.v2_2.SrmStatusOfCopyRequestResponse;
import org.dcache.srm.v2_2.SrmStatusOfGetRequestRequest;
import org.dcache.srm.v2_2.SrmStatusOfGetRequestResponse;
import org.dcache.srm.v2_2.SrmStatusOfLsRequestRequest;
import org.dcache.srm.v2_2.SrmStatusOfLsRequestResponse;
import org.dcache.srm.v2_2.SrmStatusOfPutRequestRequest;
import org.dcache.srm.v2_2.SrmStatusOfPutRequestResponse;
import org.dcache.srm.v2_2.SrmStatusOfReserveSpaceRequestRequest;
import org.dcache.srm.v2_2.SrmStatusOfReserveSpaceRequestResponse;
import org.dcache.srm.v2_2.SrmStatusOfUpdateSpaceRequestRequest;
import org.dcache.srm.v2_2.SrmStatusOfUpdateSpaceRequestResponse;
import org.dcache.srm.v2_2.SrmSuspendRequestRequest;
import org.dcache.srm.v2_2.SrmSuspendRequestResponse;
import org.dcache.srm.v2_2.SrmUpdateSpaceRequest;
import org.dcache.srm.v2_2.SrmUpdateSpaceResponse;
import org.dcache.srm.v2_2.TExtraInfo;
import org.dcache.srm.v2_2.TReturnStatus;
import org.dcache.srm.v2_2.TStatusCode;
import org.dcache.util.NetLoggerBuilder;

import static java.util.Arrays.asList;
import static org.dcache.srm.v2_2.TStatusCode.*;

public class SRMServerV2 implements ISRM  {

    private static final Set<TStatusCode> FAILURES =
            ImmutableSet.of(SRM_FAILURE,
                    SRM_AUTHENTICATION_FAILURE, SRM_AUTHORIZATION_FAILURE, SRM_INVALID_REQUEST, SRM_INVALID_PATH,
                    SRM_FILE_LIFETIME_EXPIRED, SRM_SPACE_LIFETIME_EXPIRED, SRM_EXCEED_ALLOCATION, SRM_NO_USER_SPACE,
                    SRM_NO_FREE_SPACE, SRM_DUPLICATION_ERROR, SRM_NON_EMPTY_DIRECTORY, SRM_TOO_MANY_RESULTS,
                    SRM_INTERNAL_ERROR, SRM_FATAL_INTERNAL_ERROR, SRM_NOT_SUPPORTED, SRM_ABORTED,
                    SRM_REQUEST_TIMED_OUT, SRM_FILE_BUSY, SRM_FILE_LOST, SRM_FILE_UNAVAILABLE, SRM_CUSTOM_STATUS);

    private static final Logger LOGGER = LoggerFactory.getLogger(SRMServerV2.class);

    private final SrmAuthorizer srmAuth;
    private final boolean isClientDNSLookup;
    private final AbstractStorageElement storage;
    private final RequestCounters<Class<?>> srmServerCounters;
    private final RequestExecutionTimeGauges<Class<?>> srmServerGauges;
    private final SRM srm;
    private final RequestLogger[] loggers =
            { new RequestExecutionTimeGaugeLogger(), new CounterLogger(), new AccessLogger() };

    private final ArrayOfTExtraInfo pingExtraInfo;
    private final boolean isEnabled;

    public SRMServerV2()
    {
        srm = Axis.getSRM();
        storage = Axis.getStorage();
        Configuration config = Axis.getConfiguration();
        srmAuth = new SrmAuthorizer(Axis.getSrmAuthorization(),
                srm.getRequestCredentialStorage(),
                config.isClientDNSLookup(),
                Axis.getVomsAcValidator());
        srmServerCounters = srm.getSrmServerV2Counters();
        srmServerGauges = srm.getSrmServerV2Gauges();
        pingExtraInfo = buildExtraInfo(config.getPingExtraInfo());
        isClientDNSLookup = config.isClientDNSLookup();
        isEnabled = asList(config.getVersions()).contains("2");
    }

    private ArrayOfTExtraInfo buildExtraInfo(Map<String,String> items)
    {
        if (items.isEmpty()) {
            return null;
        }

        TExtraInfo[] extraInfo = new TExtraInfo[items.size()];
        int i = 0;
        for (Map.Entry<String,String> item : items.entrySet()) {
            extraInfo [i++] = new TExtraInfo(item.getKey(),
                    Strings.emptyToNull(item.getValue()));
        }

        return new ArrayOfTExtraInfo(extraInfo);
    }

    private Object handleRequest(String requestName, Object request)  throws RemoteException
    {
        if (!isEnabled) {
            LOGGER.warn("Rejecting SRM v2 client request from '{}' by '{}' because SRM v2 is disabled.",
                        Axis.getRemoteAddress(), Axis.getDN().orElse(""));
            throw new java.rmi.RemoteException("SRM version 2 is not supported by this server.");
        }
        long startTimeStamp = System.currentTimeMillis();
        // requestName values all start "srm".  This is redundant, so may
        // be removed when creating the session id.  The initial character is
        // converted to lowercase, so "srmPrepareToPut" becomes "prepareToPut".
        String session = "srm2:" +
                Character.toLowerCase(requestName.charAt(3)) +
                requestName.substring(4);
        try (JDC ignored = JDC.createSession(session)) {
            for (RequestLogger logger : loggers) {
                logger.request(requestName, request);
            }
            SRMUser user = null;
            Object response;
            try {
                user = srmAuth.getRequestUser();
                response = dispatch(user, requestName, request);
            } catch (SRMInternalErrorException e) {
                LOGGER.error(e.getMessage());
                response = getFailedResponse(requestName, e.getStatusCode(),
                        "Authentication failed (server log contains additional information).");
            } catch (SRMAuthorizationException e) {
                LOGGER.info(e.getMessage());
                response = getFailedResponse(requestName, e.getStatusCode(), "Permission denied.");
            } catch (SRMAuthenticationException e) {
                LOGGER.warn(e.getMessage());
                response = getFailedResponse(requestName, e.getStatusCode(),
                         "Authentication failed (server log contains additional information).");
            }
            long time = System.currentTimeMillis() - startTimeStamp;
            for (RequestLogger logger : loggers) {
                logger.response(requestName, request, response, user, time);
            }
            return response;
        }
    }

    private Object dispatch(SRMUser user, String requestName, Object request) throws RemoteException
    {
        Class<?> requestClass = request.getClass();
        String capitalizedRequestName =
                Character.toUpperCase(requestName.charAt(0))+
                requestName.substring(1);
        try {
            LOGGER.debug("About to call {} handler", requestName);
            Constructor<?> handlerConstructor;
            Object handler;
            Method handleGetResponseMethod;

            String remoteIP = Axis.getRemoteAddress();
            String remoteHost = isClientDNSLookup ?
                    InetAddresses.forString(remoteIP).getCanonicalHostName() : remoteIP;

            try {
                Class<?> handlerClass = Class.forName("org.dcache.srm.handler."+
                        capitalizedRequestName);
                handlerConstructor =
                        handlerClass.getConstructor(SRMUser.class,
                        requestClass,
                        AbstractStorageElement.class,
                        SRM.class,
                        String.class);
                handler = handlerConstructor.newInstance(user,
                        request,
                        storage,
                        srm,
                        remoteHost);

                if (handler instanceof CredentialAwareHandler) {
                    CredentialAwareHandler credentialAware = (CredentialAwareHandler) handler;
                    try {
                        credentialAware.setCredential(srmAuth.getRequestCredential());
                    } catch (SRMAuthenticationException e) {
                        LOGGER.warn(e.getMessage());
                        return getFailedResponse(capitalizedRequestName, e.getStatusCode(),
                                                 "Authentication failed (server log contains additional information).");
                    }
                }

                handleGetResponseMethod = handlerClass.getMethod("getResponse");
            } catch (ClassNotFoundException e) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.info("handler discovery and dynamic loading failed", e);
                } else {
                    LOGGER.info("handler discovery and dynamic loading failed");
                }
                return getFailedResponse(capitalizedRequestName,
                        TStatusCode.SRM_NOT_SUPPORTED,
                        requestName + " is unsupported");
            }
            return handleGetResponseMethod.invoke(handler);
        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException | RuntimeException e) {
            LOGGER.error("Please report this failure to support@dcache.org", e);
            return getFailedResponse(capitalizedRequestName,
                    TStatusCode.SRM_INTERNAL_ERROR,
                    "Internal error (server log contains additional information)");
        }
    }

    private Object getFailedResponse(String requestName, TStatusCode statusCode, String errorMessage)
            throws RemoteException
    {
        char first = requestName.charAt(0);
        String capitalizedRequestName =  Character.isUpperCase(first) ? requestName :
                (Character.toUpperCase(first) + requestName.substring(1));

        try {
            Class<?> responseClass = Class.forName("org.dcache.srm.v2_2."+capitalizedRequestName+"Response");
            Constructor<?> responseConstructor = responseClass.getConstructor();
            Object response;
            try {
                response = responseConstructor.newInstance();
            } catch (InvocationTargetException e) {
                Throwables.propagateIfPossible(e, Exception.class);
                throw new RuntimeException("Unexpected exception", e);
            }
            try {
                Method setReturnStatus = responseClass
                        .getMethod("setReturnStatus", TReturnStatus.class);
                setReturnStatus.setAccessible(true);
                try {
                    setReturnStatus.invoke(response, new TReturnStatus(statusCode, errorMessage));
                } catch (InvocationTargetException e) {
                    Throwables.propagateIfPossible(e, Exception.class);
                    throw new RuntimeException("Unexpected exception", e);
                }
            } catch (Exception e) {
                LOGGER.trace("getFailedResponse invocation failed", e);
                Method setStatusCode = responseClass.getMethod("setStatusCode", TStatusCode.class);
                setStatusCode.setAccessible(true);
                setStatusCode.invoke(response, statusCode);
                Method setExplanation = responseClass.getMethod("setExplanation", String.class);
                setExplanation.setAccessible(true);
                setExplanation.invoke(response, errorMessage);
            }
            return response;
        } catch (Exception e) {
            throw new RemoteException("Failed to generate SRM reply", e);
        }
    }

    @Override
    public SrmReserveSpaceResponse srmReserveSpace(
            SrmReserveSpaceRequest srmReserveSpaceRequest)
            throws RemoteException {
        return
                (SrmReserveSpaceResponse)
                handleRequest("srmReserveSpace",srmReserveSpaceRequest);
    }

    @Override
    public SrmReleaseSpaceResponse srmReleaseSpace(
            SrmReleaseSpaceRequest srmReleaseSpaceRequest)
            throws RemoteException {
        return
                (SrmReleaseSpaceResponse)
                handleRequest("srmReleaseSpace",srmReleaseSpaceRequest);
    }

    @Override
    public SrmUpdateSpaceResponse srmUpdateSpace(
            SrmUpdateSpaceRequest srmUpdateSpaceRequest)
            throws RemoteException {
        return
                (SrmUpdateSpaceResponse)
                handleRequest("srmUpdateSpace",srmUpdateSpaceRequest);
    }


    @Override
    public SrmGetSpaceMetaDataResponse srmGetSpaceMetaData(
            SrmGetSpaceMetaDataRequest srmGetSpaceMetaDataRequest)
            throws RemoteException {
        return
                (SrmGetSpaceMetaDataResponse)
                handleRequest("srmGetSpaceMetaData",srmGetSpaceMetaDataRequest);
    }



    @Override
    public SrmSetPermissionResponse srmSetPermission(
            SrmSetPermissionRequest srmSetPermissionRequest)
            throws RemoteException {
        return
                (SrmSetPermissionResponse)
                handleRequest("srmSetPermission",srmSetPermissionRequest);
    }


    @Override
    public SrmCheckPermissionResponse srmCheckPermission(
            SrmCheckPermissionRequest srmCheckPermissionRequest)
            throws RemoteException {
        return
                (SrmCheckPermissionResponse)
                handleRequest("srmCheckPermission",srmCheckPermissionRequest);
    }

    @Override
    public SrmMkdirResponse srmMkdir( SrmMkdirRequest request) throws RemoteException {
        return
                (SrmMkdirResponse)
                handleRequest("srmMkdir",request);
    }

    @Override
    public SrmRmdirResponse srmRmdir( SrmRmdirRequest request) throws RemoteException {
        return
                (SrmRmdirResponse)
                handleRequest("srmRmdir",request);
    }

    @Override
    public SrmCopyResponse srmCopy(SrmCopyRequest request)  throws RemoteException {
        return
                (SrmCopyResponse)
                handleRequest("srmCopy",request);
    }

    @Override
    public SrmRmResponse srmRm(SrmRmRequest request)  throws RemoteException {
        return
                (SrmRmResponse)
                handleRequest("srmRm",request);
    }

    @Override
    public SrmLsResponse srmLs(SrmLsRequest srmLsRequest)
    throws RemoteException {
        return (SrmLsResponse)handleRequest("srmLs",srmLsRequest);
    }

    @Override
    public SrmMvResponse srmMv(SrmMvRequest request)
    throws RemoteException {
        return
                (SrmMvResponse)
                handleRequest("srmMv",request);
    }

    @Override
    public SrmPrepareToGetResponse srmPrepareToGet(
            SrmPrepareToGetRequest srmPrepareToGetRequest)
            throws RemoteException {
        return
                (SrmPrepareToGetResponse)
                handleRequest("srmPrepareToGet",srmPrepareToGetRequest);
    }

    @Override
    public SrmPrepareToPutResponse srmPrepareToPut(
            SrmPrepareToPutRequest srmPrepareToPutRequest)
            throws RemoteException {
        return
                (SrmPrepareToPutResponse)
                handleRequest("srmPrepareToPut",srmPrepareToPutRequest);
    }


    @Override
    public SrmReleaseFilesResponse srmReleaseFiles(
            SrmReleaseFilesRequest srmReleaseFilesRequest)
            throws RemoteException {
        return
                (SrmReleaseFilesResponse)
                handleRequest("srmReleaseFiles",srmReleaseFilesRequest);
    }

    @Override
    public SrmPutDoneResponse srmPutDone(
            SrmPutDoneRequest srmPutDoneRequest)
            throws RemoteException {
        return
                (SrmPutDoneResponse)
                handleRequest("srmPutDone",srmPutDoneRequest);
    }

    @Override
    public SrmAbortRequestResponse srmAbortRequest(
            SrmAbortRequestRequest srmAbortRequestRequest)
            throws RemoteException {
        return
                (SrmAbortRequestResponse)
                handleRequest("srmAbortRequest",srmAbortRequestRequest);
    }

    @Override
    public SrmAbortFilesResponse srmAbortFiles(
            SrmAbortFilesRequest srmAbortFilesRequest)
            throws RemoteException {
        return
                (SrmAbortFilesResponse)
                handleRequest("srmAbortFiles",srmAbortFilesRequest);
    }

    @Override
    public SrmSuspendRequestResponse srmSuspendRequest(
            SrmSuspendRequestRequest srmSuspendRequestRequest)
            throws RemoteException {
        return
                (SrmSuspendRequestResponse)
                handleRequest("srmSuspendRequest",srmSuspendRequestRequest);
    }

    @Override
    public SrmResumeRequestResponse srmResumeRequest(
            SrmResumeRequestRequest srmResumeRequestRequest)
            throws RemoteException {
        return
                (SrmResumeRequestResponse)
                handleRequest("srmResumeRequest",srmResumeRequestRequest);
    }

    @Override
    public SrmStatusOfGetRequestResponse srmStatusOfGetRequest(
            SrmStatusOfGetRequestRequest srmStatusOfGetRequestRequest)
            throws RemoteException {
        return
                (SrmStatusOfGetRequestResponse)
                handleRequest("srmStatusOfGetRequest",srmStatusOfGetRequestRequest);
    }

    @Override
    public SrmStatusOfPutRequestResponse srmStatusOfPutRequest(
            SrmStatusOfPutRequestRequest srmStatusOfPutRequestRequest)
            throws RemoteException {
        return
                (SrmStatusOfPutRequestResponse)
                handleRequest("srmStatusOfPutRequest",srmStatusOfPutRequestRequest);
    }


    @Override
    public SrmStatusOfCopyRequestResponse srmStatusOfCopyRequest(
            SrmStatusOfCopyRequestRequest request)
            throws RemoteException {
        return
                (SrmStatusOfCopyRequestResponse)
                handleRequest("srmStatusOfCopyRequest",request);
    }

    @Override
    public SrmGetRequestSummaryResponse srmGetRequestSummary(
            SrmGetRequestSummaryRequest srmGetRequestSummaryRequest)
            throws RemoteException {
        return (SrmGetRequestSummaryResponse)
        handleRequest("srmGetRequestSummary",srmGetRequestSummaryRequest);
    }

    @Override
    public SrmExtendFileLifeTimeResponse srmExtendFileLifeTime(
            SrmExtendFileLifeTimeRequest srmExtendFileLifeTimeRequest)
            throws RemoteException {
        return (SrmExtendFileLifeTimeResponse)
        handleRequest("srmExtendFileLifeTime",srmExtendFileLifeTimeRequest);
    }

    @Override
    public SrmStatusOfBringOnlineRequestResponse srmStatusOfBringOnlineRequest(SrmStatusOfBringOnlineRequestRequest srmStatusOfBringOnlineRequestRequest) throws RemoteException {
        return (SrmStatusOfBringOnlineRequestResponse)
        handleRequest("srmStatusOfBringOnlineRequest",srmStatusOfBringOnlineRequestRequest);
    }

    @Override
    public SrmBringOnlineResponse srmBringOnline(SrmBringOnlineRequest srmBringOnlineRequest) throws RemoteException {
        return (SrmBringOnlineResponse)
        handleRequest("srmBringOnline",srmBringOnlineRequest);
    }

    @Override
    public SrmExtendFileLifeTimeInSpaceResponse srmExtendFileLifeTimeInSpace(SrmExtendFileLifeTimeInSpaceRequest srmExtendFileLifeTimeInSpaceRequest) throws RemoteException {
        return (SrmExtendFileLifeTimeInSpaceResponse)
        handleRequest("srmExtendFileLifeTimeInSpace",srmExtendFileLifeTimeInSpaceRequest);
    }

    @Override
    public SrmStatusOfUpdateSpaceRequestResponse srmStatusOfUpdateSpaceRequest(SrmStatusOfUpdateSpaceRequestRequest srmStatusOfUpdateSpaceRequestRequest) throws RemoteException {
        return (SrmStatusOfUpdateSpaceRequestResponse)
        handleRequest("srmStatusOfUpdateSpaceRequest",srmStatusOfUpdateSpaceRequestRequest);
    }

    @Override
    public SrmPurgeFromSpaceResponse srmPurgeFromSpace(SrmPurgeFromSpaceRequest srmPurgeFromSpaceRequest) throws RemoteException {
        return (SrmPurgeFromSpaceResponse)
        handleRequest("srmPurgeFromSpace",srmPurgeFromSpaceRequest);
    }

    @Override
    public SrmPingResponse srmPing(SrmPingRequest srmPingRequest) throws RemoteException {
        srmServerCounters.incrementRequests(SrmPingRequest.class);

        // Ping is special as it isn't authenticated and unable to return a failure
        SrmPingResponse response = new SrmPingResponse();
        response.setVersionInfo("v2.2");
        response.setOtherInfo(pingExtraInfo);
        return response;
    }

    @Override
    public SrmGetPermissionResponse srmGetPermission(SrmGetPermissionRequest srmGetPermissionRequest) throws RemoteException {
        return (SrmGetPermissionResponse)
        handleRequest("srmGetPermission",srmGetPermissionRequest);
    }

    @Override
    public SrmStatusOfReserveSpaceRequestResponse srmStatusOfReserveSpaceRequest(SrmStatusOfReserveSpaceRequestRequest srmStatusOfReserveSpaceRequestRequest) throws RemoteException {
        return (SrmStatusOfReserveSpaceRequestResponse)
        handleRequest("srmStatusOfReserveSpaceRequest",srmStatusOfReserveSpaceRequestRequest);
    }

    @Override
    public SrmChangeSpaceForFilesResponse srmChangeSpaceForFiles(SrmChangeSpaceForFilesRequest srmChangeSpaceForFilesRequest) throws RemoteException {
        return (SrmChangeSpaceForFilesResponse)
        handleRequest("srmChangeSpaceForFiles",srmChangeSpaceForFilesRequest);
    }

    @Override
    public SrmGetTransferProtocolsResponse srmGetTransferProtocols(SrmGetTransferProtocolsRequest srmGetTransferProtocolsRequest) throws RemoteException {
        return (SrmGetTransferProtocolsResponse)
        handleRequest("srmGetTransferProtocols",srmGetTransferProtocolsRequest);
    }

    @Override
    public SrmGetRequestTokensResponse srmGetRequestTokens(SrmGetRequestTokensRequest srmGetRequestTokensRequest) throws RemoteException {
        return (SrmGetRequestTokensResponse)
        handleRequest("srmGetRequestTokens",srmGetRequestTokensRequest);
    }

    @Override
    public SrmGetSpaceTokensResponse srmGetSpaceTokens(SrmGetSpaceTokensRequest srmGetSpaceTokensRequest) throws RemoteException {
        return (SrmGetSpaceTokensResponse)
        handleRequest("srmGetSpaceTokens",srmGetSpaceTokensRequest);
    }

    @Override
    public SrmStatusOfChangeSpaceForFilesRequestResponse srmStatusOfChangeSpaceForFilesRequest(SrmStatusOfChangeSpaceForFilesRequestRequest srmStatusOfChangeSpaceForFilesRequestRequest) throws RemoteException {
        return (SrmStatusOfChangeSpaceForFilesRequestResponse)
        handleRequest("srmStatusOfChangeSpaceForFilesRequest",srmStatusOfChangeSpaceForFilesRequestRequest);
    }

    @Override
    public SrmStatusOfLsRequestResponse srmStatusOfLsRequest(SrmStatusOfLsRequestRequest srmStatusOfLsRequestRequest) throws RemoteException {
        return (SrmStatusOfLsRequestResponse)
        handleRequest("srmStatusOfLsRequest",srmStatusOfLsRequestRequest);
    }

    private interface RequestLogger
    {
        void request(String requestName, Object request);
        void response(String requestName, Object request, Object response, SRMUser user, long time);
    }

    public class AccessLogger implements RequestLogger
    {
        private final Logger ACCESS_LOGGER = LoggerFactory.getLogger("org.dcache.access.srm");

        @Override
        public void request(String requestName, Object request)
        {
        }

        @Override
        public void response(String requestName, Object request, Object response, SRMUser user, long time)
        {
            if (ACCESS_LOGGER.isErrorEnabled()) {
                TReturnStatus status = getReturnStatus(response);
                boolean isFailure = status != null && FAILURES.contains(status.getStatusCode());
                if (!isFailure && !ACCESS_LOGGER.isInfoEnabled()) {
                    return;
                }

                NetLoggerBuilder.Level level = isFailure ? NetLoggerBuilder.Level.ERROR : NetLoggerBuilder.Level.INFO;
                NetLoggerBuilder log = new NetLoggerBuilder(level, "org.dcache.srm.request").omitNullValues();
                log.add("session", JDC.getSession());
                log.add("socket.remote", Axis.getRemoteSocketAddress());
                log.add("request.method", requestName);
                log.add("user.dn", Axis.getDN().orElse("-"));
                if (user != null) {
                    log.add("user.mapped", user.getDescriptiveName());
                }
                String requestToken = getRequestToken(request, response);
                if (requestToken != null) {
                    log.add("request.token", requestToken);
                } else {
                    log.add("request.surl", getSurl(request));
                }
                if (status != null) {
                    log.add("status.code", status.getStatusCode());
                    log.add("status.explanation", status.getExplanation());
                }
                log.add("client-info", Axis.getRequestHeader("ClientInfo"));
                log.add("user-agent", Axis.getUserAgent());
                log.toLogger(ACCESS_LOGGER);
            }
        }
    }

    public class CounterLogger implements RequestLogger
    {
        @Override
        public void request(String requestName, Object request)
        {
            srmServerCounters.incrementRequests(request.getClass());
        }

        @Override
        public void response(String requestName, Object request, Object response, SRMUser user, long time)
        {
            TReturnStatus status = getReturnStatus(response);
            if (status != null && FAILURES.contains(status.getStatusCode())) {
                srmServerCounters.incrementFailed(request.getClass());
            }
        }
    }

    private class RequestExecutionTimeGaugeLogger implements RequestLogger
    {
        @Override
        public void request(String requestName, Object request)
        {
        }

        @Override
        public void response(String requestName, Object request, Object response, SRMUser user, long time)
        {
            srmServerGauges.update(request.getClass(), time);
        }
    }

    private static String getSurl(Object request)
    {
        try {
            Method getReturnStatus = request.getClass().getDeclaredMethod("getSURL");
            Class<?> returnType = getReturnStatus.getReturnType();
            if (org.apache.axis.types.URI.class.isAssignableFrom(returnType)) {
                Object uri = getReturnStatus.invoke(request);
                if (uri != null) {
                    return uri.toString();
                }
            }
        } catch (NoSuchMethodException e) {
            // Unfortunately, Java standard API provides no nice way of
            // discovering if a method exists by reflection.  This is perhaps
            // the least ugly.
        } catch (InvocationTargetException | IllegalAccessException e) {
            LOGGER.debug("Failed to extract SURL: {}", e.toString());
        }
        return null;
    }

    private static String getRequestToken(Object request, Object response)
    {
        String requestToken = getRequestToken(response);
        if (requestToken != null) {
            return requestToken;
        }
        requestToken = getRequestToken(request);
        if (requestToken != null) {
            return requestToken;
        }
        return null;
    }

    private static String getRequestToken(Object response)
    {
        try {
            Method getReturnStatus = response.getClass().getDeclaredMethod("getRequestToken");
            Class<?> returnType = getReturnStatus.getReturnType();
            if (String.class.isAssignableFrom(returnType)) {
                return (String) getReturnStatus.invoke(response);
            }
        } catch (NoSuchMethodException e) {
            // Unfortunately, Java standard API provides no nice way of
            // discovering if a method exists by reflection.  This is perhaps
            // the least ugly.
        } catch (InvocationTargetException | IllegalAccessException e) {
            LOGGER.debug("Failed to extract request token: {}", e.toString());
        }
        return null;
    }

    private static TReturnStatus getReturnStatus(Object response)
    {
        try {
            Method getReturnStatus = response.getClass().getDeclaredMethod("getReturnStatus");
            Class<?> returnType = getReturnStatus.getReturnType();
            if (TReturnStatus.class.isAssignableFrom(returnType)) {
                return (TReturnStatus) getReturnStatus.invoke(response);
            }
        } catch (NoSuchMethodException e) {
            // Unfortunately, Java standard API provides no nice way of
            // discovering if a method exists by reflection.  This is perhaps
            // the least ugly.
        } catch (InvocationTargetException | IllegalAccessException e) {
            LOGGER.debug("Failed to extract status code: {}", e.toString());
        }
        return null;
    }
}
