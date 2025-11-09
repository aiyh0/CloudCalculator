import java.io.Serializable;

enum Option{
    CALC,
    END
}

public class RequestProtocol implements Serializable{
    private Option opt;
    private String expression;

    public static RequestProtocol createCalculate(String expression){
        RequestProtocol request = new RequestProtocol();
        request.opt = Option.CALC;
        request.expression = expression;
        return request;
    }
    public static RequestProtocol createEnd(){
        RequestProtocol request = new RequestProtocol();
        request.opt = Option.END;
        return request;
    }

    public String getExpression(){ return expression; }
    public Option getOption(){ return opt; }
}
