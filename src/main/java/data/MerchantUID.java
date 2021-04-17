package data;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MerchantUID { // 주문번호 ( rest controller 에서 json 형식으로 return 하기 위한 class )
    private String merchantUID;
}
