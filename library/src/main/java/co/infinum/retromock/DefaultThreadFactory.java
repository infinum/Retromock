package co.infinum.retromock;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

final class DefaultThreadFactory implements ThreadFactory {

    /**
     * Global counter for creating unique thread pool identifiers.
     */
  static final AtomicInteger POOL_INDEX = new AtomicInteger();

  @Override
  public Thread newThread(final Runnable runnable) {
    Thread thread = new Thread(runnable, "Retromock-" + POOL_INDEX.getAndIncrement() + "-thread");
    thread.setDaemon(true);
    return thread;
  }
}
