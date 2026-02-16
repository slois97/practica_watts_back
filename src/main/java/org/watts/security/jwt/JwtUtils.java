package org.watts.security.jwt;



import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

//Esta clase crea y valida el token JWT
@Component
public class JwtUtils {

    //Clave con la que se firma el token
    private final Key key;

    //Tiempo que dura el token
    private final long EXPIRATION;

    public JwtUtils(@Value("${jwt.secret}") String secret,
                    @Value("${jwt.expiration}") long expiration
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.EXPIRATION = expiration;
    }

    //Función que genera un token para un usuario autenticado
    public String generateToken(User user) {
        // Obtenemos los roles y los convertimos a una lista de Strings
        // Spring Security los guarda como "ROLE_XXXX"
        List<String> authorities = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        // Construimos el token
        return Jwts.builder()
                .setSubject(user.getUsername())//nombre de usuario
                .claim("permissions", authorities)//roles
                .setIssuedAt(new Date())//fecha de creación
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(key, SignatureAlgorithm.HS512)//Firma
                .compact();
    }

    //función que extrae el nombre de usuario del token
    public String getUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    //Función que comprueba si el token es valido
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) { return false; }
    }
}
