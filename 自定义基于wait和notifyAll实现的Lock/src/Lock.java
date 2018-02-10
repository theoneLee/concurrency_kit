import java.util.Collection;

/**
 * lock就是对wait和notifyAll的封装。
 * 能够让业务代码减少代码（因为要保证线程安全而增加的代码）
 */
public interface Lock {

    class TimeOutException extends Exception {

        public TimeOutException(String message) {
            super(message);
        }
    }

    /**
     * 若有一个临界资源，多个线程都要去用他。
     * （重要）可以这样做：先拿到一个lock实例lockInstance，在多个线程要用这个临界资源前，lockInstance.lock()，即可让下面的操作串行化而实现线程安全，在使用完临界资源后，可以直接用lockInstance.unlock()，使下面的操作恢复并发；
     *
     *
     * 注意：某线程a调用的lock，只有等到线程a去调用unlock才能成功解锁，在其他线程unlock是无效操作
     * @throws InterruptedException
     */
    void lock() throws InterruptedException;

    /**
     * 和无参lock方法一样的功能，只是当时间超时，会直接抛出异常TimeOutException（当然这样就让该线程直接结束掉）
     * @param mills
     * @throws InterruptedException
     * @throws TimeOutException
     */
    void lock(long mills) throws InterruptedException, TimeOutException;

    /**
     * 解锁（只有对其加锁的线程才能使用该方法进行解锁）
     */
    void unlock();

    /**
     * 查看当前处于waiting队列的线程
     * @return
     */
    Collection<Thread> getBlockedThread();

    /**
     * 查看当前处于waiting队列的线程大小
     * @return
     */
    int getBlockedSize();

}