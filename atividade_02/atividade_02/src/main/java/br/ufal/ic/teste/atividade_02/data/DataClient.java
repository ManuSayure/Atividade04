package br.ufal.ic.teste.atividade_02.data;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import br.ufal.ic.teste.atividade_02.AppConfig;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author willy
 */
@Slf4j
@RequiredArgsConstructor
public class DataClient {
    
    private final TicketDBManager manager;
    private final AppConfig config;
    
    public void update() {
        
        DatabaseClient client = new DatabaseClient(manager);

        client.url(config.getUrl())
                .db(config.getDb())
                .user(config.getUser())
                .password(config.getPassword());
        
        TicketDB tickets = client.getTickets();
        
        log.info("total-tickets: {}", tickets.size());
        
        ZoneOffset zone = ZoneOffset.ofHours(-3);
        
        long start = tickets.getTickets().stream()
                .sorted((t1, t2) -> Long.compare(t1.getStartTime(), t2.getStartTime()))
                .map(t -> t.getStartTime())
                .min(Long::compare)
                .orElse(LocalDate.now(zone).atStartOfDay().toInstant(zone).toEpochMilli());

        long end   = LocalDate.now(zone).plusDays(1).atStartOfDay().toInstant(zone).toEpochMilli();
        
        String osUrl = String.format(config.getOsUrl(), start, end);
        
        log.debug("os-url: {}", osUrl);
        
        List<OrderState> states = ClientBuilder.newClient()
                .target(osUrl)
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<OrderState>>() {});
        
        states.stream()
                .sorted((s1, s2) -> Long.compare(s1.getState_time(), s2.getState_time()))
                .forEach(s -> {
            
            Ticket t = tickets.byNumber(s.getOrder_number().intValue());
            
            if (t == null) {
                log.debug("not-found: {}", s.getOrder_number());
                return;
            }
            
            switch (s.getState_name()) {
                
                case "Cozinha":
                    
                    t.setPhoneTime(s.getState_time()); break;
                    
                case "Despacho":
                    
                    t.setKitchenTime(s.getState_time()); break;
                    
                case "Embalado":
                    
                    t.setDispatchTime(s.getState_time()); break;
                    
                case "Entrega":
                	
//                    t.setWaitingTime(s.getState_time()); break;
                    
                case "Finalizado":
                    
//                    t.setEndTime(s.getState_time()); break;
            }
            
            log.debug("ticket-updated: {}", t);
        });
    }

    @RequiredArgsConstructor
    @AllArgsConstructor
    @ToString
    @Getter
    public static class OrderState {
        
        private Long id;
        private Long order_number;
        private String code;
        private Long state_index;
        private String state_name;
        private Long state_time;
        
    }
}
