package org.wscp.model;

import io.micronaut.core.annotation.Introspected;

@Introspected
public enum TaskStatus {
    // Задача создана, но не отправлена на выполнение
    DRAFT("DRAFT"),

    // # Задача в процессе подготовки к выполнению
    CREATING("CREATING"),

    // # Задача в очереди на выполнение
    QUEUED("QUEUED"),

    //# Задача на выполнении
    RUNNING("RUNNING"),

    //  # Задача в процессе отмены
    ABORTING("ABORTING"),

    //  # Задача отменена
    ABORTED("ABORTED"),

    //  # Задача столкнулась с проблемой во время выполнения и не была успешно завершена
    FAILED("FAILED"),

    //  # Задача успешно завершена
    COMPLETED("COMPLETED"),

    //   # Статус задачи неизвестен
    UNKNOWN("UNKNOWN"),

    //  # Задача удалена
    DELETED("DELETED");

    private String taskStatus;

    TaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

}