package org.dcache.chimera.nfsv41.mover;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.dcache.nfs.ChimeraNFSException;
import org.dcache.nfs.nfsstat;
import org.dcache.nfs.status.BadStateidException;
import org.dcache.nfs.status.NfsIoException;
import org.dcache.nfs.v4.AbstractNFSv4Operation;
import org.dcache.nfs.v4.CompoundContext;
import org.dcache.nfs.v4.xdr.READ4res;
import org.dcache.nfs.v4.xdr.READ4resok;
import org.dcache.nfs.v4.xdr.nfs_argop4;
import org.dcache.nfs.v4.xdr.nfs_opnum4;
import org.dcache.nfs.v4.xdr.nfs_resop4;
import org.dcache.pool.repository.RepositoryChannel;

public class EDSOperationREAD extends AbstractNFSv4Operation {

    private static final Logger _log = LoggerFactory.getLogger(EDSOperationREAD.class.getName());

     private final NFSv4MoverHandler _moverHandler;

    public EDSOperationREAD(nfs_argop4 args, NFSv4MoverHandler moverHandler) {
        super(args, nfs_opnum4.OP_READ);
        _moverHandler = moverHandler;
    }

    @Override
    public void process(CompoundContext context, nfs_resop4 result) {
        final READ4res res = result.opread;

        try {

            long offset = _args.opread.offset.value;
            int count = _args.opread.count.value;

            NfsMover mover = _moverHandler.getOrCreateMover(context.getRemoteSocketAddress(),
                    _args.opread.stateid, context.currentInode().toNfsHandle());
            if(mover == null) {
                /*
                 * return IO error instead of BadStateidException to avoid state recovery.
                 * The client will fall back to IO through MDS.
                 */
                throw new NfsIoException("No mover associated with given stateid: " + _args.opread.stateid);
            }
            mover.attachSession(context.getSession());

            ByteBuffer bb = ByteBuffer.allocate(count);
            RepositoryChannel fc = mover.getMoverChannel();

            bb.rewind();
            int bytesRead = fc.read(bb, offset);

            res.status = nfsstat.NFS_OK;
            res.resok4 = new READ4resok();
            res.resok4.data = bb;

            if( bytesRead == -1 || offset + bytesRead == fc.size() ) {
                res.resok4.eof = true;
            }

            _log.debug("MOVER: {}@{} read, {} requested.", bytesRead, offset, _args.opread.count.value);

        }catch(ChimeraNFSException he) {
            res.status = he.getStatus();
            _log.debug(he.getMessage());
        }catch(IOException ioe) {
            _log.error("DSREAD: ", ioe);
            res.status = nfsstat.NFSERR_IO;
        }catch(Exception e) {
            _log.error("DSREAD: ", e);
            res.status = nfsstat.NFSERR_SERVERFAULT;
        }
    }
}
