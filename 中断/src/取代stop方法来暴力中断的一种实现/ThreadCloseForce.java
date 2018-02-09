package 取代stop方法来暴力中断的一种实现;

public class ThreadCloseForce {


    public static void main(String[] args) {

        ThreadService service = new ThreadService();
        long start = System.currentTimeMillis();
        service.execute(() -> {
            //load a very heavy resource.
            /*while (true) {

            }*/
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        //fun(service);//一定要放在调用shutdown之前
        service.shutdown(1000);//可改为10000去测试任务正常执行完退出的情况
        long end = System.currentTimeMillis();
        System.out.println(end - start);
    }

    private static void fun(ThreadService service) {//模拟外部打断ThreadService的执行线程executeThread
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        new Thread(()->{
            service.shutdownForce();
        }).start();
    }


}
