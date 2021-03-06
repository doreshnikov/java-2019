package ru.ifmo.rain.oreshnikov.hello;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
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

    @Deprecated
    public static SocketAddress getUnresolvedSocketAddress(final DatagramPacket packet) {
        return InetSocketAddress.createUnresolved(String.valueOf(packet.getAddress()), packet.getPort());
    }

    public static void fillMessage(final DatagramPacket packet, String message) {
        packet.setData(message.getBytes(StandardCharsets.UTF_8));
    }

    public static DatagramPacket newEmptyPacket(int receiveBufferSize) {
        return new DatagramPacket(new byte[receiveBufferSize], receiveBufferSize);
    }

    public static DatagramPacket newEmptyPacket(final SocketAddress address, int receiveBufferSize) {
        return new DatagramPacket(new byte[receiveBufferSize], receiveBufferSize, address);
    }

    public static void resetAndResize(final DatagramPacket packet, int receiveBufferSize) {
        packet.setData(new byte[receiveBufferSize]);
    }

    public static boolean checkValidResponse(String requestMessage, String responseMessage) {
        return responseMessage.contains(requestMessage);
    }

}
