package br.ufms.progweb.api.service;

import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.stereotype.Service;
import java.time.Instant;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import br.ufms.progweb.api.model.User;

@Service
public class TokenService {

    private final JwtEncoder encoder;

    public TokenService(JwtEncoder encoder){
        this.encoder = encoder;
    }

    public String gerarToken(User user){
        Instant agora = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
            .subject(user.getEmail())
            .issuedAt(agora)
            .expiresAt(agora.plusSeconds(120 * 60))
            .claim("authorities", user.getTipoUser().permissoes())
            .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
