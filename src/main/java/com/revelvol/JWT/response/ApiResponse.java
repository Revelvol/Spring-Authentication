package com.revelvol.JWT.response;

import java.util.HashMap;
import java.util.Map;


public class ApiResponse {

    private int statusCode;
    private String message;
    private Map<String, Object> data ;

    //no args
    public ApiResponse(){

    }

    // with args
    public ApiResponse(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
        this.data = new HashMap<>();
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public void addData(String key, Object value){
        data.put(key,value);
    }

    public Object getData(String key) {
        return data.get(key);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ApiResponse{")
                .append("statusCode=").append(statusCode)
                .append(", message='").append(message).append('\'')
                .append(", data={");

        if (data != null) {
            boolean isFirst = true;
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                if (!isFirst) {
                    sb.append(", ");
                }
                sb.append(entry.getKey()).append("=").append(entry.getValue());
                isFirst = false;
            }
        }

        sb.append("}}");
        return sb.toString();
    }
}
