package service;

import data.PaymentInfo;
import data.payment.dto.PaymentRequestDTO;
import data.payment.PaymentMessage;
import data.payment.dto.PlanDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import mapper.PaymentMapper;
import mapper.SchoolMapper;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Log4j2
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentMapper paymentMapper;
    private final SchoolMapper schoolMapper;
    private final PaymentRequestService paymentRequestService;

    public PaymentMessage doPayment(PaymentRequestDTO paymentRequestDTO) {
        // todo 1. access_token 가져오기 ==> 실패 시 서버오류 메세지 리턴
        JSONObject authToken = paymentRequestService.getAuthToken().getJSONObject("response"); // 인증토큰 요청

        if (authToken == null) {
            // 인증토큰을 가져오지못하면
            return PaymentMessage.builder()
                    .status(501)
                    .msg("인증토큰을 가져오지 못했습니다")
                    .build();
        }

        log.info(authToken);
        log.info(paymentRequestDTO);

        JSONObject paymentResultObject = paymentRequestService.
                doPaymentWithCustomerUID(paymentRequestDTO, authToken.getString("access_token")); // 결제요청

        if (paymentResultObject == null) {
            return PaymentMessage.builder()
                    .status(500)
                    .msg("카드사 요청 실패")
                    .build();
        }

        if (paymentResultObject.get("code") != null && paymentResultObject.getInt("code") == 0) { // 카드사 통신 성공
            JSONObject responseJSON = paymentResultObject.getJSONObject("response"); //결제요청 중 response 값

            if (responseJSON.getString("status").equals("paid")) {

                // todo 5. 유저의 플랜 예약정보를 DB 에 저장.
                schoolMapper.insertPlan(paymentRequestDTO.getPlanDTO());
                log.info(paymentRequestDTO.getPlanDTO());
                // todo 6. DB 에 결제정보 저장
                String payImpUID = responseJSON.getString("imp_uid"); // 결제 고유식별자
                String customerUID = responseJSON.getString("customer_uid"); // 고객 빌링키 1:1 대응하는 고유 식별자
                String merchantUID = responseJSON.getString("merchant_uid"); // 주문정보 고유 식별자

                PaymentInfo paymentInfo = PaymentInfo.builder()
                        .customerUID(customerUID)
                        .merchantUID(merchantUID)
                        .impUID(payImpUID)
                        .amount(paymentRequestDTO.getAmount())
                        .tid(paymentRequestDTO.getPlanDTO().getTid())
                        .build();
                int savedRow = paymentMapper.savePayment(paymentInfo);

                if(savedRow == 0){
                    // todo. 실패기록을 로그파일로 남기자.
                }

                return PaymentMessage.builder()
                        .status(paymentResultObject.getInt("code"))
                        .paymentStatus(responseJSON.getString("status"))
                        .impUID(responseJSON.getString("imp_uid"))
                        .amount(responseJSON.getInt("amount"))
                        .msg("성공적으로 결제했습니다.")
                        .build();
            } else {
                return PaymentMessage.builder()
                        .status(paymentResultObject.getInt("code"))
                        .paymentStatus(responseJSON.getString("status"))
                        .msg(responseJSON.getString("fail_reason"))
                        .amount(-1)
                        .build();
            }
        } else {
            return PaymentMessage.builder()
                    .status(paymentResultObject.getInt("code"))  // 카드사 통신에 실패했을때
                    .amount(-1)
                    .msg(paymentResultObject.getString("message"))
                    .build();
        }

        // todo 1. 분석필요

    }

    public void refund() {

    }
}
