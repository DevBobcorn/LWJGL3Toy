package io.devbobcorn.toy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.lwjgl.*;

import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Objects;

import static org.lwjgl.BufferUtils.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Utils {

    private Utils() {
        // Utility class
    }

    /**
     * Reads the specified resource and returns the data as a String.
     *
     * @param resource   the resource to read
     *
     * @return the resource data
     *
     * @throws IOException if an IO error occurs
     */
    public static String resourceToString(String resource) throws IOException {
        String str;
        try {
            InputStream source = resource.startsWith("http")
                    ? new URL(resource).openStream()
                    : Thread.currentThread().getContextClassLoader()
                            .getResourceAsStream(resource);

            var streamReader = new InputStreamReader(Objects.requireNonNull(source), StandardCharsets.UTF_8);
            var sb = new StringBuilder();
            for (var line : new BufferedReader(streamReader).lines().toList()) {
                sb.append(line).append('\n');
            }
            str = sb.toString();
        } catch (Exception excp) {
            throw new IOException("Error reading string from [" + resource + "]: " + excp);
        }
        return str;
    }

    private static ByteBuffer resizeBuffer(ByteBuffer buffer, int newCapacity) {
        ByteBuffer newBuffer = BufferUtils.createByteBuffer(newCapacity);
        buffer.flip();
        newBuffer.put(buffer);
        return newBuffer;
    }

    /**
     * Reads the specified resource and returns the raw data as a ByteBuffer.
     *
     * @param resource   the resource to read
     * @param bufferSize the initial buffer size
     *
     * @return the resource data
     *
     * @throws IOException if an IO error occurs
     */
    public static ByteBuffer resourceToByteBuffer(String resource, int bufferSize) throws IOException {
        ByteBuffer buffer;

        Path path = resource.startsWith("http") ? null : Paths.get(resource);
        if (path != null && Files.isReadable(path)) {
            try (SeekableByteChannel fc = Files.newByteChannel(path)) {
                buffer = BufferUtils.createByteBuffer((int)fc.size() + 1);
                while (fc.read(buffer) != -1) {
                    ;
                }
            }
        } else {
            try (
                    InputStream source = resource.startsWith("http")
                            ? new URL(resource).openStream()
                            : Thread.currentThread().getContextClassLoader()
                                    .getResourceAsStream(resource);
                    ReadableByteChannel rbc = Channels.newChannel(source)
            ) {
                buffer = createByteBuffer(bufferSize);

                while (true) {
                    int bytes = rbc.read(buffer);
                    if (bytes == -1) {
                        break;
                    }
                    if (buffer.remaining() == 0) {
                        buffer = resizeBuffer(buffer, buffer.capacity() * 3 / 2); // 50%
                    }
                }
            }
        }

        buffer.flip();
        return memSlice(buffer);
    }

}