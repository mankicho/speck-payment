package data.payment;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@Getter
@ToString
@Builder
public class PaymentMessage {
    private int status;
    private String paymentStatus;
    private String msg;
    private String impUID;
    private int amount;
}
