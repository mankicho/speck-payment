package service;

import data.PaymentInfo;
import data.payment.dto.PaymentRequestDTO;
import data.order.OrderInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import mapper.PaymentMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

@Component
@Log4j2
@RequiredArgsConstructor
public class PaymentRequestService {

    private final String REFUND_SERVER_URL = "https://api.iamport.kr/payments/cancel";
    private final String TOKEN_SERVER_URL = "https://api.iamport.kr/users/getToken";
    private final PaymentMapper paymentMapper;

    @Value("#{pay['key']}")
    private String key;

    @Value("#{pay['secret']}")
    private String secretKey;

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

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(con.getOutputStream()));
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

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(con.getOutputStream()));

            bw.write(object.toString());
            bw.flush();

            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line = br.readLine();

            return new JSONObject(line);

        } catch (IOException e) {
            // todo 1. 에러처리
            e.printStackTrace();
            return null;
        }
    }

    // 환불 요청
    public void refund(String mid) throws IOException{
        // todo 1. access token 발급
        JSONObject token = getAuthToken().getJSONObject("response");

        log.info(token);
        // todo 2. 결제정보 조회(이미 시작한 플랜인지 검사해야함.)
        PaymentInfo paymentInfo = paymentMapper.getPaymentInfo(mid);

        if(paymentInfo == null || paymentInfo.getImpUID() == null){
            // 없는정보임을 리턴
            return ;
        }
        log.info(paymentInfo);
        JSONObject parameterData = new JSONObject();
        parameterData.put("imp_uid",paymentInfo.getImpUID());
        parameterData.put("reason","출석체크 플랜 시작 전 취소");
        URL url = new URL(REFUND_SERVER_URL);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setDoOutput(true);
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Authorization", token.getString("access_token"));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(con.getOutputStream()));
        bw.write(parameterData.toString());
        bw.flush();

        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));

        String line = br.readLine();
        log.info(line);

    }

    private JSONObject userPaymentDtoToJSONObject(PaymentRequestDTO paymentRequestDTO) {
        JSONObject object = new JSONObject();
        object.put("name","테스트");
        object.put("customer_uid", paymentRequestDTO.getCustomerUID());
        object.put("merchant_uid", paymentRequestDTO.getMidUID());
        object.put("amount", paymentRequestDTO.getAmount());
        object.put("buyer_email", paymentRequestDTO.getBuyerEmail());
        return object;
    }

}