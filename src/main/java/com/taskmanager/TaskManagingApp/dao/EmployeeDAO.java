package com.taskmanager.TaskManagingApp.dao;

import com.taskmanager.TaskManagingApp.dto.EmployeeDTO;
import com.taskmanager.TaskManagingApp.models.Employee;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
public class EmployeeDAO {

    private final JdbcTemplate jdbcTemplate;

    public EmployeeDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<EmployeeDTO> employeeRowMapper = new RowMapper<EmployeeDTO>() {
        @Override
        public EmployeeDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new EmployeeDTO(rs.getInt("id"), rs.getString("name"), rs.getString("email"), rs.getBoolean("is_deactivated"), rs.getTimestamp("created_at").toLocalDateTime(), rs.getTimestamp("updated_at").toLocalDateTime());
        }
    };

    public Integer createEmployee(String name, String email) {
        log.info("In EmployeeDAO.createEmployee() name={}, email={}", name, email);

        String sql = """
                INSERT INTO tbl_master_employee
                (name, email, is_deactivated, created_at, updated_at)
                VALUES (?, ?, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                RETURNING id
                """;

        Integer id = jdbcTemplate.queryForObject(sql, Integer.class, name, email);
        log.info("Out EmployeeDAO.createEmployee(), created employee id={}", id);

        return id;
    }

    public boolean existingEmployeeByEmail(String email) {
        log.info("In EmployeeDAO.existingEmployeeByEmail() email={}", email);

        String sql = "SELECT COUNT(*) FROM tbl_master_employee WHERE email=?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);

        boolean exists = count != null && count > 0;
        log.info("Out EmployeeDAO.existingEmployeeByEmail(), exists={}", exists);

        return exists;
    }

    public EmployeeDTO findEmployeeById(Integer id) {
        log.info("In EmployeeDAO.findEmployeeById() id={}", id);

        String sql = "SELECT * FROM tbl_master_employee WHERE id=? AND is_deleted=FALSE";
        List<EmployeeDTO> result = jdbcTemplate.query(sql, employeeRowMapper, id);

        if (result.isEmpty()) {
            log.info("Out EmployeeDAO.findEmployeeById(), no employee found id={}", id);
            return null;
        }

        log.info("Out EmployeeDAO.findEmployeeById(), employee found id={}", id);
        return result.getFirst();
    }

    public Integer updateInfo(Integer id, String name, String email) {
        log.info("In EmployeeDAO.updateInfo() id={}, name={}, email={}", id, name, email);

        StringBuilder sql = new StringBuilder("UPDATE tbl_master_employee SET updated_at = CURRENT_TIMESTAMP");

        List<Object> params = new ArrayList<>();

        if (name != null && !name.isBlank()) {
            sql.append(", name = ?");
            params.add(name);
        }

        if (email != null && !email.isBlank()) {
            sql.append(", email = ?");
            params.add(email);
        }

        sql.append(" WHERE id = ?");
        params.add(id);

        int affected = jdbcTemplate.update(sql.toString(), params.toArray());
        log.info("Out EmployeeDAO.updateInfo(), rows affected={}", affected);

        return affected;
    }

    public void deactivateEmployee(Integer id) {
        log.info("In EmployeeDAO.deactivateEmployee() id={}", id);

        String sql = """
                UPDATE tbl_master_employee
                SET is_deactivated=true, updated_at=CURRENT_TIMESTAMP
                WHERE id=?
                """;

        jdbcTemplate.update(sql, id);
        log.info("Out EmployeeDAO.deactivateEmployee(), employee deactivated id={}", id);
    }

    public void activateEmployee(Integer id) {
        log.info("In EmployeeDAO.activateEmployee() id={}", id);

        String sql = """
                UPDATE tbl_master_employee
                SET is_deactivated=false, updated_at=CURRENT_TIMESTAMP
                WHERE id=?
                """;

        jdbcTemplate.update(sql, id);
        log.info("Out EmployeeDAO.deactivateEmployee(), employee deactivated id={}", id);
    }

    public Map<Integer, EmployeeDTO> findAllEmployee() {
        log.info("In EmployeeDAO.findAllEmployee()");

        String sql = "SELECT * FROM tbl_master_employee WHERE is_deleted=false";

        Map<Integer, EmployeeDTO> result = jdbcTemplate.query(sql, rs -> {
            Map<Integer, EmployeeDTO> map = new HashMap<>();
            int rowNum = 0;
            while (rs.next()) {
                EmployeeDTO employee = employeeRowMapper.mapRow(rs, rowNum++);
                map.put(employee.id(), employee);
            }
            return map;
        });

        log.info("Out EmployeeDAO.findAllEmployee(), totalEmployees={}", result.size());
        return result;
    }

    public void deleteEmployee(Integer id) {
        log.info("In EmployeeDAO.deleteEmployee() id={}", id);

        String unassignTasksSql = """
                UPDATE tbl_master_tasks
                SET assigned_employee_id = NULL,
                    updated_at = CURRENT_TIMESTAMP
                WHERE assigned_employee_id = ?
                  AND is_deleted = false
                """;

        String deleteMappingSql = "DELETE FROM tbl_employee_task_mapping WHERE employee_id=?";

        String softDeleteEmployeeSql = """
                UPDATE tbl_master_employee
                SET is_deleted=true, updated_at=CURRENT_TIMESTAMP
                WHERE id=?
                """;

        jdbcTemplate.update(unassignTasksSql, id);
        jdbcTemplate.update(deleteMappingSql, id);
        jdbcTemplate.update(softDeleteEmployeeSql, id);

        log.info("Out EmployeeDAO.deleteEmployee(), employee soft-deleted id={}", id);
    }

    public Map<Integer, EmployeeDTO> findEmployeesByIds(List<Integer> ids) {
        log.info("In EmployeeDAO.findEmployeesByIds() ids={}", ids);

        String placeholders = String.join(",", ids.stream().map(id -> "?").toList());

        String sql = """
                SELECT *
                FROM tbl_master_employee
                WHERE id IN (%s)
                  AND is_deleted = false
                """.formatted(placeholders);

        Map<Integer, EmployeeDTO> result = jdbcTemplate.query(sql, rs -> {
            Map<Integer, EmployeeDTO> map = new HashMap<>();
            int rowNum = 0;
            while (rs.next()) {
                EmployeeDTO employee = employeeRowMapper.mapRow(rs, rowNum++);
                map.put(employee.id(), employee);
            }
            return map;
        }, ids.toArray());

        log.info("Out EmployeeDAO.findEmployeesByIds(), totalEmployeesFound={}", result.size());
        return result;
    }
}
