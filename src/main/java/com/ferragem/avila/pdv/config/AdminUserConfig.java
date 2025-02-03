package com.ferragem.avila.pdv.config;

import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import com.ferragem.avila.pdv.model.User;
import com.ferragem.avila.pdv.model.enums.RoleValue;
import com.ferragem.avila.pdv.repository.RoleRepository;
import com.ferragem.avila.pdv.repository.UserRepository;

@Configuration
public class AdminUserConfig implements CommandLineRunner {

	private RoleRepository roleRepository;

	private UserRepository userRepository;

	private BCryptPasswordEncoder passwordEncoder;

	@Value("${admin.config.username}")
	private String adminUsername;

	@Value("${admin.config.password}")
	private String adminPassword;

	@Value("${admin.config.email}")
	private String adminEmail;

	@Value("${admin.config.nome}")
	private String adminNome;

	@Value("${admin.config.sobrenome}")
	private String adminSobrenome;

	public AdminUserConfig(RoleRepository roleRepository, UserRepository userRepository,
			BCryptPasswordEncoder passwordEncoder) {
		this.roleRepository = roleRepository;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	@Transactional
	public void run(String... args) throws Exception {
		var roleAdmin = roleRepository.findByNameIgnoreCase(RoleValue.ADMIN.name());
		var userAdmin = userRepository.findByUsername(adminUsername);

		userAdmin.ifPresentOrElse(
				user -> {
					System.out.println(
							"Já existe um usuário administrador com o username: %s.".formatted(user.getUsername()));
				},
				() -> {
					var user = new User();
					user.setUsername(adminUsername);
					user.setPassword(passwordEncoder.encode(adminPassword));
					user.setEmail(adminEmail);
					user.setNome(adminNome);
					user.setSobrenome(adminSobrenome);
					user.setRoles(Set.of(roleAdmin));
					userRepository.save(user);
				});
	}
}
