import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public class BooleanLock implements Lock {

    //当该值是true时，以为这该BooleanLock实例的this锁已被抢占
    //当该值是false时，以为这该BooleanLock实例的this锁没被抢占，其他线程可以来抢占他
    private volatile boolean initValue;

    private Collection<Thread> blockedThreadCollection = new ArrayList<>();//已被阻塞的Thread队列

    private Thread currentThread;//调用这个锁的当前线程实例

    public BooleanLock() {
        this.initValue = false;
    }

    /**
     * 实际上外部的线程a在调用lock完之后，该this锁已经被释放，但因为initValue是true，所以外部的其他线程因initValue都会调用wait方法（进入this锁的waiting队列）
     *
     * @throws InterruptedException
     */
    @Override
    public synchronized void lock() throws InterruptedException {
        while (initValue) {
            blockedThreadCollection.add(Thread.currentThread());
            this.wait();
        }

        blockedThreadCollection.remove(Thread.currentThread());
        this.initValue = true;
        this.currentThread = Thread.currentThread();
    }

    @Override
    public synchronized void lock(long mills) throws InterruptedException, TimeOutException {
        if (mills <= 0)
            lock();

        long hasRemaining = mills;
        long endTime = System.currentTimeMillis() + mills;
        while (initValue) {
            if (hasRemaining <= 0)
                throw new TimeOutException("Time out");
            if (!blockedThreadCollection.contains(Thread.currentThread())){
                blockedThreadCollection.add(Thread.currentThread());
            }
            this.wait(mills);
//            this.wait();
            hasRemaining = endTime - System.currentTimeMillis();

        }

        blockedThreadCollection.remove(Thread.currentThread());
        this.initValue = true;
        this.currentThread = Thread.currentThread();

    }

    @Override
    public synchronized void unlock() {
        if (Thread.currentThread() == currentThread) {
            this.initValue = false;
            Optional.of(Thread.currentThread().getName() + " release the lock monitor.")
                    .ifPresent(System.out::println);
            this.notifyAll();
        }
    }

    @Override
    public Collection<Thread> getBlockedThread() {
        return Collections.unmodifiableCollection(blockedThreadCollection);//不允许外部对其更改
    }

    @Override
    public int getBlockedSize() {
        return blockedThreadCollection.size();
    }
}

