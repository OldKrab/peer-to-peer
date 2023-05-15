package peertopeer;

import io.grpc.ChannelLogger;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Logger;

public class PeerToPeerServer extends PeerToPeerBase {
    private final PeerToPeerService service;

    public PeerToPeerServer(String username, int port) throws IOException {
        this(username, ServerBuilder.forPort(port), port);
    }

    private PeerToPeerServer(String username, ServerBuilder<?> serverBuilder, int port) {
        super(username);
        this.port = port;
        this.service = new PeerToPeerService();
        server = serverBuilder.addService(this.service)
                .build();
    }

    public void start() throws IOException {
        server.start();
        logger.info("Server started, listening on " + port);
    }


    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }


    private class PeerToPeerService extends PeerToPeerGrpc.PeerToPeerImplBase {
        @Override
        public void connect(Service.User request, StreamObserver<Service.Empty> responseObserver) {
            System.out.println(request.getName() + " connected!");
            responseObserver.onNext(Service.Empty.newBuilder().build());
            responseObserver.onCompleted();
        }

        @Override
        public StreamObserver<Service.Message> sendMessage(StreamObserver<Service.Message> responseObserver) {
            Thread thread = new Thread(() -> PeerToPeerServer.this.readLoop(responseObserver));
            thread.start();
            return new StreamObserver<>() {
                @Override
                public void onNext(Service.Message message) {
                    onResponse(message.getMessage(), message.getSender(), message.getTime());
                }

                @Override
                public void onError(Throwable throwable) {

                }

                @Override
                public void onCompleted() {

                }
            };
        }

        private final Logger logger = Logger.getLogger(PeerToPeerService.class.getName());
    }

    private final int port;
    private final Server server;
    private final Logger logger = Logger.getLogger(PeerToPeerServer.class.getName());
}

