package data.payment.vo;

import lombok.Data;

@Data
public class RefundVO {
    private int paymentId; // 결제번호
    private String impUID; // 고유 결제번호
    private String midUID; // 고유 주문번호
    private int amount; // 금액
    private boolean isBefore; // 플랜이 시작하기 전인가?
}
