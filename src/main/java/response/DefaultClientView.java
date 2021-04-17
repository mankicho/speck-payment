package response;

import error.DefaultErrorCode;
import lombok.Data;

@Data
public class DefaultClientView {
    private int status;
    private String msg;

    public DefaultClientView(DefaultErrorCode code){
        this.status = code.getStatus();
        this.msg = code.getMsg();
    }
}
