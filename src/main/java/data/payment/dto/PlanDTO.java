package data.payment.dto;

import lombok.Data;

@Data
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
