package co.rsk.net.sync;

import com.google.common.annotations.VisibleForTesting;
import org.ethereum.core.BlockHeader;
import org.ethereum.core.BlockIdentifier;
import org.ethereum.util.ByteUtil;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

public class DownloadingHeadersSyncState extends BaseSyncState {

    private Queue<BlockHeader> pendingHeaders;
    private final SkeletonDownloadHelper skeletonDownloadHelper;

    public DownloadingHeadersSyncState(SyncConfiguration syncConfiguration, SyncEventsHandler syncEventsHandler, SyncInformation syncInformation, List<BlockIdentifier> skeleton, long connectionPoint) {
        super(syncInformation, syncEventsHandler, syncConfiguration);

        this.pendingHeaders = new ArrayDeque<>();
        this.skeletonDownloadHelper = new SkeletonDownloadHelper(syncConfiguration);
        skeletonDownloadHelper.setSkeleton(skeleton, connectionPoint);
    }

    @Override
    public boolean isSyncing() {
        return true;
    }

    @Override
    public void newBlockHeaders(List<BlockHeader> chunk) {
        Optional<ChunkDescriptor> currentChunk = skeletonDownloadHelper.getCurrentChunk();
        if (!currentChunk.isPresent()
                || chunk.size() != currentChunk.get().getCount()
                || !ByteUtil.fastEquals(chunk.get(0).getHash(), currentChunk.get().getHash())) {
            // TODO(mc) do peer scoring and banning
//            logger.trace("Invalid block headers response with ID {} from peer {}", message.getId(), peer.getPeerNodeID());
            syncEventsHandler.stopSyncing();
            return;
        }

        pendingHeaders.add(chunk.get(chunk.size() - 1));

        for (int k = 1; k < chunk.size(); ++k) {
            BlockHeader parentHeader = chunk.get(chunk.size() - k);
            BlockHeader header = chunk.get(chunk.size() - k - 1);

            if (!syncInformation.blockHeaderIsValid(header, parentHeader)) {
                // TODO(mc) do peer scoring and banning
//                logger.trace("Couldn't validate block header {} hash {} from peer {}", header.getNumber(), HashUtil.shortHash(header.getHash()), peer.getPeerNodeID());
                syncEventsHandler.stopSyncing();
                return;
            }

            pendingHeaders.add(header);
        }

        if (skeletonDownloadHelper.hasNextChunk()) {
            resetTimeElapsed();
            syncEventsHandler.sendBlockHeadersRequest(skeletonDownloadHelper.getNextChunk());
            return;
        }

//        logger.trace("Finished verifying headers from peer {}", peer.getPeerNodeID());
        syncEventsHandler.startDownloadingBodies(pendingHeaders);
    }

    @Override
    public void onEnter() {
        syncEventsHandler.sendBlockHeadersRequest(skeletonDownloadHelper.getNextChunk());
    }

    @VisibleForTesting
    public List<BlockIdentifier> getSkeleton() {
        return skeletonDownloadHelper.getSkeleton();
    }
}
