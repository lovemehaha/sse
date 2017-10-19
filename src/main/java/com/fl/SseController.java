package com.fl;


import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Controller
public class SseController {

    private Set<SseEmitter> sseEmitters = new HashSet<SseEmitter>();
    private int messageCount = 0;

    @RequestMapping(path="messages", method = RequestMethod.GET)
    public SseEmitter stream(HttpServletRequest request) throws Exception {
        final  SseEmitter sseEmitter = new SseEmitter((long)0);

        sseEmitter.onCompletion(() -> {
            synchronized (this.sseEmitters) {
                this.sseEmitters.remove(sseEmitter);
            }
        });

        sseEmitter.onTimeout(()-> {

            this.sseEmitters.remove(sseEmitter);
        });

        sseEmitters.add(sseEmitter);

        return sseEmitter;
    }
    @Scheduled(fixedDelay = 2000)
    public void scheduledMsgEmitter() throws IOException
    {
        if(!sseEmitters.isEmpty())
            ++messageCount;
        else
            System.out.println("No active Emitters ");

            sseEmitters.forEach(emitter -> {
                if (null != emitter)
                    try {
                        emitter.send("messagecount:"+messageCount);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            });
        }
}

