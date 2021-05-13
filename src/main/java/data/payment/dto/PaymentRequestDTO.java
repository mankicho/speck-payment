package data.payment.dto;

import lombok.Data;

@Data
public class PaymentRequestDTO { // 결제 요청 정보
    private String nickname;
    private String buyerEmail;
    private String midUID; // 주문번호
    private String customerUID;
    private int amount;
}
