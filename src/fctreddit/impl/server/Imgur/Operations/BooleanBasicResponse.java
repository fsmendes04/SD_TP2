package fctreddit.impl.server.Imgur.Operations;

public class BooleanBasicResponse {
    private boolean data;
    private int status;
    private boolean success;

    public boolean getData() {
        return data;
    }

    public int getStatus() {
        return status;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setData(boolean data) {
        this.data = data;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
