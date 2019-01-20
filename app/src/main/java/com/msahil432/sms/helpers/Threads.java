package com.msahil432.sms.helpers;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Threads {
  public static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
  public static Executor WorkThread = Executors.newFixedThreadPool(NUMBER_OF_CORES-3);
  public static Executor DownloadThread = Executors.newFixedThreadPool(2);
  public static Executor DbThread = Executors.newSingleThreadExecutor();
}
