package org.wscp.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Task {
    // Поля класса
    public String id;

    public TaskStatus status;

    public Map extra;

    public List<String> jobs;

    // Конструктор
    public Task(String id, TaskStatus status, Map extra) {
        this.id = id;
        this.status = status;
        this.extra = extra;
        this.jobs = new ArrayList<String>();

    }

    // Выводим информацию по продукту
    @Override
    public String toString() {
        String data = String.format("ID: %s | Status: %s | Extra: %s",
                this.id, this.status, this.extra);
        return data;
    }
}


/*

CREATE TABLE "products" (
	"id"	INTEGER,
	"good"	TEXT,
	"price"	REAL,
	"category_name"	TEXT,
	PRIMARY KEY("id")
);

 */