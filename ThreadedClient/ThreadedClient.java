import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

import codec.SimpleTextCodec;
import messages.Message;
import messages.MsgHeader;
import messages.MsgType;
import messages.UDP_messages.ChatMessage;
import messages.request.ChatReqMessage;
import messages.request.LoginMessage;
import messages.request.LogoutMessage;
import messages.request.QuitReqMessage;
import messages.request.RegisterMessage;
import messages.request.WhoOnlineMessage;
import messages.response.ChatReqOkMessage;
import messages.response.ErrorMessage;

public class ThreadedClient {
    public static void main(String[] args) throws Exception {

        // you can get around the local variables can't be dynamic in threads if you just turn them into arrays
        Socket clientSocket;
        DatagramSocket[] clientUdpSocket = {null};
        States state = null;
        BufferedReader inFromUser;
        DataOutputStream outToServer;
        DataInputStream inFromServer;
        SimpleTextCodec codec = new SimpleTextCodec();
        String userChoice, email, name, password, userAction;
        final int[] requested_udpPort = {0};
        Message response = null, request = null;
        byte[] sendData = null;
        byte[] receiveData = new byte[1024];
        Thread UDP_thread;
        final InetAddress[] clientIPAddress = {null};


        // connect (tbh I don't know where to start the thread probably here but.. )

        try {
            clientSocket = new Socket("localhost", 12345);
            System.out.println("Client connected end with \"END\"");
            state = States.CONNECTEDTOSERVER;

            inFromUser = new BufferedReader(new InputStreamReader(System.in));
            outToServer = new DataOutputStream(clientSocket.getOutputStream());                                                                  // // of byte-arrays
            inFromServer = new DataInputStream(clientSocket.getInputStream());

        } catch (Exception e) {
            e.getMessage();
            e.printStackTrace();
            System.err.println("Initialization failed");
            return;
        }

        while (state!=null) {

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
                                request = new QuitReqMessage(new MsgHeader(MsgType.QUITREQ, 1, 1, System.currentTimeMillis()));
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
                                                "Registration failed.Error: " + error.reason() + "\n Please try again or type");
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
                            requested_udpPort[0] = chatOk.getRequested_user_port();

                            // TODO establish UDP Connection here
                            // this doesn't make sense sense we aren't connecting to UDP we are just sending messages so nothing needs to be here
                            state = States.CONNECTEDTOCLIENT;
                            break;
                    }

                case States.CONNECTEDTOCLIENT:

                    // TODO implement UDP-Communication here
                    try {
                        clientUdpSocket[0] = new DatagramSocket();
                        System.out.println("ClientSocket established on port: " + clientUdpSocket[0].getLocalPort());
                        clientUdpSocket[0].setSoTimeout(5000); // 5000 ms = 5 Sekunden
                        System.out.println("SocketTimeout set to 5 seconds");
                        // what is the name of client
                        clientIPAddress[0] = InetAddress.getByName("localhost");

                        System.out.println("ServerIP found: " + clientIPAddress[0]);
                        // running = true;
                    } catch (Exception e) {
                        e.getMessage();
                        e.printStackTrace();
                        System.err.println("Initialization failed");
                        clientUdpSocket[0].close();
                        return;
                    }

                    do {
                        Thread message_send_thread = new Thread(() -> {
                            System.out.print("Message: ");
                            String message = null;
                            try {
                                message = inFromUser.readLine();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            ChatMessage chat_message = new ChatMessage(new MsgHeader(MsgType.CHAT_MSG,1, 1, System.currentTimeMillis()), message);
                            
                            byte[] msgData = codec.encode(chat_message);

                            DatagramPacket sendPacket = new DatagramPacket(msgData, msgData.length, clientIPAddress[0], requested_udpPort[0]);
                            
                            try {
                                clientUdpSocket[0].send(sendPacket);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                        message_send_thread.start();



                    } while (!inFromUser.readLine().equals("quit"));



                    // UDP_thread = new Thread(() -> {
                        

                    // });
                    // UDP_thread.start();
                    break;

                default:
                    break;

            }
        }

        System.out.println("Connection to Server closed");

    }

    private static void sendData(Message msg, SimpleTextCodec codec, DataOutputStream outToServer) {
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

    private static Message receiveData(SimpleTextCodec codec, DataInputStream inFromServer) {
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