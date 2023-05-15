package peertopeer;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PeerToPeerClient extends PeerToPeerBase {
    private final ManagedChannel channel;
    private final PeerToPeerGrpc.PeerToPeerBlockingStub blockingStub;
    private final PeerToPeerGrpc.PeerToPeerStub asyncStub;
    private Logger logger = Logger.getLogger(PeerToPeerClient.class.getName());
    ;

    public PeerToPeerClient(String username, String host, int port) {
        this(username, ManagedChannelBuilder.forAddress(host, port).usePlaintext());
    }

    /**
     * Construct client for accessing RouteGuide server using the existing channel.
     */
    private PeerToPeerClient(String username, ManagedChannelBuilder<?> channelBuilder) {
        super(username);
        channel = channelBuilder.build();
        blockingStub = PeerToPeerGrpc.newBlockingStub(channel);
        asyncStub = PeerToPeerGrpc.newStub(channel);
    }


    public void startChat() throws Exception {
        final CountDownLatch finishLatch = new CountDownLatch(1);
        blockingStub.connect(Service.User.newBuilder().setName(username).build());
        StreamObserver<Service.Message> requestObserver =
                asyncStub.sendMessage(new StreamObserver<>() {
                    @Override
                    public void onNext(Service.Message message) {
                        onResponse(message.getMessage(), message.getSender(), message.getTime());
                    }

                    @Override
                    public void onError(Throwable t) {
                        Status status = Status.fromThrowable(t);
                        logger.log(Level.WARNING, "RouteChat Failed: {0}", status);
                        finishLatch.countDown();
                    }

                    @Override
                    public void onCompleted() {
                        logger.info("Finished RouteChat");
                        finishLatch.countDown();
                    }
                });

        try {
            readLoop(requestObserver);
        } catch (RuntimeException e) {
            // Cancel RPC
            requestObserver.onError(e);
            throw e;
        }
        // Mark the end of requests
        requestObserver.onCompleted();

        // Receiving happens asynchronously
        finishLatch.await(3, TimeUnit.SECONDS);
    }






}
