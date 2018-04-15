package br.ufal.ic.teste.atividade_02.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;

/**
 *
 * @author willy
 */
public class TicketDB {
    
    @Getter
    private final int work_shift;
    
    private final Map<Integer, Ticket> ticketsById      = new HashMap<>();
    private final Map<Integer, Ticket> ticketsByNumber  = new HashMap<>();
    private final Map<String, Customer> customerByPhone = new HashMap<>();
    private final Map<String, Employee> employeeByName  = new HashMap<>();

    public TicketDB(int work_shift) {
        this.work_shift = work_shift;
    }

    public void update(Ticket t) {
        ticketsById.put(t.getEcleticaId(), t);
        ticketsByNumber.put(t.getNumber(), t);
    }

    public List<Ticket> getTickets() {
        
        return ticketsByNumber.values().stream()
                .sorted((t1, t2) -> Integer.compare(t1.getNumber(), t2.getNumber()))
                .collect(Collectors.toList());
    }
    
    public Ticket byId(int id) {
        return ticketsById.get(id);
    }

    public Ticket byNumber(int number) {
        return ticketsByNumber.get(number);
    }

    public Customer getCustomer(String name, String phone, String cep) {
        Customer customer = customerByPhone.get(phone);
        
        if (customer == null) {
            customer = new Customer(name, phone);
            
            CEPClient client = new CEPClient();
            Address address = new Address(cep);
            
            client.cepWebBuscar(address);
            customer.setAddress(address);
            
            customerByPhone.put(phone, customer);
        }
        
        return customer;
    }
    
    public Employee getEmployee(String name) {
        Employee employee = employeeByName.get(name);
        
        if (employee == null) {
            employee = new Employee(name);
            employeeByName.put(name, employee);
        }
        
        return employee;
    }

    public int size() {
        return ticketsById.size();
    }
}
