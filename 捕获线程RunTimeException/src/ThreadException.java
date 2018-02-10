/**
 * 捕获线程运行时异常（因为线程的run函数是不允许抛出异常的，只能直接捕获，而外部不可知）
 */
public class ThreadException {
    private final static int A = 10;
    private final static int B = 0;


    public static void main(String[] args) {

        Thread t = new Thread(() -> {
            try {
                Thread.sleep(5_000L);
                int result = A / B;
                System.out.println(result);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        t.setUncaughtExceptionHandler((thread, e) -> {
            System.out.println(e);
            System.out.println(thread);
        });
        t.start();
    }
}
