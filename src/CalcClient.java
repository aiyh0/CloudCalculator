import java.util.Scanner;
import java.net.*;
import java.io.*;

public class CalcClient {
    public static void main (String[] args) throws Exception{
        String host;
        int port;

        // open server_info file
        try(
            Scanner sc = new Scanner(new FileInputStream("src/server_info.dat"))
        ){
            host = sc.nextLine();
            port = Integer.parseInt(sc.nextLine());
            System.out.println("Read successful. Address copied.");
        } catch(Exception e){
            host = "localhost";
            port = 9999;
            System.err.println(e.getMessage());
            System.err.println("Read failed. Set address to default.");
        }
        
        // open socket
        try(
            Socket socket = new Socket(host, port);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            Scanner sc = new Scanner(System.in);
        ){
            System.out.println("Connected to Server");

            // read and write according to protocol
            while(true){
                String expression = sc.nextLine();

                // RequestProtocol: END
                if(expression.equalsIgnoreCase("bye")){
                    out.writeObject(RequestProtocol.createEnd());
                    out.flush();
                    break;
                }

                // RequestProtocol: CALC <expression>
                out.writeObject(RequestProtocol.createCalculate(expression));
                out.flush();               
                ResponseProtocol response = (ResponseProtocol)in.readObject();

                // ResponseProtocol: SUCCUESS <value>
                if(response.getStatus() == Status.SUCCESS){
                    String answer = response.getValue();
                    System.out.println(answer);
                }
                // ResponseProtocol: ERROR <errorCode>
                else{
                    System.out.println("Error code: " + response.getErrorCode());
                }
            }
        } catch(IOException e){
            System.err.println(e.getMessage());
        }
    }
}