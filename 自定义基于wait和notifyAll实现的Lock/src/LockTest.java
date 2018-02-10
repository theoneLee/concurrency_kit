import java.util.Optional;
import java.util.stream.Stream;

public class LockTest {
    public static void main(String[] args) throws InterruptedException {

        final BooleanLock booleanLock = new BooleanLock();
//        anotherTaskThead(booleanLock);
        Stream.of("T1", "T2", "T3", "T4")
                .forEach(name ->
                        new Thread(() -> {
                            try {
                                //booleanLock.lock();//线程抢占到monitor，对临界资源串行化操作，直到该线程执行完，才放弃该monitor
                                booleanLock.lock(3_000L);//线程抢占到monitor，对临界资源串行化操作，直到该线程执行完或时间结束，就放弃该monitor
                                Optional.of(Thread.currentThread().getName() + " have the lock Monitor")
                                        .ifPresent(System.out::println);
                                work();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (Lock.TimeOutException e) {
                                Optional.of(Thread.currentThread().getName() + " time out")
                                        .ifPresent(System.out::println);
                                //e.printStackTrace();
                            } finally {
                                booleanLock.unlock();//无论有无异常。线程调用结束后都要解锁，让其他线程可以来抢夺该锁
                            }
                        }, name).start()
                );
//        anotherTaskThead(booleanLock);
    }

    /**
     * 测试目的
     * @param booleanLock
     */
    private static void anotherTaskThead(BooleanLock booleanLock) {
        new Thread(()->{
            try {
                booleanLock.lock(7_500L);
                Optional.of(Thread.currentThread().getName() + " have the lock Monitor")
                        .ifPresent(System.out::println);
                work();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (Lock.TimeOutException e) {
                Optional.of(Thread.currentThread().getName() + " time out")
                        .ifPresent(System.out::println);
//                e.printStackTrace();
            }finally {
                booleanLock.unlock();//无论有无异常。线程调用结束后都要解锁，让其他线程可以来抢夺该锁
            }

        },"anotherThread").start();
    }

    /**
     * 实际工作
     * @throws InterruptedException
     */
    private static void work() throws InterruptedException {
        Optional.of(Thread.currentThread().getName() + " is Working...")
                .ifPresent(System.out::println);
        Thread.sleep(4_000);
    }
}