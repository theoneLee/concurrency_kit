import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;


/**
 * 基于AQS的一个简单实现，不可重入的独占锁
 */
public class BasicLock implements Lock,Serializable{

    private static class SyncAQS extends AbstractQueuedSynchronizer{

        Condition newCondition(){
            return new ConditionObject();
        }

        // 锁是否被持有
        protected boolean isHeldExclusively(){
            return getState() == 1;
        }

        //获取锁
        public boolean tryAcquire(int acquires){
            assert acquires == 1;
            if(compareAndSetState(0,1)){
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;
        }

        // 释放锁
        protected boolean tryRelease(int release){
            assert release == 1;
            if (getState()==0){
                throw new IllegalMonitorStateException();
            }
            setExclusiveOwnerThread(null);
            setState(0);
            return true;
        }


    }


    private final SyncAQS aqs = new SyncAQS();

    @Override
    public void lock() {
        aqs.acquire(1);
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        aqs.acquireInterruptibly(1);
    }

    @Override
    public boolean tryLock() {
        return aqs.tryAcquire(1);
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return aqs.tryAcquireNanos(1,unit.toNanos(time));
    }

    @Override
    public void unlock() {
        aqs.release(1);
    }

    @Override
    public Condition newCondition() {
        return aqs.newCondition();
    }




}
