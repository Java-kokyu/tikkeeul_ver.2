package com.hanghae0705.sbmoney.data;

import lombok.Builder;
import lombok.Data;

@Data
public class MessageWithNoData {
    private String msg;

    @Builder
    public MessageWithNoData(String msg) {
        this.msg = msg;
    }
}
