package org.wjh.redis.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

@Component
public class RedisClient {
    @Autowired
    private ShardedJedisPool jedisPool;

    private ShardedJedis getJedis() {
        return jedisPool.getResource();
    }

    public static byte[] serialize(Object obj) throws IOException {
        byte[] bytes = null;
        ByteArrayOutputStream baos = null;
        ObjectOutputStream oos = null;
        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            bytes = baos.toByteArray();
            baos.close();
            oos.close();
        } catch (IOException e) {
            return null;
        } finally {
            if (baos != null) {
                baos.close();
            }
            if (oos != null) {
                oos.close();
            }
        }
        return bytes;
    }

    public static Object deSerialize(byte[] bytes) throws IOException {
        Object obj = null;
        ByteArrayInputStream bais = null;
        ObjectInputStream ois = null;
        try {
            bais = new ByteArrayInputStream(bytes);
            ois = new ObjectInputStream(bais);
            obj = ois.readObject();
        } catch (Exception e) {
            return null;
        } finally {
            if (bais != null) {
                bais.close();
            }
            if (ois != null) {
                ois.close();
            }
        }
        return obj;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) throws IOException{
       byte[] bytes = getJedis().get(serialize(key));
       Object obj = deSerialize(bytes);
       return (T)obj;
    }

    public void set(String key, Object value, int expire) throws IOException {
        getJedis().setex(serialize(key), expire, serialize(value));
    }

    public void release(String key) {
        getJedis().del(key);
    }

    public boolean acquire(String key, long expire) {
        while(true) {
            long val = System.currentTimeMillis() + expire;
            if(getJedis().setnx(key, String.valueOf(val)) == 1) {
                System.out.println(Thread.currentThread().getId() +" get lock 0");
                return true;
            }
            String curOldValue = getJedis().get(key);
            if(curOldValue != null && Long.valueOf(curOldValue) < System.currentTimeMillis()) {
                String oldValue = getJedis().getSet(key, String.valueOf(val));
                if(oldValue != null && curOldValue.equals(oldValue)) {
                    System.out.println(Thread.currentThread().getId() +" get lock 1");
                    return true;
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
