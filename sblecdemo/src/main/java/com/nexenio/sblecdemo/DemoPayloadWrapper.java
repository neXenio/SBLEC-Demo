package com.nexenio.sblecdemo;

import com.nexenio.sblec.payload.PayloadWrapper;
import com.nexenio.sblec.receiver.ReceiverPayload;
import com.nexenio.sblec.sender.PayloadPriorities;

import java.nio.ByteBuffer;

import androidx.annotation.NonNull;
import io.reactivex.Completable;
import io.reactivex.Single;

public class DemoPayloadWrapper extends PayloadWrapper {

    public static final int ID = 11;

    /**
     * The expected length of a buffer containing the bytes for this payload.
     *
     * 4 bytes {@link #iconIndex}, 4 bytes {@link #colorIndex} and 8 bytes {@link #timestamp}.
     */
    private static final int BUFFER_LENGTH = 16;

    private int iconIndex;

    private int colorIndex;

    private long timestamp;

    public DemoPayloadWrapper(@NonNull ReceiverPayload receiverPayload) {
        super(receiverPayload);
    }

    public DemoPayloadWrapper(@NonNull DemoPayloadWrapper demoPayloadWrapper) {
        this.iconIndex = demoPayloadWrapper.getIconIndex();
        this.colorIndex = demoPayloadWrapper.getColorIndex();
        this.timestamp = System.currentTimeMillis();
    }

    public DemoPayloadWrapper(int iconIndex, int colorIndex) {
        this.iconIndex = iconIndex;
        this.colorIndex = colorIndex;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public Completable readFromBuffer(@NonNull ByteBuffer byteBuffer) {
        return Completable.fromAction(() -> {
            byteBuffer.rewind();

            if (byteBuffer.remaining() != BUFFER_LENGTH) {
                throw new IllegalArgumentException("Unexpected buffer size: " + byteBuffer.remaining());
            }

            iconIndex = byteBuffer.getInt();
            colorIndex = byteBuffer.getInt();
            timestamp = byteBuffer.getLong();
        });
    }

    @Override
    public Single<ByteBuffer> writeToBuffer() {
        return Single.fromCallable(() -> {
            ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_LENGTH);

            byteBuffer.putInt(iconIndex);
            byteBuffer.putInt(colorIndex);
            byteBuffer.putLong(timestamp);

            return byteBuffer;
        });
    }

    @Override
    public int getId() {
        return ID;
    }

    @Override
    public double getPriority() {
        return PayloadPriorities.MEDIUM;
    }

    public int getIconIndex() {
        return iconIndex;
    }

    public void setIconIndex(int iconIndex) {
        this.iconIndex = iconIndex;
    }

    public int getColorIndex() {
        return colorIndex;
    }

    public void setColorIndex(int colorIndex) {
        this.colorIndex = colorIndex;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "DemoPayloadWrapper{" +
                "iconIndex=" + iconIndex +
                ", colorIndex=" + colorIndex +
                ", timestamp=" + timestamp +
                ", receiverPayload=" + receiverPayload +
                '}';
    }

}
