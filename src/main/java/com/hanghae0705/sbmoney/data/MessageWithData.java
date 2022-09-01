package com.hanghae0705.sbmoney.data;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class MessageWithData {
    private String msg;
    private Object data;

    @Builder
    public MessageWithData(String msg, Object data) {
        this.msg = msg;
        this.data = data;
    }
}
