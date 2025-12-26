
package ThreadedServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Base64;
import java.util.HashMap;

import codec.SimpleTextCodec;
import messages.Message;
import messages.MsgHeader;
import messages.MsgType;
import messages.request.ChatReqMessage;
import messages.request.LoginMessage;
import messages.request.PortKeyMessage;
import messages.request.RegisterMessage;
import messages.response.ErrorMessage;
import messages.response.ForwardChatRequestMessage;
import messages.response.LoginOkMessage;
import messages.response.LogoutOkMessage;
import messages.response.RegistrationOkMessage;
import messages.response.SendPortMessage;
import messages.response.UsersOnlineMessage;
import user.User;
import user.UserManagement;

public class ThreadedServer {

     UserManagement userManagement = new UserManagement();

     String encodedResponse;
     SimpleTextCodec codec = new SimpleTextCodec();
     Message clientMessage, response, request;
     boolean headerDone = false;
     private ServerSocket welcomeSocket;
     HashMap<String, DataOutputStream> connectedClients = new HashMap<>();

     public ThreadedServer() {
          try {
               welcomeSocket = new ServerSocket(6324);
               System.out.println("Warte auf Client...");
          } catch (IOException e) {
               e.printStackTrace();
          }
     }

     public void run_forever() {
          while (true) {
               try {
                    final Socket socket = this.welcomeSocket.accept();
                    System.out.println("Client hat sich verbunden: " + socket.getInetAddress());

                    Thread thread = new Thread(() -> handleClient(socket));
                    thread.start();

               } catch (IOException e) {
                    e.printStackTrace();
               }
          }
     }

     public void handleClient(Socket socket) {
          DataInputStream inFromClient = null;
          DataOutputStream outToClient = null;
          User currUser = null;
          String requested_user = null;
          User reqUser = null;
          try {
               inFromClient = new DataInputStream(socket.getInputStream());
               outToClient = new DataOutputStream(socket.getOutputStream());

               while (true) {

                    clientMessage = receiveData(codec, inFromClient);
                    if (clientMessage == null) {
                         break;
                    }

                    switch (clientMessage.header().type()) {
                         case MsgType.REGISTER:

                              RegisterMessage message = (RegisterMessage) clientMessage;
                              InetAddress address = socket.getInetAddress();

                              currUser = new User(message.getEmail(), message.getUsername(),
                                        message.getHashedPassword(), message.getSaltEncoded(), message.getIterations(),
                                        address.getHostAddress(), socket, outToClient);

                              if (userManagement.getRegisteredUsers().contains(currUser)) {
                                   response = new ErrorMessage(
                                             new MsgHeader(MsgType.ERROR, 1, 1, System.currentTimeMillis()), 1);
                                   sendData(response, codec, outToClient);
                                   System.out.println("Registration ErrorMessage sent.");
                                   break;

                              }
                              userManagement.register(currUser);

                              response = new RegistrationOkMessage(
                                        new MsgHeader(MsgType.REGISTRATION_OK, 1, 1, System.currentTimeMillis()));
                              sendData(response, codec, outToClient);
                              System.out.println("Registration Ok message sent.");
                              break;

                         case MsgType.LOGIN:
                              LoginMessage loginMsg = (LoginMessage) clientMessage;
                              if (userManagement.findRegisteredUser(loginMsg.getEmail()) == null) {
                                   response = new ErrorMessage(
                                             new MsgHeader(MsgType.ERROR, 1, 1, System.currentTimeMillis()), 3);
                                   sendData(response, codec, outToClient);
                                   System.out.println("Login ErrorMessage sent.");
                                   break;

                              } else
                                   try {
                                        if (!verifyPassword(loginMsg.getPassword(),
                                                  userManagement.findRegisteredUser(loginMsg.getEmail())
                                                            .gethashedPassword(),
                                                  userManagement.findRegisteredUser(loginMsg.getEmail())
                                                            .getSaltEncoded(),
                                                  userManagement.findRegisteredUser(loginMsg.getEmail())
                                                            .getIterations())) {
                                             response = new ErrorMessage(
                                                       new MsgHeader(MsgType.ERROR, 1, 1, System.currentTimeMillis()),
                                                       4);
                                             sendData(response, codec, outToClient);
                                             System.out.println("Login ErrorMessage sent.");
                                             break;

                                        }

                                        else if (verifyPassword(loginMsg.getPassword(),
                                                  userManagement.findRegisteredUser(loginMsg.getEmail())
                                                            .gethashedPassword(),
                                                  userManagement.findRegisteredUser(loginMsg.getEmail())
                                                            .getSaltEncoded(),
                                                  userManagement.findRegisteredUser(loginMsg.getEmail())
                                                            .getIterations())) {
                                             userManagement.setOnline(userManagement.findRegisteredUser(loginMsg.getEmail()));

                                             response = new LoginOkMessage(
                                                       new MsgHeader(MsgType.LOGIN_OK, 1, 1,
                                                                 System.currentTimeMillis()));
                                             sendData(response, codec, outToClient);
                                             System.out.println("Login Ok message sent.");
                                        }
                                   } catch (Exception e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                   }

                              break;
                         case MsgType.PORT_KEY:
                              PortKeyMessage pk = (PortKeyMessage) clientMessage;
                              currUser.setPublicKey(pk.getPublicKey());
                              currUser.setUdpPort(pk.getGetUdpPort());
                              System.out.println("Added Port " + pk.getGetUdpPort() + " and Key to " + pk.getPublicKey()
                                        + "  User Information");
                              break;

                         case MsgType.WHO_ONLINE:
                              if (userManagement.getOnlineUsers().isEmpty()) {
                                   response = new ErrorMessage(
                                             new MsgHeader(MsgType.ERROR, 1, 1, System.currentTimeMillis()), 5);
                                   sendData(response, codec, outToClient);
                                   System.out.println("No OnlineUsers. ErrorMessage sent.");
                                   break;

                              }
                              response = new UsersOnlineMessage(
                                        new MsgHeader(MsgType.USERS_ONLINE, 1, 1, System.currentTimeMillis()),
                                        userManagement.getOnlineUsers());
                              sendData(response, codec, outToClient);
                              System.out.println("List of Online-Users sent.");
                              break;

                         case MsgType.CHAT_REQ:
                              ChatReqMessage chatReqMessage = (ChatReqMessage) clientMessage;
                              requested_user = chatReqMessage.getRequested_user();
                              reqUser = userManagement.findRegisteredUser(requested_user);

                              if (reqUser == null) {
                                   response = new ErrorMessage(
                                             new MsgHeader(MsgType.ERROR, 1, 1, System.currentTimeMillis()), 6);
                                   sendData(response, codec, outToClient);
                                   System.out.println("User not found. ErrorMessage sent.");
                                   break;

                              }
                              response = new SendPortMessage(
                                        new MsgHeader(MsgType.SEND_PORT, 1, 1, System.currentTimeMillis()),
                                        reqUser.getUdpPort(), " ", reqUser.getIp());
                              System.out.println(reqUser.getUdpPort());
                              System.out.println(reqUser.getIp()); // TODO here 0 trace back
                              sendData(response, codec, outToClient);
                              System.out.println("Requested Port forwarded to " + currUser.getName());

                              response = new SendPortMessage(
                                        new MsgHeader(MsgType.FWD_CHAT_REQ, 1, 1, System.currentTimeMillis()),
                                        currUser.getUdpPort(),
                                        currUser.getPublicKey(),
                                        currUser.getIp());
                              System.out.println(currUser.getUdpPort()); // TODO also 0
                              System.out.println(currUser.getIp());

                              DataOutputStream outToRecipient = new DataOutputStream(
                                        reqUser.getTcpSocket().getOutputStream());
                              sendData(response, codec, outToRecipient);
                              System.out.println("Message forwarded to " + reqUser.getName());
                              break;

                         case MsgType.QUIT_REQ:

                              return;

                         case MsgType.LOGOUT:

                              userManagement.setOffline(
                                        userManagement.findRegisteredUser(socket.getInetAddress().getHostAddress()));

                              response = new LogoutOkMessage(
                                        new MsgHeader(MsgType.LOGOUT, 1, 1, System.currentTimeMillis()));
                              sendData(response, codec, outToClient);
                              System.out.println("Logout Ok message sent. User looged out.");
                              break;

                         default:
                              response = new ErrorMessage(
                                        new MsgHeader(MsgType.ERROR, 1, 1, System.currentTimeMillis()), 7);
                              sendData(response, codec, outToClient);
                              System.out.println("Undefned Error. ErrorMessage sent.");
                              break;
                    }
               }

          } catch (IOException e) {
               e.printStackTrace();
          } finally {

               try {
                    if (inFromClient != null)
                         inFromClient.close();
               } catch (IOException ignored) {
               }
               try {
                    if (outToClient != null)
                         outToClient.close();
               } catch (IOException ignored) {
               }
               try {
                    socket.close();
               } catch (IOException ignored) {
               }
          }
     }

     private static void sendData(Message msg, SimpleTextCodec codec, DataOutputStream outToClient) {
          byte[] sendData = null;
          try {
               sendData = codec.encode(msg);
               outToClient.writeInt(sendData.length);
               outToClient.write(sendData);
               outToClient.flush();
          } catch (IOException e) {

               e.getMessage();
               e.printStackTrace();
               System.err.println("Could not send Data to Sever");
          }
     }

     public static boolean verifyPassword(String inputPassword, String storedHash, String storedSalt, int iterations)
               throws Exception {
          byte[] salt = Base64.getDecoder().decode(storedSalt);
          return storedHash.equals(inputPassword);
     }

     private static Message receiveData(SimpleTextCodec codec, DataInputStream inFromCLient) {
          Message response = null;
          byte[] receiveData = new byte[1024];
          try {
               int length = inFromCLient.readInt();
               receiveData = inFromCLient.readNBytes(length);
               response = codec.decode(receiveData);

          } catch (IOException e) {
               e.getMessage();
               e.printStackTrace();
               System.err.println("Could not receive Data from Sever");
               return null;
          }

          return response;

     }

     public static void main(String[] args) {
          ThreadedServer ts = new ThreadedServer();
          ts.run_forever();
     }
}
