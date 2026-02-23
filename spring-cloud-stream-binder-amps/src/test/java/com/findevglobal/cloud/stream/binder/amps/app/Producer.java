package com.findevglobal.cloud.stream.binder.amps.app;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface Producer {

    String OUTPUT1 = "output1";

    String OUTPUT2 = "output2";

    String OUTPUT3 = "output3";

    @Output(OUTPUT1)
    MessageChannel output1();

    @Output(OUTPUT2)
    MessageChannel output2();

    @Output(OUTPUT3)
    MessageChannel output3();
}
