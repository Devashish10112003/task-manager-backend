package com.taskmanager.TaskManagingApp.dao;

import com.taskmanager.TaskManagingApp.dto.TaskDTO;
import com.taskmanager.TaskManagingApp.models.PriorityType;
import com.taskmanager.TaskManagingApp.models.StatusType;
import com.taskmanager.TaskManagingApp.models.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
public class TaskDAO {

    private final JdbcTemplate jdbcTemplate;

    public TaskDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<TaskDTO> taskRowMapper = new RowMapper<TaskDTO>() {
        @Override
        public TaskDTO mapRow(ResultSet rs, int rowNum) throws SQLException {

            Integer assignedEmployeeId = rs.getObject("assigned_employee_id", Integer.class);
            Array sqlTags = rs.getArray("tags");
            String[] tags = sqlTags != null ? (String[]) sqlTags.getArray() : new String[0];

            return new TaskDTO(rs.getInt("id"), rs.getString("title"), rs.getString("description"), rs.getTimestamp("created_at").toLocalDateTime(), rs.getTimestamp("updated_at").toLocalDateTime(), assignedEmployeeId, rs.getDate("start_date").toLocalDate(), rs.getDate("end_date").toLocalDate(), tags, StatusType.valueOf(rs.getString("status")), PriorityType.valueOf(rs.getString("priority")));
        }
    };

    public Integer createTask(String title, String description, LocalDate startDate, LocalDate endDate, String[] tags, StatusType status, PriorityType priority) {
        log.info("In TaskDAO.createTask() title={}, status={}, priority={}", title, status, priority);

        StringBuilder sql = new StringBuilder("""
                INSERT INTO tbl_master_tasks
                (
                    title,
                    description,
                    created_at,
                    updated_at,
                    start_date,
                    end_date
                """);

        List<Object> params = new ArrayList<>();

        if (tags != null) {
            sql.append(", tags");
        }

        sql.append("""
                , status,
                  priority
                )
                VALUES
                (
                    ?,
                    ?,
                    CURRENT_TIMESTAMP,
                    CURRENT_TIMESTAMP,
                    ?,
                    ?
                """);

        params.add(title);
        params.add(description);
        params.add(startDate);
        params.add(endDate);

        if (tags != null) {
            sql.append(", ?");
            params.add(tags);
        }

        sql.append("""
                ,
                (SELECT id FROM tbl_static_task_status WHERE value = ?),
                (SELECT id FROM tbl_static_task_priority WHERE value = ?)
                )
                RETURNING id
                """);

        params.add(status.name());
        params.add(priority.name());

        Integer id = jdbcTemplate.queryForObject(sql.toString(), Integer.class, params.toArray());

        log.info("Out TaskDAO.createTask(), task created id={}", id);
        return id;
    }

    public Integer updateTask(Integer id, String title, String description, LocalDate startDate, LocalDate endDate, String[] tags, StatusType status, PriorityType priority, Integer employeeId) {
        log.info("In TaskDAO.updateTask() taskId={}", id);

        StringBuilder sql = new StringBuilder("UPDATE tbl_master_tasks SET updated_at = CURRENT_TIMESTAMP");

        List<Object> params = new ArrayList<>();

        if (title != null) {
            sql.append(", title = ?");
            params.add(title);
        }

        if (description != null) {
            sql.append(", description = ?");
            params.add(description);
        }

        if(startDate!=null) {
            sql.append(", start_date = ?");
            params.add(startDate);
        }

        if(endDate!=null)
        {
            sql.append(", end_date = ?");
            params.add(endDate);
        }

        if(tags!=null)
        {
            sql.append(", tags = ?");
            params.add(tags);
        }

        if (employeeId != null) {
            sql.append(", assigned_employee_id = ?");
            params.add(employeeId);

            String assignSql = "INSERT INTO tbl_employee_task_mapping (employee_id, task_id) VALUES (?, ?)";
            jdbcTemplate.update(assignSql, employeeId, id);

            log.info("TaskDAO.updateTask(), task assigned employeeId={}", employeeId);
        }

        if (status != null) {
            sql.append(", status = (SELECT id FROM tbl_static_task_status WHERE value = ?)");
            params.add(status.name());
        }

        if (priority != null) {
            sql.append(", priority = (SELECT id FROM tbl_static_task_priority WHERE value = ?)");
            params.add(priority.name());
        }

        sql.append(" WHERE id = ?");
        params.add(id);

        int affected = jdbcTemplate.update(sql.toString(), params.toArray());
        log.info("Out TaskDAO.updateTask(), rows affected={}", affected);

        return affected;
    }

    public TaskDTO findTaskById(Integer id) {
        log.info("In TaskDAO.findTaskById() id={}", id);

        String sql = """
                SELECT
                    t.id,
                    t.title,
                    t.description,
                    t.created_at,
                    t.updated_at,
                    t.assigned_employee_id,
                    t.start_date,
                    t.end_date,
                    t.tags,
                    s.value AS status,
                    p.value AS priority
                FROM tbl_master_tasks t
                JOIN tbl_static_task_status s ON t.status = s.id
                JOIN tbl_static_task_priority p ON t.priority = p.id
                WHERE t.is_deleted = false AND t.id = ?
                """;

        List<TaskDTO> result = jdbcTemplate.query(sql, taskRowMapper, id);

        if (result.isEmpty()) {
            log.info("Out TaskDAO.findTaskById(), task not found id={}", id);
            return null;
        }

        log.info("Out TaskDAO.findTaskById(), task found id={}", id);
        return result.getFirst();
    }

    public void deleteTaskById(Integer id) {
        log.info("In TaskDAO.deleteTaskById() id={}", id);

        String sql = "UPDATE tbl_master_tasks SET is_deleted=true, updated_at=CURRENT_TIMESTAMP WHERE id=?";
        jdbcTemplate.update(sql, id);

        log.info("Out TaskDAO.deleteTaskById(), task deleted id={}", id);
    }

    public List<TaskDTO> getAllTasks() {
        log.info("In TaskDAO.getAllTasks()");

        String sql = """
                SELECT
                    t.id,
                    t.title,
                    t.description,
                    t.created_at,
                    t.updated_at,
                    t.assigned_employee_id,
                    t.start_date,
                    t.end_date,
                    t.tags,
                    s.value AS status,
                    p.value AS priority
                FROM tbl_master_tasks t
                JOIN tbl_static_task_status s ON t.status = s.id
                JOIN tbl_static_task_priority p ON t.priority = p.id
                WHERE t.is_deleted = false
                """;

        List<TaskDTO> tasks = jdbcTemplate.query(sql, taskRowMapper);
        log.info("Out TaskDAO.getAllTasks(), totalTasks={}", tasks.size());

        return tasks;
    }

    public List<TaskDTO> findTaskByEmployeeId(Integer employeeId) {
        log.info("In TaskDAO.findTaskByEmployeeId() employeeId={}", employeeId);

        String sql = """
                SELECT
                    t.id,
                    t.title,
                    t.description,
                    t.created_at,
                    t.updated_at,
                    etm.employee_id AS assigned_employee_id,
                    t.start_date,
                    t.end_date,
                    t.tags,
                    s.value AS status,
                    p.value AS priority
                FROM tbl_master_tasks t
                JOIN tbl_employee_task_mapping etm ON t.id = etm.task_id
                JOIN tbl_static_task_status s ON t.status = s.id
                JOIN tbl_static_task_priority p ON t.priority = p.id
                WHERE etm.employee_id = ?
                  AND t.is_deleted = false
                """;

        List<TaskDTO> tasks = jdbcTemplate.query(sql, taskRowMapper, employeeId);
        log.info("Out TaskDAO.findTaskByEmployeeId(), totalTasks={}", tasks.size());

        return tasks;
    }

    public List<TaskDTO> findTasksByStatus(StatusType status) {
        log.info("In TaskDAO.findTasksByStatus() status={}", status);

        String sql = """
                SELECT
                    t.id,
                    t.title,
                    t.description,
                    t.created_at,
                    t.updated_at,
                    t.assigned_employee_id,
                    t.start_date,
                    t.end_date,
                    t.tags,
                    s.value AS status,
                    p.value AS priority
                FROM tbl_master_tasks t
                JOIN tbl_static_task_status s ON t.status = s.id
                JOIN tbl_static_task_priority p ON t.priority = p.id
                WHERE s.value = ?
                """;

        List<TaskDTO> tasks = jdbcTemplate.query(sql, taskRowMapper, status.name());
        log.info("Out TaskDAO.findTasksByStatus(), totalTasks={}", tasks.size());

        return tasks;
    }

    public List<TaskDTO> findTasksByPriority(PriorityType priority) {
        log.info("In TaskDAO.findTasksByPriority() priority={}", priority);

        String sql = """
                SELECT
                    t.id,
                    t.title,
                    t.description,
                    t.created_at,
                    t.updated_at,
                    t.assigned_employee_id,
                    t.start_date,
                    t.end_date,
                    t.tags,
                    s.value AS status,
                    p.value AS priority
                FROM tbl_master_tasks t
                JOIN tbl_static_task_status s ON t.status = s.id
                JOIN tbl_static_task_priority p ON t.priority = p.id
                WHERE p.value = ?
                """;

        List<TaskDTO> tasks = jdbcTemplate.query(sql, taskRowMapper, priority.name());
        log.info("Out TaskDAO.findTasksByPriority(), totalTasks={}", tasks.size());

        return tasks;
    }

    public int unassignTask(Integer id) {
        log.info("In TaskDAO.unassignTask() taskId={}", id);

        String deleteMappingSql = "DELETE FROM tbl_employee_task_mapping WHERE task_id = ?";
        jdbcTemplate.update(deleteMappingSql, id);

        String updateTaskSql = "UPDATE tbl_master_tasks SET assigned_employee_id=NULL, updated_at=CURRENT_TIMESTAMP WHERE id=?";
        int affected = jdbcTemplate.update(updateTaskSql, id);

        log.info("Out TaskDAO.unassignTask(), rows affected={}", affected);
        return affected;
    }
}
