package version2;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SimpleThreadPool {

    private int size;//todo 线程池大小

    private final int queueSize;//todo TASK_QUEUE 允许大小

    private final static int DEFAULT_TASK_QUEUE_SIZE = 2000;

    private final static int DEFAULT_SIZE=10;

    private static volatile int seq = 0;

    private final static String THREAD_PREFIX = "SIMPLE_THREAD_POOL-";

    private final static ThreadGroup GROUP = new ThreadGroup("Pool_Group");

    private final static LinkedList<Runnable> TASK_QUEUE = new LinkedList<>();//todo 任务队列

    private final static List<WorkerTask> THREAD_QUEUE = new ArrayList<>();//todo 可复用线程队列（下称WorkerTask队列）

    private final DiscardPolicy discardPolicy;// todo 拒绝策略

    public final static DiscardPolicy DEFAULT_DISCARD_POLICY = () -> {
        throw new DiscardException("Discard This Task.");
    };

    private volatile boolean destroy = false;

    public SimpleThreadPool() {
        this(DEFAULT_SIZE,DEFAULT_TASK_QUEUE_SIZE,DEFAULT_DISCARD_POLICY);
    }

    public SimpleThreadPool(int size,int queueSize,DiscardPolicy discardPolicy) {
        this.size=size;
        this.queueSize=queueSize;
        this.discardPolicy=discardPolicy;
        init();//todo 初始化 WorkerTask队列
    }

    private void init() {
        for (int i = 0; i < size; i++) {
            createWorkTask();
        }
    }

    public void submit(Runnable runnable) {
        if (destroy)
            throw new IllegalStateException("The thread pool already destroy and not allow submit task.");
        synchronized (TASK_QUEUE) {
            if (TASK_QUEUE.size() > queueSize)
                discardPolicy.discard();
            TASK_QUEUE.addLast(runnable);
            TASK_QUEUE.notifyAll();
        }
    }

    public void shutdown() throws InterruptedException {//todo 关闭线程池（只有线程池处于没有任务执行时才能关闭）
        while (!TASK_QUEUE.isEmpty()) {
            Thread.sleep(50);
        }

        synchronized (THREAD_QUEUE) {//当TASK_QUEUE没有任务，WorkerTask有两种情况：1是处于waiting（TaskState.BLOCKED），2是处于刚执行完任务处于TaskState.FREE。对于前者，只要调用interrupt并置为DEAD状态，在跳回OUTER时会不执行任何代码跳出（即该线程自然死亡）；对于后者，只要等待其重新进入BLOCKED状态即可
            int initVal = THREAD_QUEUE.size();
            while (initVal > 0) {
                for (WorkerTask task : THREAD_QUEUE) {
                    if (task.getTaskState() == TaskState.BLOCKED) {
                        task.interrupt();
                        task.close();
                        initVal--;
                    } else {
                        Thread.sleep(10);
                    }
                }
            }
        }

        System.out.println(GROUP.activeCount());

        this.destroy = true;
        System.out.println("The thread pool disposed.");
    }


    private void createWorkTask() {
        WorkerTask task = new WorkerTask(GROUP, THREAD_PREFIX + (seq++));
        task.start();
        THREAD_QUEUE.add(task);
    }


    private enum TaskState {
        FREE, RUNNING, BLOCKED, DEAD
    }

    private static class WorkerTask extends Thread {//todo 可复用线程内部类

        private volatile TaskState taskState = TaskState.FREE;//该线程状态

        public WorkerTask(ThreadGroup group, String name) {
            super(group, name);
        }

        public TaskState getTaskState() {
            return this.taskState;
        }

        @Override
        public void run() {//
            OUTER:
            while (this.taskState != TaskState.DEAD) {
                Runnable runnable;
                synchronized (TASK_QUEUE) {
                    while (TASK_QUEUE.isEmpty()) {
                        try {
                            taskState = TaskState.BLOCKED;
                            TASK_QUEUE.wait();
                        } catch (InterruptedException e) {
                            System.out.println("Closed.");
                            break OUTER;
                        }
                    }
                    runnable = TASK_QUEUE.removeFirst();//FIFO的机制（即以公平的方式实现先进到TASK_QUEUE的任务，先得到执行；由两个TASK_QUEUE的synchronized来保证这个实现）
                    //this place 1
                }

                if (runnable != null) {//注意这部分不能写到同步块里面（this place 1），否则影响submit方法拿monitor会被任务的执行给阻塞
                    taskState = TaskState.RUNNING;
                    runnable.run();
                    taskState = TaskState.FREE;
                }
            }
        }

        public void close() {
            this.taskState = TaskState.DEAD;
        }
    }

    public boolean isDestroy() {
        return this.destroy;
    }

    public static class DiscardException extends RuntimeException {
        public DiscardException(String message) {
            super(message);
        }
    }

    public interface DiscardPolicy {
        void discard() throws DiscardException;
    }
}
