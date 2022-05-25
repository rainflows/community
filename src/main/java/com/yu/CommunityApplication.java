package com.yu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

/**
 * 社区应用程序
 *
 * @author yu
 * @date 2022/05/09
 */
@SpringBootApplication
public class CommunityApplication {

	@PostConstruct
	public void init(){
		// 解决netty启动冲突的问题
		System.setProperty("es.set.netty.runtime.available.processors", "false");
	}

	public static void main(String[] args) {
		SpringApplication.run(CommunityApplication.class, args);
	}

}
