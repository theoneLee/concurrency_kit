package 以优雅的方式来实现中断但有其缺陷之处;

public class ThreadCloseGraceful2 {
    private static class Worker extends Thread {

        @Override
        public void run() {
            while (true) {
                if (Thread.interrupted())
                    break;
            }
            //-------------
            //-------------
            //-------------
        }
    }

    public static void main(String[] args) {
        Worker worker = new Worker();
        worker.start();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        worker.interrupt();
    }
}
