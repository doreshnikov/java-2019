package ru.ifmo.rain.oreshnikov.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author doreshnikov
 * @date 30-Apr-19
 */

public class HelloUDPServer implements HelloServer {

    private static final String USAGE = "Usage: HelloUDPServer port threads";
    private static final int TERMINATION_AWAIT_SECONDS = 1;
    private static final boolean VERBOSE = false;

    private DatagramSocket socket;
    private ExecutorService mainExecutor;
    private ExecutorService workers;

    private int receiveBufferSize = 0;

    public static void main(String[] args) {
        if (args == null || args.length != 2) {
            System.out.println(USAGE);
        } else {
            if (Arrays.stream(args).anyMatch(Objects::isNull)) {
                System.out.println("Non-null arguments expected");
                return;
            }
            try {
                int port = Integer.parseInt(args[0]);
                int threads = Integer.parseInt(args[1]);

                try (HelloServer server = new HelloUDPServer()) {
                    server.start(port, threads);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                    reader.readLine();
                } catch (IOException e) {
                    System.err.println("Could not listen to input command");
                }
            } catch (NumberFormatException e) {
                System.out.println("Arguments 'port' ans 'threads' are expected to be integers: " +
                        e.getMessage());
            }
        }
    }

    @Override
    public void start(int port, int threads) {
        try {
            socket = new DatagramSocket(port);
            receiveBufferSize = socket.getReceiveBufferSize();

            workers = Executors.newFixedThreadPool(threads);
            mainExecutor = Executors.newSingleThreadExecutor();
            mainExecutor.submit(this::receiveAndRespond);
        } catch (SocketException e) {
            System.err.println("Unable to establish connection via socket: " + e.getMessage());
        }
    }

    private void receiveAndRespond() {
        try {
            while (!socket.isClosed() && !Thread.interrupted()) {
                final DatagramPacket request = PacketUtils.newEmptyPacket(receiveBufferSize);
                socket.receive(request);

                workers.submit(() -> {
                    final String requestMessage = PacketUtils.decodeMessage(request);
                    log(String.format("Received '%s'", requestMessage));

                    String responseMessage = "Hello, " + requestMessage;
                    PacketUtils.fillMessage(request, responseMessage);
                    log(String.format("Sending '%s'", responseMessage));
                    try {
                        socket.send(request);
                    } catch (IOException e) {
                        if (!socket.isClosed()) {
                            System.err.println("Error occured while trying to send a response: " + e.getMessage());
                        }
                    }
                });
            }
        } catch (IOException e) {
            if (!socket.isClosed()) {
                System.err.println("Error occured while trying to receive a request: " + e.getMessage());
            }
        }
    }

    @Override
    public void close() {
        socket.close();
        mainExecutor.shutdown();
        workers.shutdown();
        try {
            mainExecutor.awaitTermination(TERMINATION_AWAIT_SECONDS, TimeUnit.SECONDS);
            workers.awaitTermination(TERMINATION_AWAIT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.err.println("Could not terminate executor pools: " + e.getMessage());
        }
    }

    private static void log(String message) {
        if (VERBOSE) {
//            new PrintStream(System.out, true, StandardCharsets.UTF_8).println(message);
            System.out.println(message);
        }
    }

}
