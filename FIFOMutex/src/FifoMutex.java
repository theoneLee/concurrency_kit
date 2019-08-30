import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

public class FifoMutex {
    private final AtomicBoolean locked = new AtomicBoolean(false);
    private final Queue<Thread> waitersQ = new ConcurrentLinkedQueue<>();


    public void lock(){
        boolean wasInterrupted = false;
        Thread current = Thread.currentThread();

        waitersQ.add(current);

        // 队首线程可以获取锁,否则利用park挂起
        while (waitersQ.peek() != current || !locked.compareAndSet(false,true)){
            LockSupport.park(this);
            if (Thread.interrupted()){//park的阻塞因中断返回的处理，设置标记后重新判断是否需要调用park
                wasInterrupted = true;
            }

            waitersQ.remove();
            if (wasInterrupted){//如果被其他线程中断过，恢复中断
                current.interrupt();
            }
        }
    }

    public void unlock(){
        locked.set(false);
        LockSupport.unpark(waitersQ.peek());
    }
}
