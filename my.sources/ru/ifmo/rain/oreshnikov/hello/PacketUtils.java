package ru.ifmo.rain.oreshnikov.hello;

import java.net.DatagramPacket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * @author doreshnikov
 */

public class PacketUtils {

    public static String encodeMessage(String prefix, int threadId, int requestId) {
        return prefix + threadId + "_" + requestId;
    }

    public static String decodeMessage(final DatagramPacket packet) {
        return new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);
    }

    public static DatagramPacket makeMessagePacket(final SocketAddress address, String message) {
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        return new DatagramPacket(messageBytes, messageBytes.length, address);
    }

    public static DatagramPacket newEmptyPacket(int receiveBufferSize) {
        return new DatagramPacket(new byte[receiveBufferSize], receiveBufferSize);
    }

    public static boolean checkValidResponse(String requestMessage, String responseMessage) {
        return responseMessage.endsWith(requestMessage);
    }

}
