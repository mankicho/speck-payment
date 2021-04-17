package error;

public enum DefaultErrorCode {
    SQLIntegrityConstraintViolation(500,"sql statement or constraint error");

    private int status;
    private String msg;

    DefaultErrorCode(int status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    public int getStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }
}
