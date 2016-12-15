package com.winsun.fruitmix.eventbus;

import com.winsun.fruitmix.mediaModule.model.MediaShare;
import com.winsun.fruitmix.util.OperationTargetType;
import com.winsun.fruitmix.util.OperationType;

/**
 * Created by Administrator on 2016/11/9.
 */

public class EditPhotoInMediaShareRequestEvent extends RequestEvent {

    private MediaShare diffContentsInOriginalMediaShare;
    private MediaShare diffContentsInModifiedMediaShare;

    private MediaShare modifiedMediaShare;

    public EditPhotoInMediaShareRequestEvent(OperationType operationType, OperationTargetType operationTargetType, MediaShare diffContentsInOriginalMediaShare, MediaShare diffContentsInModifiedMediaShare,MediaShare modifiedMediaShare) {
        super(operationType, operationTargetType);
        this.diffContentsInOriginalMediaShare = diffContentsInOriginalMediaShare;
        this.diffContentsInModifiedMediaShare = diffContentsInModifiedMediaShare;
        this.modifiedMediaShare = modifiedMediaShare;
    }

    public MediaShare getDiffContentsInOriginalMediaShare() {
        return diffContentsInOriginalMediaShare;
    }

    public MediaShare getDiffContentsInModifiedMediaShare() {
        return diffContentsInModifiedMediaShare;
    }

    public MediaShare getModifiedMediaShare() {
        return modifiedMediaShare;
    }
}
