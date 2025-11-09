import java.util.Scanner;
import java.net.*;
import java.io.*;

public class CalcClient {
    public static void main (String[] args) throws Exception{
        final int PORT = 9999;
        final String HOST = "localhost";

        try(
            Socket socket = new Socket(HOST, PORT);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            Scanner sc = new Scanner(System.in);
        ){
            // Welcome message
            System.out.println(in.readLine());

            while(true){
                String expression = sc.nextLine();
                out.println(expression); // send expression
                if(expression.equalsIgnoreCase("bye")){
                    break;
                }
                String answer = in.readLine(); // get answer
                System.out.println(answer);
            }
        } catch(IOException e){
            System.err.println(e.getMessage());
        }
    }
}
