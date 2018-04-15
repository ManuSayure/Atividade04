package br.ufal.ic.teste.atividade_02.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.ToString;

/**
 * TODO: poder atualizar o waitingTime, pois, no futuro, o motoboy vai poder
 * confirmar o horário de recebimento e talvez seja mais fácil confiar nesse
 * horário.
 *
 * @author willy
 *
 */
@Getter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Ticket {

    private static final long TOLERANCE = 2 * 60 * 1000; // 02 minutos
    
    private static final long ATTENDING_TOLERANCE = 5 * 60 * 1000; // 05 minutos

    private Integer ecleticaId;
    private Integer number;
    private Integer work_shift;

    private Long startTime; // hora que foi iniciado o atendimento

    private Long phoneTime; // hora que impresso o ticket para ser enviado para a cozinha

    private Long kitchenTime; // hora que saiu da cozinha

    private Long dispatchTime; // hora que foi embalando

    private Long waitingTime; // hora que foi entregue ao motoqueiro para entrega

    private Long endTime; // hora que retornou para a loja

    private double total; // total + service - discount = ticket total
    private double discount;
    private double service;

    private Customer customer;

    private Employee employee;

    private boolean valid = true;

    public void setKitchenTime(Long time) {
        this.kitchenTime = time;
        validate();
    }

    public void setPhoneTime(long time) {
        this.phoneTime = time;
        validate();
    }

    public void setDispatchTime(long time) {
        this.dispatchTime = time;
        validate();
    }

    private Ticket validate() {
        
        valid = true;

        long diffTime = Long.MAX_VALUE;

        if (waitingTime != null) {

            diffTime = waitingTime - startTime + TOLERANCE;
        }

        checkPhoneTime();

        checkKitchenTime(diffTime);

        checkDispatchTime(diffTime);

        return this;
    }

    private void checkPhoneTime() {

        if (phoneTime != null && (phoneTime - startTime) > ATTENDING_TOLERANCE) {
            
            valid = false;
        }
    }

    private void checkKitchenTime(long diffTime) {

        if (kitchenTime != null) {

            if (phoneTime == null) {

                phoneTime = startTime;
            }

            if ((kitchenTime - startTime) > diffTime // se for muito grande
                    || (kitchenTime - phoneTime) < TOLERANCE) { // se for muito pequena

                valid = false;
            }

            if (waitingTime != null && valid) {
                kitchenTime = Math.min(kitchenTime, waitingTime);
            }
        }

        if (waitingTime != null && kitchenTime == null) {

            valid = false;
        }
    }

    private void checkDispatchTime(long diffTime) {

        if (dispatchTime != null) {

            if ((dispatchTime - startTime) > diffTime) {

                valid = false;
            }

            if (waitingTime != null && valid) {
                dispatchTime = Math.min(dispatchTime, waitingTime);
            }
        }
    }

    protected Ticket() { }
    
    public Ticket(Integer ecleticaId, Integer number, Integer work_shift, Long startTime, Long phoneTime,
            Long kitchenTime, Long dispatchTime, Long waitingTime, Long endTime, double total, double discount,
            double service, Customer customer, Employee employee) {
        super();
        this.ecleticaId = ecleticaId;
        this.number = number;
        this.work_shift = work_shift;
        this.startTime = startTime;
        this.phoneTime = phoneTime;
        this.kitchenTime = kitchenTime;
        this.dispatchTime = dispatchTime;
        this.waitingTime = waitingTime;
        this.endTime = endTime;
        this.total = total;
        this.discount = discount;
        this.service = service;
        this.customer = customer;
        this.employee = employee;

        validate();
    }

    public Ticket(Integer number, Integer work_shift, Long startTime, Long waitingTime, Long endTime) {

        this(null, number, work_shift, startTime, null, null, null, waitingTime, endTime, 0d, 0d, 0d, null, null);
    }
}
