import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Deque;
import java.util.ArrayDeque;

public class CalcServer {
    public static void main(String[] args) throws Exception {
        //String output = calculate("5 + ( 2 ^ 3 * 4 - 2 ) / 5");
        String output = calculate("5 + 4 * 10 / 0");
        System.out.println(output);

        final int PORT = 9999;
        try(
            ServerSocket serverSocket = new ServerSocket(PORT);
        ){
            while(true){
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                new Thread(clientHandler).start();
            }
        } catch(IOException e){
            System.err.println(e.getMessage());
        }
    }

    // implement runnable interface for thread
    private static class ClientHandler implements Runnable{
        private final Socket socket;

        ClientHandler(Socket socket){
            this.socket = socket;
        }

        @Override
        public void run(){
            try(
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            ){
                String clientAddress = socket.getRemoteSocketAddress().toString();
                System.out.println("Connected to client: " + clientAddress);
                out.println("Caculator program. Write an expression.(Infix)");
                String line;
                while((line = in.readLine()) != null){
                    if(line.equalsIgnoreCase("bye")){
                        out.println("Goodbye");
                        break;
                    }
                    System.out.println(clientAddress + ": " + line);
                    try{
                        String result = calculate(line);
                        out.println(result);                      
                    } catch(Exception e){
                        out.println("Invalid expression: " + e.getMessage());
                    }                   
                }
            } catch(IOException e){
                System.err.println(e.getMessage());
            } catch(Exception e){
                System.err.println(e.getMessage());
            } finally{
                try{
                    socket.close();
                    System.out.println("Client disconnected: " + socket.getRemoteSocketAddress());
                } catch(IOException e){
                    System.err.println(e.getMessage());
                }
            }
        }
    }

    private static String calculate(String expression) throws Exception{
        // convert infix expression into postfix expression
        // using shunting yard algorithm
        Map<String, Integer> precedence = new HashMap<>();
        precedence.put("+",2);
        precedence.put("-",2);
        precedence.put("*",3);
        precedence.put("/",3);
        precedence.put("%",3);
        precedence.put("^",4);

        Map<String, Boolean> rightAssoc = new HashMap<>();
        rightAssoc.put("+",false);
        rightAssoc.put("-",false);
        rightAssoc.put("*",false);
        rightAssoc.put("/",false);
        rightAssoc.put("%",false);
        rightAssoc.put("^",true);

        String[] tokens = expression.trim().split("\\s+");
        String output = " ";
        Deque<String> opstack = new ArrayDeque<>();

        for(String token: tokens){
            if(token.equals("(")){
                opstack.push("(");
            } 
            else if(token.equals(")")){
                while(!opstack.isEmpty() && !opstack.peek().equals("(")){
                    output += opstack.pop() + " ";
                }
                opstack.pop();
            }
            else{
                if(precedence.containsKey(token)){
                    while(!opstack.isEmpty()){
                        if(opstack.peek().equals("(")) break;
                        if(precedence.get(token) > precedence.get(opstack.peek())) break;
                        if(precedence.get(token) == precedence.get(opstack.peek()) && rightAssoc.get(token)) break;
                        output += opstack.pop() + " ";
                    }
                    opstack.push(token);
                }
                else{
                    output += Double.parseDouble(token) + " ";
                }
            }
        }
        while(!opstack.isEmpty()){
            output += opstack.pop() + " ";
        }

        // evaluate postfix expression
        tokens = output.trim().split("\\s+");
        Deque<Double> result = new ArrayDeque<>();
        for(String token: tokens){
            if(precedence.containsKey(token)){
                double right = result.pop(); 
                double left = result.pop();

                switch(token){
                    case "+": result.push(left + right); break;
                    case "-": result.push(left - right); break;
                    case "*": result.push(left * right); break;
                    case "/": result.push(left / right); break;
                    case "%": result.push(left % right); break;
                    case "^": result.push(Math.pow(left, right)); break;
                    default: 
                }
            }
            else{
                result.push(Double.parseDouble(token));
            }
        }

        return String.valueOf(result.peek());
    }
}
