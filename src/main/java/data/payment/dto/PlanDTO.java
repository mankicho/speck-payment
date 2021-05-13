package data.payment.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Builder
public class PlanDTO {
    private int tid;
    private int schoolId;
    private int classId;
    private int setDay;
    private String memberEmail;
    private String startDate;
    private String endDate;
    private int cnt;
    private int setPaymentAmount;
}
