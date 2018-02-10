package version1;

public class Test {
    public static void main(String[] args) {
        SimpleThreadPool threadPool = new SimpleThreadPool();
        for (int i = 0; i < 40; i++) {
            threadPool.submit(() -> {
                System.out.println("The runnable  be serviced by " + Thread.currentThread() + " start.");
                try {
                    Thread.sleep(10_000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("The runnable be serviced by " + Thread.currentThread() + " finished.");
            });
        }
        //注意这个实现，当所有任务都执行完时，main函数不会退出是正常的，因为线程池还有活跃的线程，要有一个关闭线程池的接口调用后才可以退出
    }
}
