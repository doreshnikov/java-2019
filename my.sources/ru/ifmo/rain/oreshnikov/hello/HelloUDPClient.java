package ru.ifmo.rain.oreshnikov.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * @author doreshnikov
 */

public class HelloUDPClient implements HelloClient {

    private static final String USAGE = "Usage: HelloUDPClient (name|ip) port prefix threads requests";

    private static final int TIMEOUT_MINUTES_PER_REQUEST = 1;
    private static final int SOCKET_SO_TIMEOUT = 1000;
    private static final boolean VERBOSE = true;

    public static void main(String[] args) {
        if (args == null || args.length != 5) {
            System.out.println(USAGE);
        } else {
            if (Arrays.stream(args).anyMatch(Objects::isNull)) {
                System.out.println("Non-null arguments expected");
                return;
            }
            try {
                int port = Integer.parseInt(args[1]);
                int threads = Integer.parseInt(args[3]);
                int requests = Integer.parseInt(args[4]);
                new HelloUDPClient().run(args[0], port, args[2], threads, requests);
            } catch (NumberFormatException e) {
                System.err.println("Arguments 'port', 'threads' and 'requests' are expected to be integers: " +
                        e.getMessage());
            }
        }
    }

    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        try {
            final SocketAddress address = new InetSocketAddress(InetAddress.getByName(host), port);
            parallelProcessAll(address, prefix, threads, requests);
        } catch (UnknownHostException e) {
            System.err.println("Unable to reach specified host: " + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("Execution was interrupted: " + e.getMessage());
        }
    }

    private void parallelProcessAll(final SocketAddress address, String prefix, int threads, int requests)
            throws InterruptedException {
        ExecutorService workers = Executors.newFixedThreadPool(threads);
        IntStream.range(0, threads).forEach((threadId) -> processTask(address, prefix, threadId, requests));
        workers.shutdown();
        workers.awaitTermination(TIMEOUT_MINUTES_PER_REQUEST * requests * threads, TimeUnit.MINUTES);
    }

    private void processTask(final SocketAddress address, String prefix, int threadId, int requests) {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(SOCKET_SO_TIMEOUT);
            for (int requestId = 0; requestId < requests; requestId++) {
                String requestMessage = PacketUtils.encodeMessage(prefix, threadId, requestId);
                log(String.format("Sending '%s'", requestMessage));
                final DatagramPacket request = PacketUtils.makeMessagePacket(address, requestMessage);
                final DatagramPacket response = PacketUtils.newEmptyPacket(socket.getReceiveBufferSize());

                boolean received = false;
                while (!received && !socket.isClosed() && !Thread.interrupted()) {
                    try {
                        socket.send(request);
                        socket.receive(response);
                        String responseMessage = PacketUtils.decodeMessage(response);
                        log(String.format("Received '%s'", responseMessage));
                        received = PacketUtils.checkValidResponse(requestMessage, responseMessage);
                    } catch (IOException e) {
                        System.err.println("Error occured while trying to send a request or process a response: "
                                + e.getMessage());
                    }
                }
            }
        } catch (SocketException e) {
            System.err.println("Unable to establish connection via socket: " + e.getMessage());
        }
    }

    private void log(String message) {
        if (VERBOSE) {
            System.out.println(message);
        }
    }

}
