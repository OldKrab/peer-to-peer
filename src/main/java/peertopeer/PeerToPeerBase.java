package peertopeer;

import com.google.protobuf.Timestamp;
import io.grpc.stub.StreamObserver;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class PeerToPeerBase {

    protected final String username;

    public PeerToPeerBase(String username) {
        this.username = username;
    }

    protected void writeEnterString() {
        System.out.print("Enter message: ");
    }

    protected void onResponse(String response, String sender, String time) {
        System.out.println("\nGot message from \"" + sender + "\" at " + time + ": " + response);
        writeEnterString();
    }

    protected void readLoop(StreamObserver<Service.Message> requestObserver) {
        var scanner = new Scanner(System.in);
        while (true) {
            writeEnterString();
            String input = scanner.nextLine();

            requestObserver.onNext(Service.Message.newBuilder().setMessage(input).setSender(username)
                    .setTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))).build());
        }
    }
}
