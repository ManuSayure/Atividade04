package br.ufal.ic.teste.atividade_02.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import br.ufal.ic.teste.atividade_02.data.TicketDB;
import br.ufal.ic.teste.atividade_02.data.TicketDBManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author willy
 */
@Path("/tickets")
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
@Slf4j
public class TicketResource {

    private final TicketDBManager manager;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(
            @QueryParam(value = "work_shift") Integer work_shift,
            @QueryParam(value = "start") Long start,
            @QueryParam(value = "end") Integer end) {
        
        TicketDB db = manager.getDb();
        
        if (start != null && end != null) {
            log.warn("not-implemented");
        }
        
        return (work_shift == null)
                ? withCORS(Response.ok(db == null ? "[]" : db.getTickets()))
                : withCORS(Response.ok(manager.getTickets(work_shift)));
    }
    
    private Response withCORS(ResponseBuilder builder) {
        
        return builder
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Expose-Headers", "Content-Length,X-JSON")
                .header("Access-Control-Allow-Methods", "HEAD,GET,POST,PUT,PATCH,DELETE")
                .header("Access-Control-Allow-Headers", "Origin,Content-Type,X-Auth-Token,X-Requested-With,Authorization,Accept,Client-Security-Token")
                .build();
    }
}
