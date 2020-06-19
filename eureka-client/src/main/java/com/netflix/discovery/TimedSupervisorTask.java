package com.netflix.discovery;

import java.util.TimerTask;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import com.netflix.servo.monitor.Counter;
import com.netflix.servo.monitor.LongGauge;
import com.netflix.servo.monitor.MonitorConfig;
import com.netflix.servo.monitor.Monitors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO: 它是一个固定间的周期性任务，一旦遇到了timeout超时就会将下一次任务的执行时间翻倍，如果继续超时就继续翻倍，知道达到设定的上限为止，达到上限就是固定时间间隔了
 * A supervisor task that schedules subtasks while enforce a timeout.
 * Wrapped subtasks must be thread safe.
 *
 * @author David Qiang Liu
 */
public class TimedSupervisorTask extends TimerTask {
    private static final Logger logger = LoggerFactory.getLogger(TimedSupervisorTask.class);

    private final Counter successCounter;
    private final Counter timeoutCounter;
    private final Counter rejectedCounter;
    private final Counter throwableCounter;
    private final LongGauge threadPoolLevelGauge;

    private final String name;
    private final ScheduledExecutorService scheduler;
    private final ThreadPoolExecutor executor;
    private final long timeoutMillis;
    private final Runnable task;

    private final AtomicLong delay;
    private final long maxDelay;

    public TimedSupervisorTask(String name, ScheduledExecutorService scheduler, ThreadPoolExecutor executor,
                               int timeout, TimeUnit timeUnit, int expBackOffBound, Runnable task) {
        this.name = name;
        this.scheduler = scheduler;
        this.executor = executor;
        this.timeoutMillis = timeUnit.toMillis(timeout);
        this.task = task;
        this.delay = new AtomicLong(timeoutMillis);
        this.maxDelay = timeoutMillis * expBackOffBound;

        // Initialize the counters and register.
        successCounter = Monitors.newCounter("success");
        timeoutCounter = Monitors.newCounter("timeouts");
        rejectedCounter = Monitors.newCounter("rejectedExecutions");
        throwableCounter = Monitors.newCounter("throwables");
        threadPoolLevelGauge = new LongGauge(MonitorConfig.builder("threadPoolUsed").build());
        Monitors.registerObject(name, this);
    }

    @Override
    public void run() {
        Future<?> future = null;
        try {
            // TODO: 提交目标任务，开始执行，使用future进行跟踪
            future = executor.submit(task);
            threadPoolLevelGauge.set((long) executor.getActiveCount());
            // TODO: 立马获取结果，超时时间是timeoutMills,默认是30s超时，此方法会阻塞，直到超时，或者正常执行完成
            future.get(timeoutMillis, TimeUnit.MILLISECONDS);  // block until done or timeout
            // TODO: 若正常执行完成，调用set方法重新设置delay的值为初始值
            delay.set(timeoutMillis);
            threadPoolLevelGauge.set((long) executor.getActiveCount());
            successCounter.increment();
        } catch (TimeoutException e) {
            logger.warn("task supervisor timed out", e);
            timeoutCounter.increment();
            // TODO: 拿到当前的delay时间
            long currentDelay = delay.get();
            // TODO: 在当前的delay时间基础上翻倍，当然不能超过最大值，最大值默认翻10倍
            long newDelay = Math.min(maxDelay, currentDelay * 2);
            // TODO: 以线程安全的方式把新值放上去
            delay.compareAndSet(currentDelay, newDelay);
        // 线程池拒绝异常，线程池关闭或者任务积压太多了，输出日志
        } catch (RejectedExecutionException e) {
            if (executor.isShutdown() || scheduler.isShutdown()) {
                logger.warn("task supervisor shutting down, reject the task", e);
            } else {
                logger.warn("task supervisor rejected the task", e);
            }

            rejectedCounter.increment();
            // TODO: 其他异常，输出日志
        } catch (Throwable e) {
            if (executor.isShutdown() || scheduler.isShutdown()) {
                logger.warn("task supervisor shutting down, can't accept the task");
            } else {
                logger.warn("task supervisor threw an exception", e);
            }

            throwableCounter.increment();
            // TODO: 重点=========
        } finally {
            // TODO: 要么执行完毕，要么发生异常，都用cancel方法来清理任务，因为要重新开启一个任务，最好显示调用来回收资源
            if (future != null) {
                future.cancel(true);
            }
            // TODO: 只要调度器没有停止，就再指定等待时间之后再执行一次同样的任务
            // TODO: 此处是任务具有周期性的根本原因，上个任务执行完成后，立马开启下一个延迟任务
            if (!scheduler.isShutdown()) {
                scheduler.schedule(this, delay.get(), TimeUnit.MILLISECONDS);
            }
        }
    }

    @Override
    public boolean cancel() {
        Monitors.unregisterObject(name, this);
        return super.cancel();
    }
}