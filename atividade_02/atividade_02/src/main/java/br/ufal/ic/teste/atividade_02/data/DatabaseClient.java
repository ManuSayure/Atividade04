package br.ufal.ic.teste.atividade_02.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Locale;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author willy
 */
@Setter
@Accessors(fluent = true)
@RequiredArgsConstructor
@Slf4j
public class DatabaseClient {
    
    private final TicketDBManager manager;
    
    private String db;
    private String user;
    private String password;
    private String url;

    @SneakyThrows
    public TicketDB getTickets() {
        // This will load the MySQL driver, each DB has its own driver
        Class.forName("com.mysql.jdbc.Driver");
        // Setup the connection with the DB
        
        String dbUrl = String.format("jdbc:mysql://%s/%s?user=%s&password=%s",
                url, db, user, password);
        
        Connection connect = DriverManager.getConnection(dbUrl);
        connect.setAutoCommit(false);

        // Statements allow to issue SQL queries to the database
        ResultSet maxResult = connect.createStatement().executeQuery(
                "select max(Cod_Turno) from frans006 where "
                        + "NumeroPedDely <> 0 and FLAG_CANC = '*';");
        
        Integer work_shift = null;
        
        if (maxResult.next()) {
            
            work_shift = maxResult.getInt(1);
            maxResult.close();
        }
        else {
            
            return null;
        }
        
        log.info("work_shift: {}", work_shift);
        
        manager.checkWorkShift(work_shift);
        
        // OrdemLancamento: id
        PreparedStatement statement = connect.prepareStatement(
                "SELECT OrdemLancamento, COD_CTRL, NO_TICKET, DT_HO_ABRE, "
                + "VLR_TOTAL, VLR_DESC_TOT, VLR_SERV, "
                + "Cod_Turno, Cliente, TelCli, Entregador, "
                + "HrSaida, HrVolta, NumeroPedDely, enviado_motoboy "
                + "FROM frans006 where NumeroPedDely <> 0 and "
                + "FLAG_CANC = '*' and Cod_Turno = ? order by Cod_Turno desc;");
        
        statement.setInt(1, work_shift);
        statement.setFetchSize(5);
        
        // Result set get the result of the SQL query
        ResultSet rs = statement.executeQuery();
        
        TicketDB db = manager.getDb();
        
        while (rs.next()) {
            
            TicketData t = new TicketData(rs);
            
            Customer customer = db.getCustomer(t.Cliente, t.TelCli, t.cep);
            Employee employee = db.getEmployee(t.Entregador);
            
            Ticket ticket = t.ticket(customer, employee);
            
            if (ticket != null) {
                db.update(ticket);
            }
            
            log.debug("ticket: {}", ticket);
        }

        rs.close();
        statement.close();
        
        return db;
    }
    
    @Getter
    @ToString
//    @ToString (of = {"NumeroPedDely", "Cod_Turno", "DT_HO_ABRE", "HrSaida", "HrVolta" })
    public static class TicketData {
        
        private long OrdemLancamento;
        
        private int COD_CTRL, NO_TICKET, Cod_Turno, NumeroPedDely;
        
        private String DT_HO_ABRE, Cliente, TelCli, cep, Entregador, enviado_motoboy;
        
        private double VLR_TOTAL, VLR_DESC_TOT, VLR_SERV;
        
        private String HrSaida, HrVolta;

        public TicketData(ResultSet resultset) {
            
            try {
                OrdemLancamento = (long) resultset.getDouble("OrdemLancamento");
                
                COD_CTRL = resultset.getInt("COD_CTRL");
                NO_TICKET = resultset.getInt("NO_TICKET");
                Cod_Turno = resultset.getInt("Cod_Turno");
                NumeroPedDely = resultset.getInt("NumeroPedDely");
                
                DT_HO_ABRE = resultset.getString("DT_HO_ABRE");
                Cliente = resultset.getString("Cliente");
                TelCli = resultset.getString("TelCli");
                Entregador = resultset.getString("Entregador");
                enviado_motoboy = resultset.getString("enviado_motoboy");
                
                VLR_TOTAL = resultset.getDouble("VLR_TOTAL");
                VLR_DESC_TOT = resultset.getDouble("VLR_DESC_TOT");
                VLR_SERV = resultset.getDouble("VLR_SERV");
                
                HrSaida = resultset.getString("HrSaida");
                HrVolta = resultset.getString("HrVolta");
                
            } catch (SQLException ex) {
                throw new IllegalArgumentException(ex);
            }
        }
        
        
        Ticket ticket(Customer customer, Employee employee) {
            
            try {
                DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                        .appendPattern("dd/MM/yyyy HH:mm:ss")
                        .toFormatter(Locale.forLanguageTag("pt-br"));
                
                DateTimeFormatter timeForm = new DateTimeFormatterBuilder()
                        .appendPattern("HH:mm:ss")
                        .toFormatter(Locale.forLanguageTag("pt-br"));
                
                
                LocalDateTime time = LocalDateTime.parse(DT_HO_ABRE, formatter);
                
                ZoneOffset zone = ZoneOffset.ofHours(-3);
                
                long startTime = time.toInstant(zone).toEpochMilli();
                
                Long waitingTime = (HrSaida == null || HrSaida.isEmpty())
                        ? null
                        : time.toLocalDate().atStartOfDay().toInstant(zone).toEpochMilli()
                            + LocalTime.parse(HrSaida, timeForm).getLong(ChronoField.SECOND_OF_DAY) * 1000L;
                
                Long endTime = (HrVolta == null || HrVolta.isEmpty() || HrVolta.equals("0"))
                        ? null
                        : time.toLocalDate().atStartOfDay().toInstant(zone).toEpochMilli()
                            + LocalTime.parse(HrVolta, timeForm).getLong(ChronoField.SECOND_OF_DAY) * 1000L;
                
                return new Ticket(COD_CTRL, NumeroPedDely, Cod_Turno, startTime, null,
                        null, null, waitingTime, endTime, VLR_TOTAL, 
                        VLR_DESC_TOT, VLR_SERV, customer, employee);
                
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }
    }
}
