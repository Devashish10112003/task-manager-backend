package com.taskmanager.TaskManagingApp.service;

import ch.qos.logback.core.util.StringUtil;
import com.taskmanager.TaskManagingApp.dao.EmployeeDAO;
import com.taskmanager.TaskManagingApp.dto.EmployeeDTO;
import com.taskmanager.TaskManagingApp.models.Employee;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.taskmanager.TaskManagingApp.constants.ErrorMessages.*;

@Service
@Slf4j
public class EmployeeService {

    private final EmployeeDAO employeeDAO;

    public EmployeeService(EmployeeDAO employeeDAO) {
        this.employeeDAO = employeeDAO;
    }

    public Integer createEmployee(String name, String email) {
        log.info("In EmployeeService.createEmployee() name={}, email={}", name, email);

        if (StringUtil.isNullOrEmpty(name)) {
            log.warn("Out EmployeeService.createEmployee(), name is null/empty");
            throw new IllegalArgumentException(NAME_CANT_BE_NULL);
        }
        if (StringUtil.isNullOrEmpty(email)) {
            log.warn("Out EmployeeService.createEmployee(), email is null/empty");
            throw new IllegalArgumentException(EMAIL_CANT_BE_NULL);
        }
        if (employeeDAO.existingEmployeeByEmail(email)) {
            log.warn("Out EmployeeService.createEmployee(), email already exists={}", email);
            throw new IllegalArgumentException(String.format(EMAIL_ALREADY_EXISTS,email));
        }

        Integer id = employeeDAO.createEmployee(name, email);
        log.info("Out EmployeeService.createEmployee(), employee created with id={}", id);
        return id;
    }

    public EmployeeDTO getEmployeeById(Integer id) {
        log.info("In EmployeeService.getEmployeeById() id={}", id);

        EmployeeDTO employee = employeeDAO.findEmployeeById(id);
        if (employee == null) {
            log.warn("Out EmployeeService.getEmployeeById(), employee not found id={}", id);
            throw new IllegalStateException(String.format(EMPLOYEE_NOT_FOUND, id));
        }

        log.info("Out EmployeeService.getEmployeeById(), employee found id={}", id);
        return employee;
    }

    public EmployeeDTO updateEmployeeInfo(Integer id, String name, String email) {
        log.info("In EmployeeService.updateEmployeeInfo() id={}, name={}, email={}", id, name, email);

        if (StringUtil.isNullOrEmpty(name) && StringUtil.isNullOrEmpty(email)) {
            log.warn("Out EmployeeService.updateEmployeeInfo(), all update fields are null");
            throw new IllegalArgumentException(ALL_INFO_NULL);
        }

        EmployeeDTO existingEmployee = employeeDAO.findEmployeeById(id);
        if (existingEmployee == null) {
            log.warn("Out EmployeeService.updateEmployeeInfo(), employee not found id={}", id);
            throw new IllegalStateException(String.format(EMPLOYEE_NOT_FOUND,id));
        }

        if (!StringUtil.isNullOrEmpty(email) && !email.equals(existingEmployee.email()) && employeeDAO.existingEmployeeByEmail(email)) {
            log.warn("Out EmployeeService.updateEmployeeInfo(), email already exists email={}", email);
            throw new IllegalArgumentException(String.format(EMAIL_ALREADY_EXISTS,email));
        }

        int affected = employeeDAO.updateInfo(id, name, email);
        log.info("EmployeeService.updateEmployeeInfo(), rows affected={}", affected);

        log.info("Out EmployeeService.updateEmployeeInfo(), employee updated id={}", id);
        return getEmployeeById(id);
    }

    public EmployeeDTO deactivateEmployee(Integer id) {
        log.info("In EmployeeService.deactivateEmployee() id={}", id);

        EmployeeDTO employee = employeeDAO.findEmployeeById(id);
        if (employee == null) {
            log.warn("Out EmployeeService.deactivateEmployee(), employee not found id={}", id);
            throw new IllegalStateException(String.format(EMPLOYEE_NOT_FOUND,id));
        }

        if (employee.isDeactivated()) {
            log.warn("Out EmployeeService.deactivateEmployee(), employee already deactivated id={}", id);
            throw new IllegalStateException(String.format(EMPLOYEE_ALREADY_DEACTIVATED,id));
        }

        employeeDAO.deactivateEmployee(id);
        log.info("Out EmployeeService.deactivateEmployee(), employee deactivated id={}", id);

        return employeeDAO.findEmployeeById(id);
    }

    public EmployeeDTO activateEmployee(Integer id) {
        log.info("In EmployeeService.activateEmployee() id={}", id);

        EmployeeDTO employee = employeeDAO.findEmployeeById(id);
        if (employee == null) {
            log.warn("Out EmployeeService.activateEmployee(), employee not found id={}", id);
            throw new IllegalStateException(String.format(EMPLOYEE_NOT_FOUND,id));
        }

        if (!employee.isDeactivated()) {
            log.warn("Out EmployeeService.activateEmployee(), employee already activated id={}", id);
            throw new IllegalStateException(String.format(EMPLOYEE_ALREADY_ACTIVATED,id));
        }

        employeeDAO.activateEmployee(id);
        log.info("Out EmployeeService.activateEmployee(), employee activated id={}", id);

        return employeeDAO.findEmployeeById(id);
    }

    public Map<Integer, EmployeeDTO> getEmployee() {
        log.info("In EmployeeService.getEmployee()");

        Map<Integer, EmployeeDTO> employees = employeeDAO.findAllEmployee();
        log.info("Out EmployeeService.getEmployee(), totalEmployees={}", employees.size());

        return employees;
    }

    public void deleteEmployee(Integer id) {
        log.info("In EmployeeService.deleteEmployee() id={}", id);

        EmployeeDTO existingEmployee = employeeDAO.findEmployeeById(id);
        if (existingEmployee == null) {
            log.warn("Out EmployeeService.deleteEmployee(), employee not found id={}", id);
            throw new IllegalStateException(String.format(EMPLOYEE_NOT_FOUND,id));
        }

        employeeDAO.deleteEmployee(id);
        log.info("Out EmployeeService.deleteEmployee(), employee deleted id={}", id);
    }

    public Map<Integer, EmployeeDTO> getEmployeesByIds(List<Integer> ids) {
        log.info("In EmployeeService.getEmployeesByIds() ids={}", ids);

        Map<Integer, EmployeeDTO> employees = employeeDAO.findEmployeesByIds(ids);
        log.info("Out EmployeeService.getEmployeesByIds(), totalEmployeesFound={}", employees.size());

        return employees;
    }
}
