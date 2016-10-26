# process-watcher
A Java framework that watches external processes for liveliness and executes arbitrary commands when they terminate.

## Usage
The most common use case is embedding the `process-watcher` into an application that should terminate itself when the an external process terminates.
```java
public static void main(String... args) {
  Thread t = new Thread(() -> {
    String pid = "61257";
    ProcessWatcher watcher = new ProcessWatcher();
    CountDownLatch signal = new CountDownLatch(1);
    watcher.watch(pid, () -> {
      System.out.println("The watched process has terminated");
      signal.countDown();
    });
  });
  t.setDaemon(true);
  t.start();
  // TODO: add other actions
  try {
    signal.await();
  }
  catch (InterruptedException e) {
    throw new RuntimeException(e);
  }
}
```
