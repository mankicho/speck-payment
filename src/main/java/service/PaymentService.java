package service;

import data.PaymentInfo;
import data.order.OrderInfo;
import data.payment.dto.PaymentRequestDTO;
import data.payment.PaymentMessage;
import data.payment.dto.PaymentWebHookDTO;
import data.payment.dto.PlanDTO;
import data.payment.dto.RefundDTO;
import data.payment.response.RefundResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import mapper.PaymentMapper;
import mapper.SchoolMapper;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@Log4j2
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentServiceWithIAmPortServer paymentServiceWithIAmPortServer;
    private final PaymentMapper paymentMapper;
    private final SchoolMapper schoolMapper;
    private final FileService fileService;

    public PaymentMessage doPayment(PaymentRequestDTO paymentRequestDTO) {
        return paymentServiceWithIAmPortServer.doPayment(paymentRequestDTO);
    }

    public RefundResponse refund(RefundDTO refundDTO) {
        return paymentServiceWithIAmPortServer.refund(refundDTO);
    }

    public JSONObject checkOrder(String impUID) {
        return paymentServiceWithIAmPortServer.checkOrder(impUID);
    }

    public void paymentComplete(PaymentWebHookDTO paymentWebHookDTO) {
        // todo 1. 웹훅 로그 DB 에 저장
        paymentMapper.insertWebHook(paymentWebHookDTO);

        switch (paymentWebHookDTO.getStatus()) {
            case "paid": // 결제 승인
                // todo 1-1. 결제정보 조회
                // 웹훅에서 결제로그를 가져와서 주문정보 조회
                JSONObject checkedObject = paymentServiceWithIAmPortServer.checkOrder(paymentWebHookDTO.getImp_uid());

                JSONObject checkedObjectResponse = checkedObject.getJSONObject("response");
                if (checkedObjectResponse.get("custom_data") != null) { // todo. 이게 null 일 가능성이 있나??
                    // 클라이언트가 요청할때 넣은 body 데이터 조회
                    JSONObject customDataObject = new JSONObject(checkedObjectResponse.getString("custom_data"));
                    log.info(customDataObject);
                    log.info(LocalDateTime.now() + " : " + checkedObjectResponse.toString());

                    // body 데이터를 이용해 유저가 예약한 예약정보 DB 에 저장
                    int tid = insertPlanInfo(customDataObject);
                    // todo 1-2. 사용자데이터 DB에 저장.
                    PaymentInfo paymentInfo = PaymentInfo.builder()
                            .tid(tid)
                            .amount(checkedObjectResponse.getInt("amount"))
                            .impUID(paymentWebHookDTO.getImp_uid())
                            .merchantUID(paymentWebHookDTO.getMerchant_uid())
                            .customerUID(checkedObjectResponse.getString("customer_uid"))
                            .build();

                    int insertedRow = paymentMapper.savePayment(paymentInfo);

                    if (insertedRow == 0) {
                        // todo . 결제는 됐는데 사용자 예약정보가 저장이안되면? 로그파일에 로깅
                        fileService.writeError(LocalDateTime.now() + " : " + paymentInfo +" data missing");
                    }

                }
                break;
        }
    }

    private int insertPlanInfo(JSONObject customData) {
        PlanDTO planDTO = PlanDTO.builder()
                .schoolId(customData.getInt("schoolId"))
                .classId(customData.getInt("classId"))
                .cnt(customData.getInt("cnt"))
                .startDate(customData.getString("startDate"))
                .endDate(customData.getString("endDate"))
                .memberEmail(customData.getString("memberEmail"))
                .setDay(customData.getInt("setDay"))
                .setPaymentAmount(customData.getInt("setPaymentAmount"))
                .build();

        schoolMapper.insertPlan(planDTO);

        return planDTO.getTid();
    }
}
