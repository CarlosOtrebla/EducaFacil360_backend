package com.otrebla.educa_facil_360.service;

import com.otrebla.educa_facil_360.dto.Employee.EmployeeRequestDTO;
import com.otrebla.educa_facil_360.dto.Employee.EmployeeResponseDTO;
import com.otrebla.educa_facil_360.dto.PageResponseDTO;
import com.otrebla.educa_facil_360.dto.Employee.EmployeeRequestDTO;
import com.otrebla.educa_facil_360.dto.Employee.EmployeeResponseDTO;
import com.otrebla.educa_facil_360.dto.Employee.EmployeeUpdateDTO;
import com.otrebla.educa_facil_360.exception.NotFoundException;
import com.otrebla.educa_facil_360.exception.RequestParamRequired;
import com.otrebla.educa_facil_360.model.Employee;
import com.otrebla.educa_facil_360.model.Employee;
import com.otrebla.educa_facil_360.repository.EmployeeRepository;
import com.otrebla.educa_facil_360.repository.EmployeeRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class EmployeeService {
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    public EmployeeResponseDTO saveEmployee(EmployeeRequestDTO employeeRequestDTO) {
        System.out.println("Dados do EmployeeRequestDTO: " + employeeRequestDTO);
        Employee employee = new Employee(employeeRequestDTO);
        return new EmployeeResponseDTO(employeeRepository.save(employee));
    }
    
    public PageResponseDTO<EmployeeResponseDTO> getAllByFieldSearch(String field, String search, Pageable pageable) throws RequestParamRequired {
        if (Objects.equals(field, "name")) {
            return new PageResponseDTO<>(employeeRepository
                    .findAllNonDeletedByName(search, pageable).map(EmployeeResponseDTO::new));
        }
        
        throw new RequestParamRequired();
    }
    
    public PageResponseDTO<EmployeeResponseDTO> getAllEmployees(Pageable pageable) {
        return new PageResponseDTO<>(employeeRepository
                .findAll(pageable)
                .map(EmployeeResponseDTO::new));
    }
    
    public EmployeeResponseDTO getEmployeeById(UUID id) throws NotFoundException {
        Employee employee = employeeRepository.findById(id).orElseThrow(NotFoundException::new);
        return new EmployeeResponseDTO(employee);
    }
    
    // Editar
    public EmployeeResponseDTO updateEmployeeById(UUID id, EmployeeUpdateDTO newEmployeeDto) {
        Employee employee = employeeRepository.findById(id).orElseThrow(NotFoundException::new);
        
        // Se algum campo for deixado em branco, mantém o dado antigo
        if (newEmployeeDto.getName() == null) newEmployeeDto.setName(employee.getName());
        if (newEmployeeDto.getEmail() == null) newEmployeeDto.setEmail(employee.getEmail());
        if (newEmployeeDto.getCpf() == null) newEmployeeDto.setCpf(employee.getCpf());
        if (newEmployeeDto.getPhoneNumber() == null) newEmployeeDto.setPhoneNumber(employee.getPhoneNumber());
        
        // Faz o PUT
        BeanUtils.copyProperties(newEmployeeDto, employee,
                "id", "registerTime", "password", "active"); // Campos que eu quero que ele não copie
        
        return new EmployeeResponseDTO(employeeRepository.save(employee));
    }
    
    // Desabilitar
    public void toggleEmployeeActiveById(UUID id) {
        Employee employee = employeeRepository.findById(id).orElseThrow(NotFoundException::new);
        employee.setActive(!employee.getActive());
        employeeRepository.save(employee);
    }
    
    // Excluir
    public void deleteEmployeeById(UUID id) {
        Employee employee = employeeRepository.findNonDeletedById(id).orElseThrow(NotFoundException::new);
        employee.setIsDeleted(true);
        employeeRepository.save(employee);
    }
    
    // Verifica se o email existe na base de dados.
    public boolean existsByEmail(String email) {
        return employeeRepository.findByEmail(email).isPresent();
    }
    
    // Atualiza a senha
    public void updatePasswordByEmail(String email, String newPassword) {
        Optional<Employee> employeeOpt = employeeRepository.findByEmail(email);
        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            employee.setPassword(newPassword);
            employeeRepository.save(employee);
        }
    }
}