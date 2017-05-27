package com.setvect.bokslphoto.test.temp.java8;

import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.junit.Test;

public class ParallelStameTestCase {
	@Test
	public void test() {
		Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).parallel().forEach(x -> {
			String a = Thread.currentThread().getName();
			System.out.println("111111111111111111111" + a);
			try {
				TimeUnit.MILLISECONDS.sleep(10);
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("222222222222222222222" + a);

			System.out.println(x);
		});

		System.out.println("끝.");
	}
}
