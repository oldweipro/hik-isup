package com.oldwei.isup.model.tts;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
public class DataItem implements Serializable {
    @Serial
    private static final long serialVersionUID = -3572278952302040034L;
    private String desc;
    private String text;
    private String voice;
    private String volume;
}
