package fctreddit.impl.server.Imgur.Utils;

import java.util.Map;

public class BasicResponse<T> {
    private Map<?, ?> data;
    private int status;
    private boolean success;

    public Map<?, ?> getData() {
        return data;
    }

    public int getStatus() {
        return status;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setData(Map<?, ?> data) {
        this.data = data;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
