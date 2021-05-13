package controller;

import data.order.OrderInfo;
import data.order.dto.OrderDto;
import data.payment.PaymentMessage;
import data.payment.dto.PaymentRequestDTO;
import data.payment.dto.PaymentWebHookDTO;
import data.payment.dto.RefundDTO;
import data.payment.response.RefundResponse;
import error.DefaultErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import mapper.PaymentMapper;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.*;
import service.PaymentServiceWithIAmPortServer;
import service.PaymentService;
import response.DefaultClientView;

import java.io.IOException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

@RequestMapping(value = "/user")
@RestController
@RequiredArgsConstructor
@Log4j2
public class PaymentController {

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    private final PaymentService paymentService; // 결제 기능
    private final PaymentMapper paymentMapper; // DB 에 저장하기 위한 Mapper

    // DB 오류 시 클라이언트에게 리턴할 메세지
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    @ResponseBody // json 형식으로 return 하기위해
    public DefaultClientView handleSQLIntegrityConstraintViolationException(Exception e) {
        log.info(e.getMessage() + " occur!!");
        return new DefaultClientView(DefaultErrorCode.SQLIntegrityConstraintViolation); // SQL 실행도중 에러가 발생함을 알린다. JSON 형식으로
    }

    @PostMapping(value = "/payments/complete")
    public void paymentComplete(@RequestBody PaymentWebHookDTO paymentWebHookDTO) {
        paymentService.paymentComplete(paymentWebHookDTO);
    }

    // 정기결제 URL
    @PostMapping(value = "/regular/payments/do")
    public PaymentMessage doPayments(@RequestBody PaymentRequestDTO paymentRequestDTO) {
        return paymentService.doPayment(paymentRequestDTO);
    }

    // todo 1. 결제하기 전에 유저가 주문정보를 보내주면 주문정보 기본키를 생성해 유저에게 리턴
    // todo 2. 해당 주문정보로 나중에 결제 위변조 검증
    @PostMapping(value = "/save/order")
    public OrderDto saveOrder(@RequestBody OrderDto orderDto) {
        String id = orderDto.getMemberEmail();
        String orderId = generateOrderId(id);
        orderDto.setOrderInfo(orderId);

        int savedRow = paymentMapper.saveOrder(orderDto);

        if (savedRow == 1) {
            return orderDto;
        }

        return OrderDto.builder()
                .memberEmail(orderDto.getMemberEmail())
                .classId(orderDto.getClassId())
                .orderInfo("invalid")
                .build();
    }

    @PostMapping(value = "/refund")
    public RefundResponse refund(@RequestBody RefundDTO refundDTO) {
        return paymentService.refund(refundDTO);
    }

    private String generateOrderId(String email) {
        String now = simpleDateFormat.format(new Date());
        if (!email.contains("@")) {
            return email + "_" + now;
        }

        return email.split("@")[0] + "_" + now;
    }
}
