package peertopeer;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length > 2) {
            PeerToPeerClient client = new PeerToPeerClient(args[0], args[1],  Integer.parseInt(args[2]));
            client.startChat();
        } else {
            PeerToPeerServer server = new PeerToPeerServer(args[0], Integer.parseInt(args[1]));
            server.start();
            server.blockUntilShutdown();
        }
    }
}
