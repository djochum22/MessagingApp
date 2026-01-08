package ThreadedClient;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

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
import messages.request.RegisterMessage;
import messages.request.WhoOnlineMessage;
import messages.response.ErrorMessage;
import messages.response.SendPortMessage;

public class TestClient2 {
    private SSLSocket clientSocket;
    private UDPHandler udpHandler;

    private volatile States state = null;
    private BufferedReader inFromUser;
    private DataOutputStream outToServer;
    private DataInputStream inFromServer;
    private String userChoice, email, name;
    private char [] password;
    private Message response = null, request = null;
    private SimpleTextCodec codec;
    private byte[] sendData = null;
    private int requested_udpPort;
    private String requestedKey = null;
    private InetAddress reqAddress = null;
    private boolean udpRunning = false;
    private DatagramPacket lastPacket = null;
    private boolean running = false;
    private static final int SALT_LENGTH = 16;
    private static final int ITERATIONS = 100_000;
    private static final int KEY_LENGTH = 256;
    private String hashedPasswort;
    private byte [] salt;
    private String saltEncoded;
    private TrustManager[] trustManagers;
    private SSLContext sslContext;
    private SSLSocketFactory sslSocketFactory;
    private KeyPairGenerator kpg;
    private KeyPair kp;
    private PublicKey pub, reqPublicKey;
    private PrivateKey priv;
    private boolean exit = false;
    private boolean loggedIn = false;


    public TestClient2() {
        codec = new SimpleTextCodec();
        try {
            trustManagers = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        // this still needs to be implemented as all Servers are being Trusted
                        // TODO check with Schaible if this needs to be done
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }
            };
        } catch (Exception e) {
            e.getStackTrace();
        }
        
    }

    public static void main(String[] args) throws IOException {
        TestClient2 client = new TestClient2();
        client.run();
    }

    public void run() throws IOException {

        try {
            // Create SSL context
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagers, new SecureRandom());

            // Create SSL socket factory
            sslSocketFactory = sslContext.getSocketFactory();

            try {
                // Create SSL socket
                clientSocket = (SSLSocket)sslSocketFactory.createSocket("localhost", 6324);

                // clientSocket = new Socket("localhost", 6324);
                System.out.println("Client connected end with \"END\"");
                running = true;
                state = States.WAIT_FOR_REGISTRATION;
                inFromUser = new BufferedReader(new InputStreamReader(System.in));
                outToServer = new DataOutputStream(clientSocket.getOutputStream()); // // of byte-arrays
                inFromServer = new DataInputStream(clientSocket.getInputStream());
                udpHandler = new UDPHandler(inFromUser);

            } catch (Exception e) {
                e.getMessage();
                e.printStackTrace();
                System.err.println("Initialization failed");
                return;
            }

            Thread listener = new Thread(() -> {
                while (running) {

                    response = receiveData(codec, inFromServer);
                    MsgType type = response.header().type();
                    switch (type) {
                        case LOGIN_OK:

                            state = States.LOGGEDIN;
                            System.out.println("Login Successful");

                            break;
                        case REGISTRATION_OK:

                            state = States.WAITFORLOGIN;
                            System.out.println("Registration ok. Please login.");
                            break;
                        case USERS_ONLINE:

                            state = States.USERS_REQUESTED;
                            System.out.println(response.toString());
                            break;
                        case SEND_PORT:
                            System.out.println("Requested UserPort and IP-Adress received. Starting Chat...");
                            SendPortMessage port = (SendPortMessage) response;
                            try {
                                reqAddress = InetAddress.getByName(port.getIpAddress());
                                reqPublicKey = port.getPublicKey();

                            } catch (UnknownHostException e) {
                                e.printStackTrace();
                            }
                            requested_udpPort = port.getPort();
                            udpHandler.setPeer(requested_udpPort, reqAddress); // is created

                            state = States.CHATTING;

                            break;

                        case LOGOUT_OK:

                            try {
                                System.out.println("Closing UDP-Socket...");
                                clientSocket.close();
                                udpHandler.getClientUdpSocket().close();
                                state = null;
                                loggedIn = false;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            break;

                        case CHAT_MSG:

                            ChatMessage chatMessage = (ChatMessage) response;

                            System.out.println("New ChatMessage: " + chatMessage.text());
                            state = States.CHATTING;
                            break;

                        case ERROR:

                            ErrorMessage error = (ErrorMessage) response;
                            int errReason = error.reason();

                            switch (errReason) {
                                case 1:
                                    System.out.println("Registration failed. User already in Database.");
                                    state = States.USERS_REQUESTED;
                                    break;
                                case 2:
                                    System.out.println("Registration failed. Unknown registration error.");
                                    state = States.USERS_REQUESTED;
                                    break;
                                case 3:
                                    System.out.println("Login failed. Username doesn't exist.");
                                    state = States.WAITFORLOGIN;
                                    break;
                                case 4:
                                    System.out.println("Login failed. Wrong password.");
                                    state = States.WAITFORLOGIN;
                                    break;
                                case 5:
                                    System.out.println("WhoOnline failed. No User online.");
                                    state = States.USERS_REQUESTED;
                                    break;
                                case 6:
                                    System.out.println("ChatReq failed. Username not found.");
                                    state = States.USERS_REQUESTED;
                                    break;
                                default:
                                    System.out.println("Unknown Error");
                                    state = States.WAITFORLOGIN;
                                    break;
                            }

                        default:
                            break;
                    }
                }
            });
            listener.start();

            while (running) {

                switch (state) {
                    case States.WAIT_FOR_REGISTRATION:

                        System.out.println("Hello please register");
                        System.out.println("Please enter your email");
                        email = inFromUser.readLine();
                        System.out.println("Please choose your name");
                        name = inFromUser.readLine();
                        System.out.println("Please choose your password");
                        password = inFromUser.readLine().toCharArray();
                        salt = generateSalt();
                        saltEncoded = Base64.getEncoder().encodeToString(salt);
                        hashedPasswort=hashPassword(password,salt, ITERATIONS);

                        request = new RegisterMessage(
                                new MsgHeader(MsgType.REGISTER, 1, 1, System.currentTimeMillis()),
                                email, name, hashedPasswort,saltEncoded,ITERATIONS);

                        try {
                            sendData(request, codec, outToServer);
                            System.out.println("Registration sent to Server. HashedPW " + hashedPasswort + ", Salt " + saltEncoded + ", Iterations " + ITERATIONS);

                        } catch (Exception e) {
                            System.err.println("Message could not be sent");
                            e.printStackTrace();
                            e.getMessage();
                        }

                        state = States.WAITING_FOR_RESPONSE;
                        break;

                    case States.WAITFORLOGIN:
                        System.out.println("Please login.");

                        System.out.println("Please enter your login-email");
                        email = inFromUser.readLine();
                        System.out.println("Please enter your password");
                        password = inFromUser.readLine().toCharArray();
                        hashedPasswort=hashPassword(password,salt, ITERATIONS);

                        request = new LoginMessage(
                                new MsgHeader(MsgType.LOGIN, 1, 1, System.currentTimeMillis()),
                                email,
                                hashedPasswort);
                        try {
                            sendData(request, codec, outToServer);

                        } catch (Exception e) {
                            System.err.println("Message could not be sent");
                            e.printStackTrace();
                            e.getMessage();
                        }

                        state = States.WAITING_FOR_RESPONSE;

                        break;

                    case LOGGEDIN:
                        if (!loggedIn) {
                            if (!udpRunning) {
                                udpHandler.start(); // startet startListener()
                                udpRunning = true;
                            }

                            System.out.println("UDP Port established. Sending PortNo " + udpHandler.getPort()
                                    + " and Public Key to Server..");

                            // Generating clients keys
                            kpg = KeyPairGenerator.getInstance("RSA");
                            kpg.initialize(2048);
                            kp = kpg.generateKeyPair();
                            pub = kp.getPublic();
                            priv = kp.getPrivate();
    
                            // Send server clients public key
                            request = new PortKeyMessage(
                                    new MsgHeader(MsgType.PORT_KEY, 1, 1, System.currentTimeMillis()), pub,
                                    udpHandler.getPort());

                            sendData = codec.encode(request);

                            try {
                                sendData(request, codec, outToServer);

                            } catch (Exception e) {
                                System.err.println("Message could not be sent");
                                e.printStackTrace();
                                e.getMessage();
                            }

                            System.out.println(udpHandler.getPort());
                        }

                        loggedIn = true;

                        request = new WhoOnlineMessage(
                                new MsgHeader(MsgType.WHO_ONLINE, 1, 1, System.currentTimeMillis()));
                          try {
                            sendData(request, codec, outToServer);

                        } catch (Exception e) {
                            System.err.println("Message could not be sent");
                            e.printStackTrace();
                            e.getMessage();
                        }

                        System.out.println("Requesting Online-Users from Server...");
                        state = States.WAITING_FOR_RESPONSE;

                        break;

                    case USERS_REQUESTED:
                        System.out.println("Who do you want to chat with? Please type name or 'logout' or 'refresh'");
                        userChoice = inFromUser.readLine();

                        if (userChoice.equals("logout")) {

                            request = new LogoutMessage(
                                    new MsgHeader(MsgType.LOGOUT, 1, 1, System.currentTimeMillis()));

                            try {
                                sendData(request, codec, outToServer);

                            } catch (Exception e) {
                                System.err.println("Message could not be sent");
                                e.printStackTrace();
                                e.getMessage();
                            }
                            state = States.WAITING_FOR_RESPONSE;
                            break;

                        } else if (userChoice.equals("refresh")) {
                            request = new WhoOnlineMessage(
                                    new MsgHeader(MsgType.WHO_ONLINE, 1, 1, System.currentTimeMillis()));
                            sendData = codec.encode(request);

                            try {
                                sendData(request, codec, outToServer);

                            } catch (Exception e) {
                                System.err.println("Message could not be sent");
                                e.printStackTrace();
                                e.getMessage();
                            }

                            state = States.WAITING_FOR_RESPONSE;
                            break;

                        }

                        request = new ChatReqMessage(
                                new MsgHeader(MsgType.CHAT_REQ, 1, 1, System.currentTimeMillis()), userChoice);
                        try {
                            sendData(request, codec, outToServer);

                        } catch (Exception e) {
                            System.err.println("Message could not be sent");
                            e.printStackTrace();
                            e.getMessage();
                        }

                        System.out.println("ChatRequest sent to Server.");
                        state = States.WAITING_FOR_RESPONSE;

                        break;

                    case CHATTING:

                        try {
                            if (udpRunning) {
                                udpHandler.send();
                                
                            }
                        } catch (IOException e) {
                            System.out.println("UDP-Message could not be sent.\nPort: " + requested_udpPort + "\nIP: "
                                    + reqAddress);
                            e.printStackTrace();
                        }

                        if (!exit) {
                            state = States.CHATTING;
                        }
                        break;

                    case States.WAITING_FOR_RESPONSE:

                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                        }
                        break;

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            e.getMessage();
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

   

    // Passwort hashen mit PBKDF2 + HMAC-SHA256
    public static String hashPassword(char[] password, byte[] salt, int iterations) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_LENGTH);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] hash = skf.generateSecret(spec).getEncoded();
        return Base64.getEncoder().encodeToString(hash);
    }

    // Salt generieren
    public static byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);
        return salt;
    }
 

    private class UDPHandler {

        private BufferedReader inFromUser;
        private DatagramSocket clientUdpSocket;
        private boolean udpRunning = false;
        private volatile UDPStates udpStates = null;
        private volatile InetAddress peerAddress;
        private volatile int peerPort;
        private volatile DatagramPacket lastPacket = null;

        public UDPHandler(BufferedReader inFromUser) {
            try {
                this.clientUdpSocket = new DatagramSocket();
                clientUdpSocket.setSoTimeout(1000);

            } catch (SocketException e) {
                e.printStackTrace();
            }
            this.udpRunning = true;
            this.inFromUser = inFromUser;
            this.udpStates = UDPStates.WAIT_FOR_MESSAGE;

        }

        public DatagramSocket getClientUdpSocket() {
            return clientUdpSocket;

        }

        public int getPort() {
            System.out.println(clientUdpSocket.getLocalPort()); // TODO here port is stll ok

            return clientUdpSocket.getLocalPort();
        }

        public boolean udpRunning() {
            return udpRunning;
        }

        public void setPeer(int users_port, InetAddress ipAddress) {
            this.peerAddress = ipAddress;
            this.peerPort = users_port;
        }

        public void start() {
            udpRunning = true;

            try {
                startListener();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void startListener() throws IOException {

            Thread message_receive_thread = new Thread(() -> {

                while (udpRunning) {

                    byte[] receiveData = new byte[1024];
                    Message udp_message;

                    try {
                        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                        clientUdpSocket.receive(receivePacket);
                        int length = receivePacket.getLength();
                        byte[] data = Arrays.copyOf(receivePacket.getData(), length);

                        // decrpt data
                        Cipher dec = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
                        dec.init(Cipher.DECRYPT_MODE, priv);
                        data = dec.doFinal(data);

                        // if it is an ACK because it would just be a zero
                        if (length == 1 && data[0] == 48) {
                            System.out.println("ACK RECEIVED!");
                            udpStates = UDPStates.WAIT_FOR_MESSAGE;
                            lastPacket = null;
                            continue;
                        }

                        udp_message = codec.decode(data);

                        System.out.println(udp_message.toString());

                        ChatMessageACK chat_message_ack = new ChatMessageACK(0);
                        byte[] msgData = chat_message_ack.data();
                        DatagramPacket sendPacket = new DatagramPacket(msgData, msgData.length,
                                receivePacket.getAddress(),
                                receivePacket.getPort());

                        clientUdpSocket.send(sendPacket);
                        System.out.println("ACK SENT");
                        udpStates = UDPStates.WAIT_FOR_MESSAGE;

                    } catch (SocketTimeoutException c) {
                        if (udpStates == UDPStates.WAIT_FOR_ACK && lastPacket != null) {
                            try {
                                clientUdpSocket.send(lastPacket);
                            } catch (IOException e) {
                                e.getMessage();
                            }
                        }
                        udpStates = UDPStates.WAIT_FOR_MESSAGE;
                    } catch (Exception e) {
                        e.getMessage();
                    }
                }

            });
            message_receive_thread.start();
        }

        public void setSocketTimeout(int ms) {

            try {
                clientUdpSocket.setSoTimeout(ms);
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }

        private void send() throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

            // Thread message_receive_thread = new Thread(() -> {
            String message = null;

            if (udpStates==UDPStates.WAIT_FOR_ACK){
                System.out.print("Wait for Ack");
            } else {
                System.out.println("Message: ");
            }

            udpStates = UDPStates.WAIT_FOR_SEND;

            try {
                message = inFromUser.readLine();
                if (message.equals("exit")) {
                    udpStates = UDPStates.WAIT_FOR_MESSAGE;
                    lastPacket = null;
                    udpRunning = false;
                    state = States.LOGGEDIN;
                    exit = true;
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        
            ChatMessage chat_message = new ChatMessage(
                    new MsgHeader(MsgType.CHAT_MSG, 1, 1, System.currentTimeMillis()), message);

            byte[] msgData;

            msgData = codec.encode(chat_message);
            
            // Encrypt data
            Cipher enc = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            enc.init(Cipher.ENCRYPT_MODE, reqPublicKey);
            msgData = enc.doFinal(msgData);
            
            DatagramPacket sendPacket = new DatagramPacket(msgData, msgData.length, peerAddress, peerPort);// TODO
            lastPacket = sendPacket;
            udpStates = UDPStates.WAIT_FOR_ACK;
            clientUdpSocket.send(sendPacket);
            System.out.println("Message sent");

        }

    };
    // );
    // message_receive_thread.start();

}
