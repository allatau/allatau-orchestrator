package org.wscp.server.sqlite;

import com.google.gson.Gson;
import jakarta.inject.Singleton;
import org.sqlite.JDBC;
import org.wscp.model.Task;
import org.wscp.model.TaskStatus;

import java.sql.*;
import java.util.*;

@Singleton
public class DbHandlerImpl implements DbHandler {

    // Константа, в которой хранится адрес подключения
    private static final String CON_STR = "jdbc:sqlite:data/wscp.sqlite";
    private static DbHandlerImpl instance = null;

    public static synchronized DbHandlerImpl getInstance() throws SQLException {
        if (instance == null)
            instance = new DbHandlerImpl();
        return instance;
    }

    // Объект, в котором будет храниться соединение с БД
    private final Connection connection;

    public DbHandlerImpl() throws SQLException {

        // Регистрируем драйвер, с которым будем работать
        // в нашем случае Sqlite
        DriverManager.registerDriver(new JDBC());
        // Выполняем подключение к базе данных
        this.connection = DriverManager.getConnection(CON_STR);

    }

    public List<Task> getAllTasks() {

        // Statement используется для того, чтобы выполнить sql-запрос
        try (Statement statement = this.connection.createStatement()) {
            // В данный список будем загружать наши продукты, полученные из БД
            List<Task> tasks = new ArrayList<Task>();
            // В resultSet будет храниться результат нашего запроса,
            // который выполняется командой statement.executeQuery()
            ResultSet resultSet = statement.executeQuery("SELECT id, status, extra FROM tasks");

            Gson gson = new Gson();

            // Проходимся по нашему resultSet и заносим данные в products
            while (resultSet.next()) {
                Map extraMap = gson.fromJson(resultSet.getString("extra"), Map.class);
                String task_id= resultSet.getString("id");

                Task current_task = new Task(
                        task_id,
                        TaskStatus.valueOf(resultSet.getString("status").toUpperCase()),
                        extraMap
                );

                List<String> jobs = this.getJobsByTaskId(task_id);
                current_task.jobs = jobs;

                System.out.println("sql id: " + task_id + " " + resultSet.getString("status") + ", jobs counts: " + current_task.jobs.size());

                tasks.add(current_task);
            }
            // Возвращаем наш список
            return tasks;

        } catch (SQLException e) {
            e.printStackTrace();
            // Если произошла ошибка - возвращаем пустую коллекцию
            return Collections.emptyList();
        }
    }

    // Добавление продукта в БД
    public void addTask(Task task) {
        // Создадим подготовленное выражение, чтобы избежать SQL-инъекций
        try (PreparedStatement statement = this.connection.prepareStatement(
                "REPLACE INTO tasks(`id`,`status`, `extra`) " +
                        "VALUES(?, ?, ?)")) {


            Gson gson = new Gson();
            String jsonExtra = gson.toJson(task.extra);

            statement.setObject(1, task.id);
            statement.setObject(2, task.status);
            statement.setObject(3, jsonExtra);
            // Выполняем запрос
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void updateTask(Task task) {
        try (PreparedStatement statement = this.connection.prepareStatement(
                "UPDATE tasks SET status=?, extra=? " +
                        "WHERE id = ?")) {

            Gson gson = new Gson();
            String jsonExtra = gson.toJson(task.extra);

            statement.setObject(1, task.status);
            statement.setObject(2, jsonExtra);
            statement.setObject(3, task.id);
            // Выполняем запрос
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // Удаление продукта по id
    public void deleteTask(String id) {
        try (PreparedStatement statement = this.connection.prepareStatement(
                "DELETE FROM tasks WHERE id = ?")) {
            statement.setObject(1, id);
            // Выполняем запрос
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    // Получение по id
//    public Task getTaskById(String id) {
//
//        // Statement используется для того, чтобы выполнить sql-запрос
//        try (Statement statement = this.connection.createStatement()) {
//            // В данный список будем загружать наши продукты, полученные из БД
//            List<Task> tasks = new ArrayList<Task>();
//            // В resultSet будет храниться результат нашего запроса,
//            // который выполняется командой statement.executeQuery()
//            ResultSet resultSet = statement.executeQuery("SELECT id, status, extra FROM tasks");
//
//            Gson gson = new Gson();
//            Map extraMap = gson.fromJson(resultSet.getString("extra"), Map.class);
//
//
//            // Проходимся по нашему resultSet и заносим данные в products
//            while (resultSet.next()) {
//                if(resultSet.getString("id").equals(id)) {
//                    tasks.add(new Task(
//                            resultSet.getString("id"),
//                            TaskStatus.valueOf(resultSet.getString("status").toUpperCase()),
//                            extraMap
//                    ));
//                }
//
//            }
//            // Возвращаем наш список
//            return tasks.get(0);
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//            // Если произошла ошибка - возвращаем пустую коллекцию
//            return null;
//        }
//    }
    public Task getTaskById(String id) {
        String sql = "SELECT id, status, extra FROM tasks WHERE id = ?";

        try (PreparedStatement statement = this.connection.prepareStatement(sql);) {
            statement.setString(1, id);
            ResultSet resultSet = statement.executeQuery();


            if (resultSet.next()) {
                String taskId = resultSet.getString("id");
                TaskStatus status = TaskStatus.valueOf(resultSet.getString("status").toUpperCase()) ;

                Gson gson = new Gson();
                Map extraMap = gson.fromJson(resultSet.getString("extra"), Map.class);

                Task current_task = new Task(id, status, extraMap);

                List<String> jobs = this.getJobsByTaskId(taskId);
                current_task.jobs = jobs;


                return current_task;
            }


        } catch (SQLException e) {
            e.printStackTrace();

            return null;
        }

        return null;
    }


    public void recordJobIdToTaskById(String taskId, String jobId) {
        // Создадим подготовленное выражение, чтобы избежать SQL-инъекций
        try (PreparedStatement statement = this.connection.prepareStatement(
                "REPLACE INTO task_job(`job_id`,`task_id`) " +
                        "VALUES(?, ?)")) {


            statement.setObject(1, taskId);
            statement.setObject(2, jobId);

            // Выполняем запрос
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private List<String> getJobsByTaskId(String task_id) {

        // Statement используется для того, чтобы выполнить sql-запрос
        try (Statement statement = this.connection.createStatement()) {
            // В данный список будем загружать наши продукты, полученные из БД
            List<String> jobs = new ArrayList<String>();
            // В resultSet будет храниться результат нашего запроса,
            // который выполняется командой statement.executeQuery()
            ResultSet resultSet = statement.executeQuery("SELECT job_id, task_id FROM task_job");

            Gson gson = new Gson();

            // Проходимся по нашему resultSet и заносим данные в products
            while (resultSet.next()) {
                if(resultSet.getString("task_id").equals(task_id)) {
                    jobs.add(resultSet.getString("job_id"));
                }
            }

            // Возвращаем наш список
            return jobs;

        } catch (SQLException e) {
            e.printStackTrace();
            // Если произошла ошибка - возвращаем пустую коллекцию
            return Collections.emptyList();
        }
    }


}
