package ru.ifmo.rain.oreshnikov.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author doreshnikov
 * @date 30-Apr-19
 */

public class HelloUDPServer implements HelloServer {

    private static final int TERMINATION_AWAIT_SECONDS = 1;
    private static final boolean VERBOSE = false;

    private DatagramSocket socket;
    private ExecutorService singleExecutor;
    private ExecutorService workers;

    public HelloUDPServer() {
        socket = null;
        singleExecutor = null;
        workers = null;
    }

    @Override
    public void start(int port, int threads) {
        try {
            socket = new DatagramSocket(port);
            workers = Executors.newFixedThreadPool(threads);
            singleExecutor = Executors.newSingleThreadExecutor();
            singleExecutor.submit(this::receiveAndRespond);
        } catch (SocketException e) {
            System.err.println("Unable to establish connection via socket: " + e.getMessage());
        }
    }

    private void receiveAndRespond() {
        while (!socket.isClosed() && !Thread.interrupted()) {
            try {
                final DatagramPacket request = PacketUtils.newEmptyPacket(socket.getReceiveBufferSize());
                socket.receive(request);
                final String requestMessage = PacketUtils.decodeMessage(request);
                log(String.format("Received '%s'", requestMessage));
                workers.submit(() -> {
                    String responseMessage = "Hello, " + requestMessage;
                    log(String.format("Sending '%s'", responseMessage));
                    final DatagramPacket response = PacketUtils.makeMessagePacket(
                            request.getSocketAddress(), responseMessage);
                    try {
                        socket.send(response);
                    } catch (IOException e) {
                        if (!socket.isClosed()) {
                            System.err.println("Error occured while trying to send a response: " + e.getMessage());
                        }
                    }
                });
            } catch (IOException e) {
                if (!socket.isClosed()) {
                    System.err.println("Error occured while trying to receive a request: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public void close() {
        socket.close();
        singleExecutor.shutdown();
        workers.shutdown();
        try {
            singleExecutor.awaitTermination(TERMINATION_AWAIT_SECONDS, TimeUnit.SECONDS);
            workers.awaitTermination(TERMINATION_AWAIT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.err.println("Could not terminate executor pools: " + e.getMessage());
        }
    }

    private void log(String message) {
        if (VERBOSE) {
            System.out.println(message);
        }
    }

}
