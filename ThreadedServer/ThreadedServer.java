package ThreadedServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import codec.SimpleTextCodec;
import messages.Message;
import messages.MsgHeader;
import messages.MsgType;
import messages.request.LoginMessage;
import messages.request.RegisterMessage;
import messages.response.OkMessage;
import messages.response.UsersOnlineMessage;
import user.User;
import user.UserManagement;

public class ThreadedServer {

     UserManagement userManagement = new UserManagement();

     String encodedResponse;
     SimpleTextCodec codec = new SimpleTextCodec();
     Message clientMessage, response;
     boolean headerDone = false;
     private ServerSocket welcomeSocket;

     public ThreadedServer() {
          try {
               welcomeSocket = new ServerSocket(12345);
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

                    final Thread thread = new Thread(() -> {
                         DataInputStream inFromClient = null;
                         DataOutputStream outToClient = null;

                         try {
                              inFromClient = new DataInputStream(socket.getInputStream());
                              outToClient = new DataOutputStream(socket.getOutputStream());
                         } catch (IOException e) {
                              e.printStackTrace();
                         }

                         clientMessage = receiveData(codec, inFromClient);
                         switch (clientMessage.header().type()) {
                              case MsgType.REGISTER:

                                   // TODO CHECK FOR EXISTING USERNAME

                                   RegisterMessage message = (RegisterMessage) clientMessage;
                                   int udpPort = (int) (Math.random() * 9000) + 1000;
                                   User user = new User(message.getEmail(), message.getUsername(),
                                             message.getPassword(), socket.getInetAddress(), udpPort);
                                   userManagement.register(user);
                                   response = new OkMessage(
                                             new MsgHeader(MsgType.OK, 1, 1, System.currentTimeMillis()));
                                   sendData(response, codec, outToClient);
                                   System.out.println("Registration Ok message sent.");
                                   break;

                              case MsgType.LOGIN:
                                   LoginMessage loginMsg = (LoginMessage) clientMessage;
                                   userManagement.setOnline(userManagement.findRegisteredUser(loginMsg.getEmail()));

                                    response = new OkMessage(new MsgHeader(MsgType.OK, 1, 1, System.currentTimeMillis()));
                                   sendData(response, codec, outToClient);
                                   System.out.println("Login Ok message sent.");
                                   break;

                              case MsgType.WHO_ONLINE:
                                   response= new UsersOnlineMessage(new MsgHeader(MsgType.USERS_ONLINE, 1, 1, System.currentTimeMillis()), userManagement.getOnlineUsers());
                                     sendData(response, codec, outToClient);
                                   System.out.println("List of Online-Users sent message sent.");
                                   break;

                              case MsgType.CHAT_REQ:

                              


                                   break;

                              case MsgType.QUIT:
                                   break;

                              case MsgType.LOGOUT:
                                   break;

                              default:
                                   break;

                         }

                    });
                    thread.start();
               } catch (IOException e) {
                    e.printStackTrace();
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
