package com.bbytes.recruiz.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadUtils {

	public static ExecutorService newFixedThreadPool(String threadGroupName, int nThreads) {
		ExecutorService executorService = Executors.newFixedThreadPool(nThreads, new NameThreadFactory(threadGroupName));
		return executorService;

	}

	protected static class NameThreadFactory implements ThreadFactory {
		private final AtomicInteger threadNumber = new AtomicInteger(1);

		private String name;

		public NameThreadFactory(String name) {
			this.name = name;
		}

		public Thread newThread(Runnable r) {
			return new Thread(r, name + "-" + threadNumber.getAndIncrement());
		}
	}

}
