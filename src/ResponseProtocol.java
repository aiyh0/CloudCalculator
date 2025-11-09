import java.io.Serializable;
enum Status{
    SUCCESS,
    ERROR
}

enum ErrorCode{
    DIVIDE_BY_ZERO,
    INVALID_TOKEN,
    MISSING_OPERAND,
    MISSING_OPERATOR,
    MISMATCHING_PARENTHESIS
}

public class ResponseProtocol implements Serializable{
    private Status status;
    private String value;
    private ErrorCode error;

    public static ResponseProtocol createSuccess(String value){
        ResponseProtocol response = new ResponseProtocol();
        response.status = Status.SUCCESS;
        response.value = value;
        return response;
    }
    public static ResponseProtocol createError(ErrorCode error){
        ResponseProtocol response = new ResponseProtocol();
        response.status = Status.ERROR;
        response.error = error;
        return response;
    }

    public Status getStatus(){ return status; }
    public String getValue(){ return value; }
    public ErrorCode getErrorCode(){ return error;}
}
