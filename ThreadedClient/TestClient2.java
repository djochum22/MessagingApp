package ThreadedClient;


    


import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.rmi.server.ServerNotActiveException;
import java.util.Arrays;

import codec.SimpleTextCodec;
import messages.Message;
import messages.MsgHeader;
import messages.MsgType;
import messages.UDPmessages.ChatMessage;
import messages.UDPmessages.ChatMessageACK;
import messages.request.ChatReqMessage;
import messages.request.LoginMessage;
import messages.request.LogoutMessage;
import messages.request.PortKeyMessage;
import messages.request.QuitReqMessage;
import messages.request.RegisterMessage;
import messages.request.WhoOnlineMessage;
import messages.response.ChatReqOkMessage;
import messages.response.ErrorMessage;
import messages.response.SendPortMessage;

public class TestClient2 {
    private Socket clientSocket;
    private DatagramSocket clientUdpSocket;
    private States state = null;
    private BufferedReader inFromUser;
    private DataOutputStream outToServer;
    private DataInputStream inFromServer;
    private String userChoice, email, name, password, userAction;
    private Message response = null, request = null;
    private SimpleTextCodec codec;
    private byte[] sendData = null;
    private int requested_udpPort;
    private int personal_port;
    private UDPStates udpStates = null;
    private InetAddress reqAddress = null;
    private boolean udpRunning = false;
    private DatagramPacket lastPacket = null;
private boolean running = false;
    public TestClient2() {
        codec = new SimpleTextCodec();
    }

    public static void main(String[] args) throws IOException {
        ThreadedClient client = new ThreadedClient();
        client.run();
    }


 public void run() throws IOException {

        try {
            try {
                clientSocket = new Socket("localhost", 6324);
                System.out.println("Client connected end with \"END\"");
                running=true;
                inFromUser = new BufferedReader(new InputStreamReader(System.in));
                outToServer = new DataOutputStream(clientSocket.getOutputStream()); // // of byte-arrays
                inFromServer = new DataInputStream(clientSocket.getInputStream());

            } catch (Exception e) {
                e.getMessage();
                e.printStackTrace();
                System.err.println("Initialization failed");
                return;
            }

            Thread listener = new Thread(()->{
                while (running) {
                    
                response=receiveData(codec, inFromServer);
             MsgType type = response.header().type();
             switch (type) {
                case OK:
                    
                    break;
             
                default:
                    break;
             }
            }
            });
            listener.start();

            while (state != null) {
         
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
                                            new MsgHeader(MsgType.QUIT_REQ, 1, 1, System.currentTimeMillis()));
                                    sendData(request, codec, outToServer);
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
                                        response = receiveData(codec, inFromServer);

                                        if (response.header().type() == MsgType.ERROR) {
                                            ErrorMessage error;
                                            error = (ErrorMessage) response;
                                            System.out.println(
                                                    "Login failed. Error: " + error.reason() + "\n Please try again");
                                            continue outer;
                                        }

                                    } while (response.header().type() == MsgType.ERROR);
                                    System.out.println("Login successful");
                                    personal_port = establishUDPSocket();
                                    request = new PortKeyMessage(
                                            new MsgHeader(MsgType.PORT_KEY, 1, 1, System.currentTimeMillis()), "",
                                            clientUdpSocket.getPort());
                                    UDPConnectionListener();

                                    state = States.ONLINE;

                                    break s;
                                default:
                                    break;
                            }
                        } while (!(userAction.toLowerCase().equals("quit")));

                        System.out.println("Bye-Message sent:\n");
                        clientSocket.close();
                        state = null;
                        break;
                    case States.ONLINE:
                        

                        request = new WhoOnlineMessage(
                                new MsgHeader(MsgType.WHO_ONLINE, 1, 1, System.currentTimeMillis()));
                        sendData = codec.encode(request);

                        sendData(request, codec, outToServer);

                        if (response.header().type() == MsgType.ERROR) {
                            ErrorMessage error;
                            error = (ErrorMessage) response;
                            System.out
                                    .println("No online users. Error: " + error.reason()
                                            + ",\r\n You will be logged out");
                            state = States.CONNECTEDTOSERVER;
                            break;
                        }

                        do {
                            System.out.println("Who do want to chat with? Please type name or 'logout' or 'refresh'");
                            System.out.println(response.toString());

                            userChoice = inFromUser.readLine();

                            if (userChoice.equals("logout")) {

                                request = new LogoutMessage(
                                        new MsgHeader(MsgType.LOGOUT, 1, 1, System.currentTimeMillis()));

                                sendData(request, codec, outToServer);
                                System.out.println("Bye-Message sent:\n" + sendData);
                                clientSocket.close();
                                state = null;
                                return;

                            } else if (userChoice.equals("refresh")) {
                                request = new WhoOnlineMessage(
                                        new MsgHeader(MsgType.WHO_ONLINE, 1, 1, System.currentTimeMillis()));
                                sendData = codec.encode(request);

                                sendData(request, codec, outToServer);
                                // response = receiveData(codec, inFromServer);

                                if (response.header().type() == MsgType.ERROR) {
                                    ErrorMessage error;
                                    error = (ErrorMessage) response;
                                    System.out
                                            .println("No online users. Error: " + error.reason()
                                                    + ",\r\n You will be logged out");
                                    state = States.CONNECTEDTOSERVER;
                                    break;
                                }
                            }
                        } while (userChoice.equals("refresh"));

                        request = new ChatReqMessage(
                                new MsgHeader(MsgType.CHAT_REQ, 1, 1, System.currentTimeMillis()), userChoice);
                        sendData(request, codec, outToServer);
                        // response = receiveData(codec, inFromServer);

                        // TODO Logic to make sure forwarded msg is received

                        if (response.header().type() == MsgType.ERROR) {
                            ErrorMessage error;
                            error = (ErrorMessage) response;
                            System.out.println("Chat could not be established. Error: " + error.reason()
                                    + ",\r\n You will be logged out");
                            state = States.CONNECTEDTOSERVER;
                            break;
                        }

                        break;
                    case States.CONNECTEDTOCLIENT:
                        UDPConnectionSend(requested_udpPort, reqAddress, personal_port);
                }
            }

        } catch (Exception e) {
            // TODO: handle exception
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

    private int establishUDPSocket() throws IOException {
        udpRunning = true;
        try {
            clientUdpSocket = new DatagramSocket();
            System.out.println("ClientSocket established on port: " + clientUdpSocket.getLocalPort());
            clientUdpSocket.setSoTimeout(5000); // 5000 ms = 5 Sekunden
            System.out.println("SocketTimeout set to 5 seconds");
            return clientSocket.getLocalPort();

        } catch (Exception e) {
            e.getMessage();
            e.printStackTrace();
            System.err.println("Initialization failed");
            clientUdpSocket.close();
            return 0;
        }
    }

    private void UDPConnectionListener() throws IOException {

        Thread message_receive_thread = new Thread(() -> {

            while (udpRunning) {

                byte[] receiveData = new byte[1024];
                Message udp_message;

                try {
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    clientUdpSocket.receive(receivePacket);
                    int length = receivePacket.getLength();
                    byte[] data = Arrays.copyOf(receivePacket.getData(), length);

                    // if it is an ACK because it would just be a zero
                    if (length == 1 && data[0] == 48) {
                        udpStates = UDPStates.ACK_RECEIVED;
                        System.out.println("ACK RECEIVED!");
                        continue;
                    }

                    udp_message = codec.decode(data);

                    System.out.println(udp_message.toString());
                    udpStates = UDPStates.MESSAGE_RCV;

                    ChatMessageACK chat_message_ack = new ChatMessageACK(0);
                    byte[] msgData = chat_message_ack.data();
                    DatagramPacket sendPacket = new DatagramPacket(msgData, msgData.length, receivePacket.getAddress(),
                            receivePacket.getPort());

                    clientUdpSocket.send(sendPacket);
                    System.out.println("ACK SENT");

                } catch (SocketTimeoutException c) {
                    if (udpStates == UDPStates.WAIT_FOR_ACK && lastPacket != null) {
                        try {
                            clientUdpSocket.send(lastPacket);

                        } catch (IOException e) {
                            e.getMessage();
                        }
                    }
                    udpStates = UDPStates.LOST_ACK;
                } catch (IOException e) {
                    e.getMessage();
                }
            }

        });
        message_receive_thread.start();
    }

    private void UDPConnectionSend(int users_port, InetAddress ipAddress, int personal_port) throws IOException {

        udpStates = UDPStates.ACK_RECEIVED;

        Thread message_send_thread = new Thread(() -> {
            String message = null;

            byte[] msgData;

            try {
                while (udpRunning && state == States.CONNECTEDTOCLIENT) {
                    System.out.print("Message: ");

                    message = inFromUser.readLine();
                    if (message == null || message.equals("exit")) {
                        udpRunning = false;
                        break;
                    }

                    ChatMessage chat_message = new ChatMessage(
                            new MsgHeader(MsgType.CHAT_MSG, 1, 1, System.currentTimeMillis()), message);

                    msgData = codec.encode(chat_message);

                    DatagramPacket sendPacket = new DatagramPacket(msgData, msgData.length, ipAddress, users_port);
                    lastPacket = sendPacket;
                    udpStates = UDPStates.WAIT_FOR_ACK;
                    clientUdpSocket.send(sendPacket);
                }
            } catch (IOException e) {
                e.getMessage();
            }

        });

        message_send_thread.start();

        state = States.ONLINE;
    }
