package br.ufms.progweb.api.config;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import br.ufms.progweb.api.model.TipoUser;
import br.ufms.progweb.api.model.User;
import br.ufms.progweb.api.repository.TipoUserRepository;
import br.ufms.progweb.api.repository.UserRepository;
import br.ufms.progweb.api.service.UserService;

/**
 * Cria o primeiro Admin na subida da aplicação. Sem ele ninguém conseguiria
 * cadastrar usuários, já que a rota exige a permissão cadastrar_user.
 * Não faz nada se o e-mail já existir, então é seguro rodar várias vezes.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final TipoUserRepository tipoUserRepository;
    private final UserService userService;

    private final boolean habilitado;
    private final String adminEmail;
    private final String adminSenha;

    public DataInitializer(UserRepository userRepository,
                           TipoUserRepository tipoUserRepository,
                           UserService userService,
                           @Value("${app.seed.enabled:true}") boolean habilitado,
                           @Value("${app.seed.admin.email:admin@progweb.com}") String adminEmail,
                           @Value("${app.seed.admin.senha:admin123}") String adminSenha) {
        this.userRepository = userRepository;
        this.tipoUserRepository = tipoUserRepository;
        this.userService = userService;
        this.habilitado = habilitado;
        this.adminEmail = adminEmail;
        this.adminSenha = adminSenha;
    }

    @Override
    public void run(String... args) {
        if (!habilitado || userRepository.existsByEmail(adminEmail)) {
            return;
        }

        TipoUser tipoAdmin = tipoUserRepository.findByNome("Admin").orElse(null);
        if (tipoAdmin == null) {
            log.warn("Seed ignorado: não existe tipo_user com nome 'Admin'. Cadastre-o no banco primeiro.");
            return;
        }

        User admin = new User("Administrador", "67900000000", adminEmail, "00000000000",
                LocalDate.of(2000, 1, 1), "79000000", "Endereço padrão do administrador", adminSenha);
        admin.setTipoUser(tipoAdmin);

        userService.criar(admin);
        log.info("Admin inicial criado: {}", adminEmail);
    }
}
