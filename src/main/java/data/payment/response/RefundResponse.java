package data.payment.response;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class RefundResponse {
    private int status;
    private String msg;
    private String reason;
}
