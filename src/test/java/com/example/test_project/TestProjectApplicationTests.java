package com.example.test_project;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
class TestProjectApplicationTests {

    @Autowired
    private StringRedisTemplate redisTemplate;

	@Test
	void contextLoads() {
	}

	@Test
	public void testRedis() {
        redisTemplate.opsForValue().set("hello", "world");
        String value = redisTemplate.opsForValue().get("hello");
        System.out.println("Redis 값 확인: " + value);
	}

}
