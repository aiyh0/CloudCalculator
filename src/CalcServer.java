import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.NoSuchElementException;

public class CalcServer {
    public static void main(String[] args) throws Exception {
        // String value = calculate("26-8+6=35");
        // System.out.println(value);

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
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ){
                String clientAddress = socket.getRemoteSocketAddress().toString();
                System.out.println("Connected to client: " + clientAddress);
               
                // read and write object according to protocol
                RequestProtocol request;
                while((request = (RequestProtocol)in.readObject()) != null){

                    // RequestProtocol: END
                    if(request.getOption() == Option.END){
                        System.out.println(clientAddress + " ended connection");
                        break;
                    }

                    // RequestProtocol: CALC <expression>
                    String expression = request.getExpression();
                    System.out.println(clientAddress + ": " + expression);

                    // ResponseProtocol: SUCCUESS <value>
                    try{             
                        String answer = calculate(expression);
                        out.writeObject(ResponseProtocol.createSuccess(answer));      
                    } 
                    // ResponseProtocol: ERROR <errorCode>
                    catch(ArithmeticException e){ // divide by zero                       
                        out.writeObject(ResponseProtocol.createError(ErrorCode.DIVIDE_BY_ZERO));
                    } catch(NumberFormatException e){ // fail to parseDouble (invalid operator or operand) 
                        out.writeObject(ResponseProtocol.createError(ErrorCode.INVALID_TOKEN));
                    } catch(NoSuchElementException e){ // pop empty stack (missing operand)        
                        out.writeObject(ResponseProtocol.createError(ErrorCode.MISSING_OPERAND));
                    } catch(MissingOperatorException e){
                        out.writeObject(ResponseProtocol.createError(ErrorCode.MISSING_OPERATOR));
                    } catch(ParenthesisException e){
                        out.writeObject(ResponseProtocol.createError(ErrorCode.MISMATCHING_PARENTHESIS));
                    } finally{
                        out.flush();
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

        // tokenize expression
        String exp = expression.trim();
        String temp = "";
        Deque<String> tokens = new ArrayDeque<>();
        for(int i=0; i<exp.length(); i++){
            String c = "" + exp.charAt(i);

            if(precedence.containsKey(c) || c.equals("(") || 
                c.equals(")") || c.equals(" ")){
                if(temp.length() > 0){
                    tokens.add(temp);
                    temp = "";
                }
                if(c.equals(" ")){
                    continue;
                }
                tokens.add(c);
            }
            else{
                temp += c;
            }
        }
        if(temp.length() > 0){
            tokens.add(temp);
        }

        // convert into postfix expression;
        Deque<String> output = new ArrayDeque<>();
        Deque<String> opstack = new ArrayDeque<>();

        for(String token: tokens){
            if(token.equals("(")){
                opstack.push("(");
            } 
            else if(token.equals(")")){
                while(!opstack.isEmpty() && !opstack.peek().equals("(")){
                    output.add(opstack.pop());
                }
                if(opstack.isEmpty()){
                    throw new ParenthesisException("Missing opening parenthesis");
                }
                opstack.pop();
            }
            else{
                if(precedence.containsKey(token)){
                    while(!opstack.isEmpty()){
                        if(opstack.peek().equals("(")) break;
                        if(precedence.get(token) > precedence.get(opstack.peek())) break;
                        if(precedence.get(token) == precedence.get(opstack.peek()) && rightAssoc.get(token)) break;
                        output.add(opstack.pop());
                    }
                    opstack.push(token);
                }
                else{
                    output.add("" + Double.parseDouble(token));
                }
            }
        }
        while(!opstack.isEmpty()){
            if(opstack.peek().equals("(")){
                throw new ParenthesisException("Missing closing parenthesis");
            }
            output.add(opstack.pop());
        }

        // evaluate postfix expression
        Deque<Double> result = new ArrayDeque<>();
        for(String token: output){
            if(precedence.containsKey(token)){
                double right = result.pop(); 
                double left = result.pop();

                switch(token){
                    case "+": result.push(left + right); break;
                    case "-": result.push(left - right); break;
                    case "*": result.push(left * right); break;
                    case "^": result.push(Math.pow(left, right)); break;
                    case "/": 
                        if(right == 0){
                            throw new ArithmeticException();
                        }
                        result.push(left / right); 
                        break;
                    case "%": 
                        if(right == 0){
                            throw new ArithmeticException();
                        }
                        result.push(left % right); 
                        break;
                    default: 
                }
            }
            else{
                result.push(Double.parseDouble(token));
            }
        }

        if(result.size() > 1){
            throw new MissingOperatorException();
        }
        return String.valueOf(result.peek());
    }

    // custom exception for handling special case
    private static class MissingOperatorException extends RuntimeException {
        MissingOperatorException(String message){
            super(message);
        }
        MissingOperatorException(){
            super("Missing operator");
        }
    }
    private static class ParenthesisException extends RuntimeException {
        ParenthesisException(String message){
            super(message);
        }
        ParenthesisException(){
            super("Missing Parenthesis");
        }
    }
}
