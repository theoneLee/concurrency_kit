package 取代stop方法来暴力中断的一种实现;


/**
 * 因为使用优雅方式去interrupt线程，在任务线程被长时间阻塞时（比如文件io）是无法去校验是否已经接受到中断信号然后去关掉任务线程的。
 * 这种情况可以采用类似于jdk 的stop方法（但是被弃用，因为不安全）
 * 而替代方案如下：借助一个与实际任务无关的线程a，然后将实际执行任务的线程作为他的守护线程，
 * 也就是即使守护线程被阻塞，但是a是不会被io阻塞的，所以当a被中断，作为守护线程也被中断
 *
 * 用法：在main函数里，创建一个实例，然后调用execute(传入实际要执行的长时间任务task)，再调用shutdown(传入任务可执行的最长时间t)
 * 即可实现：
 * 当t足够长，并且a和main没有被interrupt --》task成功执行完毕
 * 当t不够长（因为一些原因导致task执行时间超过预期，而这种情况下会一直阻塞掉该线程，而浪费资源） --》task因时间不够而执行失败，并且能够成功退出回收该线程
 * 当t足够长，并且a被interrupt --》task因a被中断而执行失败，并且能够成功退出回收该线程
 */
public class ThreadService {

    private Thread executeThread;//与实际任务无关的线程a

    private boolean finished = false;

    public void execute(Runnable task) {
        executeThread = new Thread() {
            @Override
            public void run() {
                Thread runner = new Thread(task);
                runner.setDaemon(true);

                runner.start();//实际执行任务，并等待线程执行完再设置finished标记
                try {
                    runner.join();
                    finished = true;
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                }
            }
        };

        executeThread.start();
    }

    public void shutdown(long mills) {
        long currentTime = System.currentTimeMillis();
        while (!finished) {
            if ((System.currentTimeMillis() - currentTime) >= mills) {
                System.out.println("任务超时，需要结束他!");
                executeThread.interrupt();
                break;
            }

//经测试属于无效代码，这部分并不能实现：当executeThread.interrupt()时，捕获该异常并让循环退出，导致循环一直在跑，即使实际任务完成了，也因循环在运作而要等到循环结束，才能让线程正常结束掉
            //可见interrupt不会直接结束掉线程。而是要等线程内没其他工作时才能正常退出线程。（保证安全退出）
//            try {
//                executeThread.sleep(1);//在sleep时被外部interrupt会抛出一个异常，然后只要捕获后跳出循环
//            } catch (InterruptedException e) {
//                System.out.println("执行线程executeThread被打断!");
//                break;
//            }
        }
        //跳出循环代表线程被中断或者执行任务的线程执行完毕，恢复finished标记
        finished = false;
    }

    public void shutdownForce() {
        System.out.println("手动interrupt执行线程executeThread");
        executeThread.interrupt();
        finished=true;//如果不将其置为true，线程内的shutdown仍有循环在跑，导致即使interrupt了也不能退出线程。
    }

}