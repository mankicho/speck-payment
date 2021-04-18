package data.payment.dto;

import lombok.Data;

@Data
public class RefundDTO {
    private String impUID; // 결제번호
    private String midUID; // 주문번호
}
