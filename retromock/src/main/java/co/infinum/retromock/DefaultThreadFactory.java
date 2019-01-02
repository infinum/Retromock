package co.infinum.retromock;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

final class DefaultThreadFactory implements ThreadFactory {

  static final AtomicInteger POOL_INDEX = new AtomicInteger();

  @Override
  public Thread newThread(final Runnable r) {
    Thread thread = new Thread(r, "Retromock-" + POOL_INDEX.getAndIncrement() + "-thread");
    thread.setDaemon(true);
    return thread;
  }
}
