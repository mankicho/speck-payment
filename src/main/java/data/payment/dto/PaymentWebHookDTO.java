package data.payment.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class PaymentWebHookDTO {
    private String imp_uid; // 결제 고유번호
    private String merchant_uid; // 주문 고유번호
    private String status; // 결제상태
}
