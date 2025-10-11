package org.cage.eaglemq.common.event;

import lombok.extern.slf4j.Slf4j;
import org.cage.eaglemq.common.utils.ReflectUtils;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * ClassName: EventBus
 * PackageName: org.cage.eaglemq.common.event
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/11 上午1:06
 * @Version: 1.0
 */
@Slf4j
public class EventBus {
    Map<Class<? extends Event>, List<Listener>> eventListenerMap = new ConcurrentHashMap<>();

    private String taskName = "event-bus-task-";

    private ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            10,
            100,
            3, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(1000),
            r -> {
                Thread thread = new Thread(r);
                thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                    @Override
                    public void uncaughtException(Thread t, Throwable e) {
                        log.info("{}  执行过程出现问题：{}", taskName, e.getCause());
                    }
                });
                thread.setName(taskName + UUID.randomUUID().toString());
                return thread;
            }

    );


    public EventBus(String taskName) {
        this.taskName = taskName;
    }


    public void init() {
        ServiceLoader<Listener> serviceLoader = ServiceLoader.load(Listener.class);
        for (Listener listener : serviceLoader) {
            Class clazz = ReflectUtils.getInterfaceT(listener, 0);
            this.register(clazz, listener);
        }
    }

    public <E extends Event> void register(Class<? extends Event> event, Listener<E> listener) {
        List<Listener> listenerList = eventListenerMap.getOrDefault(event, new ArrayList<>());
        listenerList.add(listener);
        eventListenerMap.put(event, listenerList);
    }

    public void publish(Event event) throws Exception {
        List<Listener> listeners = eventListenerMap.get(event.getClass());
        threadPoolExecutor.execute(() -> {
            for (Listener listener : listeners) {
                try {
                    listener.onReceive(event);
                } catch (Exception e) {
                    log.error("任务执行过程抛出异常：{}", e.getMessage());
                }
            }
        });
    }
}
