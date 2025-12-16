import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import codec.SimpleTextCodec;
import messages.Message;
import messages.MsgHeader;
import messages.MsgType;
import messages.request.ChatReqMessage;
import messages.request.LoginMessage;
import messages.request.LogoutMessage;
import messages.request.QuitReqMessage;
import messages.request.RegisterMessage;
import messages.request.WhoOnlineMessage;
import messages.response.ChatReqOkMessage;
import messages.response.ErrorMessage;

public class ThreadedClientObject {
    private Socket clientSocket;
    private States state = null;
    private BufferedReader inFromUser;
    private DataOutputStream outToServer;
    private DataInputStream inFromServer;
    private String userChoice, email, name, password, userAction;
    private Message response = null, request = null;
    private SimpleTextCodec codec;
    private byte[] sendData = null;
    private byte[] receiveData;
    private int requested_udpPort;

    public ThreadedClientObject() {
        codec = new SimpleTextCodec();
        receiveData = new byte[1024];
    }

    public static void main(String[] args) throws IOException {
        ThreadedClientObject client = new ThreadedClientObject();
        client.run();
    }

    public void run() throws IOException {
        try {
            clientSocket = new Socket("localhost", 6324);
            System.out.println("Client connected end with \"END\"");
            //state = States.CONNECTEDTOSERVER;
            state = States.ONLINE;
            inFromUser = new BufferedReader(new InputStreamReader(System.in));
            outToServer = new DataOutputStream(clientSocket.getOutputStream()); // // of byte-arrays
            inFromServer = new DataInputStream(clientSocket.getInputStream());

        } catch (Exception e) {
            e.getMessage();
            e.printStackTrace();
            System.err.println("Initialization failed");
            return;
        }

        while (state != null) {
            switch (state) {
                case States.CONNECTEDTOSERVER:
                    outer: do {

                        do {
                            System.out.println("What do you want to do? register | login | quit");
                            userAction = inFromUser.readLine();

                        } while (!(userAction.toLowerCase().equals("register")
                                || userAction.toLowerCase().equals("login")
                                || userAction.toLowerCase().equals("quit")));

                        switch (userAction) {
                            case "quit":
                                request = new QuitReqMessage(
                                        new MsgHeader(MsgType.QUITREQ, 1, 1, System.currentTimeMillis()));
                                break;

                            case "register":
                                // register (Server has to add data to registeredUserList)
                                do {
                                    System.out.println("Please enter your email");
                                    email = inFromUser.readLine();
                                    System.out.println("Please choose your name");
                                    name = inFromUser.readLine();
                                    System.out.println("Please choose your password");
                                    password = inFromUser.readLine();

                                    request = new RegisterMessage(
                                            new MsgHeader(MsgType.REGISTER, 1, 1, System.currentTimeMillis()),
                                            email, name, password);
                                    sendData(request, codec, outToServer);
                                    response = receiveData(codec, inFromServer);

                                    if (response.header().type() == MsgType.ERROR) {
                                        ErrorMessage error;
                                        error = (ErrorMessage) response;
                                        System.out.println(
                                                "Registration failed.Error: " + error.reason()
                                                        + "\n Please try again or type");
                                        // we should define errorcodes to simplify this
                                        continue outer;
                                    }

                                } while (response.header().type() == MsgType.ERROR);
                                System.out.println("Registration successful");

                                continue outer;

                            case "login":
                                do {

                                    System.out.println("Please enter your login-email");
                                    email = inFromUser.readLine();
                                    System.out.println("Please enter your password");
                                    password = inFromUser.readLine();

                                    request = new LoginMessage(
                                            new MsgHeader(MsgType.LOGIN, 1, 1, System.currentTimeMillis()),
                                            email,
                                            password);
                                    sendData(request, codec, outToServer);
                                    receiveData(codec, inFromServer);

                                    if (response.header().type() == MsgType.ERROR) {
                                        ErrorMessage error;
                                        error = (ErrorMessage) response;
                                        System.out.println(
                                                "Login failed. Error: " + error.reason() + "\n Please try again");
                                        continue outer;
                                    }

                                } while (response.header().type() == MsgType.ERROR);
                                System.out.println("Login successful");
                                state = States.ONLINE;

                                break;
                            default:
                                break;
                        }
                    } while (!(userAction.toLowerCase().equals("quit")));

                    sendData(request, codec, outToServer);
                    System.out.println("Bye-Message sent:\n" + sendData);
                    clientSocket.close();
                    state = null;
                    break;
                case States.ONLINE:

                    request = new WhoOnlineMessage(
                            new MsgHeader(MsgType.WHO_ONLINE, 1, 1, System.currentTimeMillis()));
                    sendData = codec.encode(request);

                    sendData(request, codec, outToServer);
                    receiveData(codec, inFromServer);

                    if (response.header().type() == MsgType.ERROR) {
                        ErrorMessage error;
                        error = (ErrorMessage) response;
                        System.out
                                .println("No online users. Error: " + error.reason() + ",\r\n You will be logged out");
                        state = States.CONNECTEDTOSERVER;
                        break;
                    }

                    System.out.println("Who do want to chat with? Please type name or 'logut'");

                    userChoice = inFromUser.readLine();

                    switch (userChoice) {
                        case "logout":

                            request = new LogoutMessage(
                                    new MsgHeader(MsgType.LOGOUT, 1, 1, System.currentTimeMillis()));

                            sendData(request, codec, outToServer);
                            System.out.println("Bye-Message sent:\n" + sendData);
                            clientSocket.close();
                            state = null;
                            break;
                        default: // default because there will be different names
                            request = new ChatReqMessage(
                                    new MsgHeader(MsgType.WHO_ONLINE, 1, 1, System.currentTimeMillis()), userChoice);
                            sendData(request, codec, outToServer);
                            receiveData(codec, inFromServer);

                            if (response.header().type() == MsgType.ERROR) {
                                ErrorMessage error;
                                error = (ErrorMessage) response;
                                System.out.println("Chat could not be established. Error: " + error.reason()
                                        + ",\r\n You will be logged out");
                                state = States.CONNECTEDTOSERVER;
                                break;
                            }

                            ChatReqOkMessage chatOk = (ChatReqOkMessage) response;
                            requested_udpPort = chatOk.getRequested_user_port();

                            state = States.CONNECTEDTOCLIENT;
                            System.out.print("yay we can connect now UDP here");
                            break;
                    }
            }
        }
    }

    private void sendData(Message msg, SimpleTextCodec codec, DataOutputStream outToServer) {
        byte[] sendData = null;
        try {
            sendData = codec.encode(msg);
            outToServer.writeInt(sendData.length);
            outToServer.write(sendData);
            outToServer.flush();
        } catch (IOException e) {

            e.getMessage();
            e.printStackTrace();
            System.err.println("Could not send Data to Sever");
        }
    }

    private Message receiveData(SimpleTextCodec codec, DataInputStream inFromServer) {
        Message response = null;
        byte[] receiveData = new byte[1024];
        try {
            int length = inFromServer.readInt();
            receiveData = inFromServer.readNBytes(length);
            response = codec.decode(receiveData);

        } catch (IOException e) {
            e.getMessage();
            e.printStackTrace();
            System.err.println("Could not receive Data from Sever");
            return null;
        }

        return response;
    }
}
