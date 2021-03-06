package 利用join实现简单的线程池;

import java.util.*;

/**
 * 任务很多（远大于可执行线程的情况下），线程的复用和调度
 * 一个简单的线程池实现
 */
public class CaptureService {

    final static private LinkedList<Control> CONTROLS = new LinkedList<>();
    private final static int MAX_WORKER = 5;

    public static void main(String[] args) {

        List<Thread> worker = new ArrayList<>();
        Arrays.asList("M1", "M2", "M3", "M4", "M5", "M6", "M7", "M8", "M9", "M10").stream()
                .map(CaptureService::createCaptureThread)//函数推导，将字符串队列转化为Thread队列
                .forEach(t -> {
                    t.start();
                    worker.add(t);
                });

        worker.stream().forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        Optional.of("All of capture work finished").ifPresent(System.out::println);
    }


    private static Thread createCaptureThread(String name) {
        return new Thread(() -> {
            Optional.of("The worker [" + Thread.currentThread().getName() + "] BEGIN capture data.")
                    .ifPresent(System.out::println);
            synchronized (CONTROLS) {//若超过可执行任务数量，就让其进入waiting状态；否则就加入任务队列
                while (CONTROLS.size() > MAX_WORKER) {
                    try {
                        CONTROLS.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                CONTROLS.addLast(new Control());
            }


            Optional.of("The worker [" + Thread.currentThread().getName() + "] is working...")
                    .ifPresent(System.out::println);
            try {
                Thread.sleep(10000);//todo 模拟执行任务
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            synchronized (CONTROLS) {
                Optional.of("The worker [" + Thread.currentThread().getName() + "] END capture data.")
                        .ifPresent(System.out::println);
                CONTROLS.removeFirst();
                CONTROLS.notifyAll();
            }
        }, name);
    }

    private static class Control {
    }
}
