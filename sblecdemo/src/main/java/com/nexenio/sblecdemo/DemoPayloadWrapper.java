package com.nexenio.sblecdemo;

import com.nexenio.sblec.payload.PayloadWrapper;
import com.nexenio.sblec.receiver.ReceiverPayload;
import com.nexenio.sblec.sender.PayloadPriorities;
import com.nexenio.sblec.sender.SenderPayload;

import java.nio.ByteBuffer;

import androidx.annotation.NonNull;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * A {@link PayloadWrapper} for a payload containing an icon, a color and a timestamp.
 *
 * You could also directly work with the {@link ReceiverPayload} and {@link SenderPayload} classes.
 * This wrapper is just here for convenience, taking care of the data encoding and decoding.
 */
public class DemoPayloadWrapper extends PayloadWrapper {

    /**
     * The ID of this payload. It helps to identify the payload on the receiving device. IDs in
     * range [0, 9] are reserved for the SBLEC protocol.
     */
    public static final int ID = 11;

    /**
     * The expected length of a buffer containing the bytes for this payload.
     *
     * 4 bytes {@link #iconIndex}, 4 bytes {@link #colorIndex} and 8 bytes {@link #timestamp}.
     */
    private static final int BUFFER_LENGTH = 16;

    /**
     * The icon index refers to different icons that a {@link DemoView} can show.
     */
    private int iconIndex;

    /**
     * The color index refers to different colors that a {@link DemoView} can show.
     */
    private int colorIndex;

    /**
     * The timestamp indicates when the payload has been created. If multiple payloads are received,
     * only the one with the highest timestamp will be visualized in a {@link DemoView}.
     */
    private long timestamp;

    /**
     * A constructor that can be used when receiving a {@link ReceiverPayload} with the {@link
     * ReceiverPayload#getId() ID} matching {@link #ID}.
     */
    public DemoPayloadWrapper(@NonNull ReceiverPayload receiverPayload) {
        super(receiverPayload);
    }

    /**
     * A constructor that can be used to create a duplicate of an existing {@link
     * DemoPayloadWrapper}. The timestamp will be set to now.
     */
    public DemoPayloadWrapper(@NonNull DemoPayloadWrapper demoPayloadWrapper) {
        this.iconIndex = demoPayloadWrapper.getIconIndex();
        this.colorIndex = demoPayloadWrapper.getColorIndex();
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * A constructor that can be used to create a new instance with the specified icon and color.
     */
    public DemoPayloadWrapper(int iconIndex, int colorIndex) {
        this.iconIndex = iconIndex;
        this.colorIndex = colorIndex;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Will be used when processing a received {@link ReceiverPayload}. All required values should
     * be parsed from the specified stream here.
     */
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

    /**
     * This will be used when converting the wrapper into an actual {@link SenderPayload} (by
     * calling {@link #toSenderPayload()}). All required values should be encoded into a new {@link
     * ByteBuffer} here.
     */
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

    /**
     * The priority may be any value between {@link PayloadPriorities#MINIMUM} and {@link
     * PayloadPriorities#MAXIMUM}. If not specified, the priority will be set to {@link
     * PayloadPriorities#MEDIUM}.
     */
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
