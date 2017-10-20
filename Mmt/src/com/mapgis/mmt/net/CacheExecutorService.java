package com.mapgis.mmt.net;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CacheExecutorService {
	  static short nThreads = 5;

	  public static final ExecutorService b = Executors.newFixedThreadPool(nThreads);
	  public static final ExecutorService c = Executors.newFixedThreadPool(nThreads);
}
