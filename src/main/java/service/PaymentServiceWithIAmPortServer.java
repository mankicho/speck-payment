package service;

import data.PaymentInfo;
import data.payment.dto.PaymentRequestDTO;
import data.order.OrderInfo;
import data.payment.dto.RefundDTO;
import data.payment.response.RefundResponse;
import data.payment.vo.RefundVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import mapper.PaymentMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.sql.Connection;
import java.time.LocalDateTime;

@Component
@Log4j2
@RequiredArgsConstructor
public class PaymentServiceWithIAmPortServer {

    private final String REFUND_SERVER_URL = "https://api.iamport.kr/payments/cancel";
    private final String TOKEN_SERVER_URL = "https://api.iamport.kr/users/getToken";
    private final PaymentMapper paymentMapper;

    private final FileService fileService;

    @Value("#{pay['key']}")
    private String key;

    @Value("#{pay['secret']}")
    private String secretKey;


    // 인증정보 요청하기
    public JSONObject getAuthToken() {
        JSONObject restAPIKey = new JSONObject();
        restAPIKey.put("imp_key", key);
        restAPIKey.put("imp_secret", secretKey);
        try {
            URL url = new URL(TOKEN_SERVER_URL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestProperty("Content-Type", "application/json"); // 헤더속성
            con.setDoOutput(true); // data 를 stream 에 '쓰기'
            con.setRequestMethod("POST"); // 포스트 방식

            BufferedWriter bw = getBufferedWriter(con);
            bw.write(restAPIKey.toString());
            bw.flush(); // restAPIKey 와 함께 요청

            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));

            String line = br.readLine();


            return new JSONObject(line);

        } catch (Exception e) {
            // todo 1. 적절한 에러처리 할 것
            e.printStackTrace();
            return null;
        }
    }

    // 주문정보 조회
    public JSONObject checkOrder(OrderInfo orderInfo) {
        try {
            URL url = new URL("https://api.iamport.kr/payments/" + orderInfo.getImpUID());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setDoOutput(true);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", orderInfo.getAccessToken());

            OutputStream os = connection.getOutputStream();
            os.flush();

            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line = br.readLine();

            log.info(line);

            return new JSONObject(line);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 결제요청
    public JSONObject doPaymentWithCustomerUID(PaymentRequestDTO paymentRequestDTO, String accessToken) {
        try {
            JSONObject object = userPaymentDtoToJSONObject(paymentRequestDTO);
            URL url = new URL("https://api.iamport.kr/subscribe/payments/again"); // customerUID 로 결제하기
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Authorization", accessToken);

            BufferedWriter bw = getBufferedWriter(con);

            bw.write(object.toString());
            bw.flush();

            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line = br.readLine();

            return new JSONObject(line);

        } catch (IOException e) {
            // todo 1. 에러처리
            log.info("결제서버 통신 오류 " + e.getMessage());
            return null;
        }
    }

    // 환불 요청
    public RefundResponse refund(RefundDTO refundDTO) {
        // todo 1. access token 발급
        JSONObject token = getAuthToken().getJSONObject("response");

        log.info("refundDTO = " + refundDTO);
        // todo 2. 결제정보 조회(이미 시작한 플랜인지 검사해야함.)
        RefundVO refundVO = paymentMapper.getPaymentInfo(refundDTO);

        if (refundVO == null) { // DB 에 해당 결제정보와 주문정보 데이터가 없으면
            return RefundResponse.builder()
                    .status(500)
                    .msg("환불 요청 실패")
                    .reason("존재하지 않는 결제 정보입니다.")
                    .build();
        }
        if (!refundVO.isBefore()) { // 이미 시작한 플랜이라면 (오늘 날짜가 플랜 시작날짜보다 같거나 나중이라면)
            return RefundResponse.builder()
                    .status(400)
                    .msg("환불 요청 실패")
                    .reason("이미 시작한 출석체크 계획은 환불할 수 없습니다.")
                    .build();
        }

        JSONObject parameterData = new JSONObject();
        parameterData.put("imp_uid", refundVO.getImpUID());
        parameterData.put("reason", "출석체크 시작 전 결제 취소"); // 환불 데이터 생성

        try {
            URL url = new URL(REFUND_SERVER_URL); // 환불 서버 URL
            HttpURLConnection con = (HttpURLConnection) url.openConnection(); // 서버 통신을 위한 connection
            con.setDoOutput(true); // 데이터 쓰기모드
            con.setRequestMethod("POST"); // POST
            con.setRequestProperty("Content-Type", "application/json"); // 전송데이터가 JSON 임을 명시
            con.setRequestProperty("Authorization", token.getString("access_token")); // 액세스 토큰
            BufferedWriter bw = getBufferedWriter(con); // 버퍼 롸이터 생성

            bw.write(parameterData.toString());
            bw.flush();

            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));

            String line = br.readLine();

            JSONObject refundResult = new JSONObject(line); // 통신 결과를 JSON 으로 생성

            if (refundResult.getInt("code") == 0) { // 환불처리가 정상적으로 되면
                log.info(LocalDateTime.now() + " : success refund " + refundResult.toString());
                int deleteRow = paymentMapper.deletePayment(refundDTO); // DB 에서 해당 결제정보를 삭제한다

                if (deleteRow == 0) {
                    // todo. 파일로 로그 남김.
                    fileService.writeError("환불데이터 " + refundDTO + " DB 삭제 실패");
                }
                String reason = refundResult.getJSONObject("response").getString("cancel_reason") != null ?
                        refundResult.getJSONObject("response").getString("cancel_reason") : "출석체크 시작 전 결제 환불";

                return RefundResponse.builder()
                        .status(refundResult.getInt("code"))
                        .msg("환불 요청 성공")
                        .reason(reason)
                        .build();
            } else {
                return RefundResponse.builder()
                        .status(refundResult.getInt("code"))
                        .msg("환불 요청 실패")
                        .reason(refundResult.getString("message"))
                        .build();

            }

        } catch (IOException e) {
            return RefundResponse.builder()
                    .status(500)
                    .msg("환불 요청 실패")
                    .reason("서버 에러")
                    .build();
        }
    }

    private JSONObject userPaymentDtoToJSONObject(PaymentRequestDTO paymentRequestDTO) {
        JSONObject object = new JSONObject();
        object.put("name", "테스트");
        object.put("customer_uid", paymentRequestDTO.getCustomerUID());
        object.put("merchant_uid", paymentRequestDTO.getMidUID());
        object.put("amount", paymentRequestDTO.getAmount());
        object.put("buyer_email", paymentRequestDTO.getBuyerEmail());
        return object;
    }

    private BufferedWriter getBufferedWriter(HttpURLConnection con) throws IOException {
        return new BufferedWriter(new OutputStreamWriter(con.getOutputStream()));
    }
}