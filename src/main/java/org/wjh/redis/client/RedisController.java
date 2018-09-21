package org.wjh.redis.client;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class RedisController {

    @Autowired
    private RedisClient redisClient;
    public static int i = 0;

    @RequestMapping("/get.do")
    @ResponseBody
    public Object get() throws IOException {
        boolean lock = redisClient.acquire("wangjihui", 1000);
        if(lock) {
            i++;
            System.out.println(Thread.currentThread().getId() +" i=" + i);
            redisClient.release("wangjihui");
        }
        return null;
    }
}
