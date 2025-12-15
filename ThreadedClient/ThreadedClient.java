import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.DatagramSocket;
import java.net.Socket;

import codec.SimpleTextCodec;
import messages.Message;
import messages.MsgHeader;
import messages.MsgType;
import messages.request.LoginMessage;
import messages.request.RegisterMessage;
import messages.request.WhoOnlineMessage;
import messages.response.ErrorMessage;

public class ThreadedClient {
    public static void main(String[] args) throws Exception {

        Socket clientSocket;
        DatagramSocket clientUdpSocket;
        States state = States.DISCONNECTED;
        BufferedReader inFromUser;
        DataOutputStream outToServer;
        DataInputStream inFromServer;
        SimpleTextCodec codec = new SimpleTextCodec();
        String userChoice, email, name, password;
        int udpPort;
        Message response, request;
        byte[] sendData = null;
        byte[] receiveData = new byte[1024];

        // connect (tbh I don't know where to start the thread probably here but.. )

        try {
            clientSocket = new Socket("localhost", 12345);
            System.out.println("Client connected end with \"END\"");
            state = States.CONNECTEDTOSERVER;

            inFromUser = new BufferedReader(new InputStreamReader(System.in));
            outToServer = new DataOutputStream(clientSocket.getOutputStream()); // different reader and writer because
                                                                                // of byte-arrays
            inFromServer = new DataInputStream(clientSocket.getInputStream());

        } catch (Exception e) {
            e.getMessage();
            e.printStackTrace();
            System.err.println("Initialization failed");
            return;
        }

        while (clientSocket.isConnected()) {

            do {
                System.out.println("Are you registered? y/n");
                userChoice = inFromUser.readLine();
            } while (!userChoice.equals("y") || !userChoice.equals("n"));

            // register (Server has to add data to registeredUserList)
            if (userChoice.equals("n")) {
                reg: do {
                    System.out.println("Please enter your email");
                    email = inFromUser.readLine();
                    System.out.println("Please choose your name");
                    name = inFromUser.readLine();
                    System.out.println("Please choose your password");
                    password = inFromUser.readLine();

                    // do we want to ask specifically for the udpPort? In real life this does not
                    // make sense but maybe here it does?

                    request = new RegisterMessage(new MsgHeader(MsgType.REGISTER, 1, 1, System.currentTimeMillis()),
                            email, name, password);
                    sendData = codec.encode(request);

                    outToServer.writeInt(sendData.length);
                    outToServer.write(sendData);
                    outToServer.flush();

                    // need to simulate error while registration e.g. username already taken
                    int length = inFromServer.readInt();
                    receiveData = inFromServer.readNBytes(length);

                    response = codec.decode(receiveData);

                    if (response.header().type() == MsgType.ERROR) {
                        ErrorMessage error;
                        error = (ErrorMessage) response;
                        System.out.println("Registration failed:" + error.reason() + "\n Please try again");
                        // we should define errorcodes to simplify this
                        continue reg;
                    }

                } while (response.header().type() == MsgType.ERROR);
                System.out.println("Registration successful");

            }

            // no else since login is necessary anayway
            do {

                System.out.println("Please enter your login-email");
                email = inFromUser.readLine();
                System.out.println("Please enter your password");
                password = inFromUser.readLine();

                request = new LoginMessage(new MsgHeader(MsgType.LOGIN, 1, 1, System.currentTimeMillis()), email,
                        password);
                sendData = codec.encode(request);

                outToServer.write(sendData);
                outToServer.flush();

                int length = inFromServer.readInt();
                receiveData = inFromServer.readNBytes(length);

                response = codec.decode(receiveData);

                if (response.header().type() == MsgType.ERROR) {
                    ErrorMessage error;
                    error = (ErrorMessage) response;
                    System.out.println("Login failed: " + error.reason() + "\n Please try again");
                    return; // also return to beginning of do while and TODO implement possibilty to quit
                            // and stop login
                }

            } while (response.header().type() == MsgType.ERROR);
            System.out.println("Login successful");
            state = States.ONLINE;

            // TODO Quit Message? Options?

            request = new WhoOnlineMessage(new MsgHeader(MsgType.WHO_ONLINE, 1, 1, System.currentTimeMillis()));
            sendData = codec.encode(request);

            outToServer.write(sendData);
            outToServer.flush();

            int length = inFromServer.readInt();
            receiveData = inFromServer.readNBytes(length);

            response = codec.decode(receiveData);
            System.out.println(response.toString());

            if (response.header().type() == MsgType.ERROR) {
                    ErrorMessage error;
                    error = (ErrorMessage) response;
                    System.out.println("No onine users" + error.reason()); //  TODO on server: check if online user list != null    
                    return;        
                }

            



        }

    }
}
