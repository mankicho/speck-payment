package mapper;

import data.MemberCustomerId;
import data.order.dto.OrderDto;
import data.PaymentInfo;
import org.apache.ibatis.annotations.Param;

public interface PaymentMapper {
    MemberCustomerId getCustomerUID(@Param("email") String email);

    int registerCustomerUID(@Param("email") String email, @Param("customerId") String customerId);

    int getRealAmount(@Param("orderId") String midUID);

    int saveOrder(OrderDto orderDto);

    int savePayment(PaymentInfo paymentInfo);

    PaymentInfo getPaymentInfo(@Param("mid") String mid);
}
