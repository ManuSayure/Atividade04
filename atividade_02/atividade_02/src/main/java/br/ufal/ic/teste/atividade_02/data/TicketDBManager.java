package br.ufal.ic.teste.atividade_02.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author willy
 */
@Slf4j
@RequiredArgsConstructor
public class TicketDBManager {

    private final ObjectMapper mapper;

    @Getter
    private TicketDB db;

    @SneakyThrows
    public List<Ticket> getTickets(int work_shift) {

        Path file = Files
                .list(Paths.get("."))
                .filter(f -> String.valueOf(f).contains(Integer.toString(work_shift)))
                .findFirst().get();
        
        CollectionType type = mapper.getTypeFactory()
                .constructCollectionType(List.class, Ticket.class);
        
        return (file == null)
                ? new ArrayList<>()
                : mapper.readValue(file.toFile(), type);

    }
    
    /**
     * Verificar se o turno mudou.
     *
     * Se sim, ele salva os dados do turno anterior. Para evitar fazer merge de
     * tickets, a chamada desse método limpa todos os dados anteriores.
     *
     * @param work_shift
     */
    void checkWorkShift(Integer work_shift) {

        if (db != null && db.getWork_shift() != work_shift) {

            save();
        }

        db = new TicketDB(work_shift);
    }

    private void save() {
        log.warn("ticket-db-save: TODO");

        List<Ticket> tickets = db.getTickets();

        String path = String.format("%s-%d", LocalDate.now().toString(),
                db.getWork_shift());

        try {

            mapper.writeValue(new File(path), tickets);
        } catch (IOException ex) {

            log.error("error-saving-ticket-db", ex);
        }
    }
}
