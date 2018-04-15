package br.ufal.ic.teste.atividade_02;

import io.dropwizard.Configuration;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author willy
 */
@Getter @Setter
public class AppConfig extends Configuration {

    private String url;
    private String db;
    private String user;
    private String password;
    private String osUrl;
    private Long updateRate;
    
}
