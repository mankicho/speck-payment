package service;

import data.PaymentInfo;
import data.payment.dto.PaymentRequestDTO;
import data.payment.PaymentMessage;
import data.payment.dto.RefundDTO;
import data.payment.response.RefundResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import mapper.PaymentMapper;
import mapper.SchoolMapper;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentServiceWithIAmPortServer paymentServiceWithIAmPortServer;

    public PaymentMessage doPayment(PaymentRequestDTO paymentRequestDTO) {
        return paymentServiceWithIAmPortServer.doPayment(paymentRequestDTO);
    }

    public RefundResponse refund(RefundDTO refundDTO) {
        return paymentServiceWithIAmPortServer.refund(refundDTO);
    }
}
