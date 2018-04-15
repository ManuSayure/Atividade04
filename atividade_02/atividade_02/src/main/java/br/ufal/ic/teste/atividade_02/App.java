package br.ufal.ic.teste.atividade_02;

import io.dropwizard.Application;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import java.io.File;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration.Dynamic;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.servlets.CrossOriginFilter;

import br.ufal.ic.teste.atividade_02.data.DataClient;
import br.ufal.ic.teste.atividade_02.data.TicketDBManager;
import br.ufal.ic.teste.atividade_02.resources.TicketResource;

/**
 *
 * @author willy
 */
@Slf4j
public class App extends Application<AppConfig> {

    public static void main(String[] args) throws Exception {
                    
        new App().run(args);
    }
    
    private TicketDBManager manager;
    
    private ScheduledFuture<?> scheduled;

    @Override
    public String getName() {
        return "cib-client";
    }

    @Override
    public void initialize(Bootstrap<AppConfig> bootstrap) {
        bootstrap.setConfigurationSourceProvider(
            new ResourceConfigurationSourceProvider()); // ler o arquivo de configuração do classpath
    }

    @Override
    public void run(AppConfig config,
            Environment environment) {
        
        log.info("default:path: {}", new File("").getAbsolutePath());
        
        manager = new TicketDBManager(environment.getObjectMapper());
        
        final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        
        scheduled = executor.scheduleAtFixedRate(
                () -> {
                    
                    LocalTime start = LocalTime.of(9, 0);
                    LocalTime end   = LocalTime.of(23, 0);
                    
                    LocalTime now   = LocalTime.now(ZoneOffset.ofHours(-3));
                    
                    if (now.isBefore(start) || now.isAfter(end)) {
                        
                        log.debug("sleeping");
                        return;
                    }
                
                    try {
                        log.info("start-scheduled-thread");

                        DataClient client = new DataClient(manager, config);

                        client.update();

                        log.info("finished-scheduled-thread");
                    }
                    catch (Exception ex) {

                        log.error("thread", ex);
                    }
                    
                }, 0, config.getUpdateRate(), TimeUnit.SECONDS);
        
        
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");

        insecure(environment);
        
        
        
        final TicketResource resource = new TicketResource(manager);
        
        environment.jersey().register(resource);
    }
    
    public static void insecure(Environment env) {
        // https://stackoverflow.com/questions/25775364
        Dynamic corsFilter = env.servlets().addFilter("CORS", CrossOriginFilter.class);
        corsFilter.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
        corsFilter.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET,PUT,POST,DELETE,OPTIONS");
        corsFilter.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
        corsFilter.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin");
        corsFilter.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*");
        corsFilter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
    }
}
