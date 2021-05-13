package data.order.dto;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@ToString
@Builder
public class OrderDto { // 고객의 주문정보를 담고있는 객체
    private String orderInfo; // 주문번호
    private String memberEmail; // 고객 이메일
    private int schoolId; // 어떤 갤럭시
    private int classId; //  어떤 탐험단
    private int totalPaymentAmount; // 총 결제금액
}
