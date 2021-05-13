package mapper;

import data.MemberCustomerId;
import data.order.dto.OrderDto;
import data.PaymentInfo;
import data.payment.dto.PaymentWebHookDTO;
import data.payment.dto.RefundDTO;
import data.payment.vo.RefundVO;
import org.apache.ibatis.annotations.Param;

public interface PaymentMapper {
    MemberCustomerId getCustomerUID(@Param("email") String email);

    int registerCustomerUID(@Param("email") String email, @Param("customerId") String customerId);

    int getRealAmount(@Param("orderId") String midUID);

    int saveOrder(OrderDto orderDto);

    int savePayment(PaymentInfo paymentInfo);

    RefundVO getPaymentInfo(RefundDTO refundDTO);

    int deletePayment(RefundDTO refundDTO);

    int insertWebHook(PaymentWebHookDTO paymentWebHookDTO);
}
