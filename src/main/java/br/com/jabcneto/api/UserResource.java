package br.com.jabcneto.api;

import br.com.jabcneto.entity.Role;
import br.com.jabcneto.entity.User;
import br.com.jabcneto.service.UserService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserResource {
    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<List<User>> getUsers() {
        return ResponseEntity.ok().body( userService.getUsers());
    }

    @PostMapping("/user/save")
    public ResponseEntity<User> saveUser(@RequestBody User user) {
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/user/save").toUriString());
        return ResponseEntity.created(uri).body( userService.saveUser(user));
    }
    @PostMapping("/role/save")
    public ResponseEntity<Role> saveRole(@RequestBody Role role) {
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/role/save").toUriString());
        return ResponseEntity.created(uri).body( userService.saveRole(role));
    }

    @PostMapping("/role/add")
    public ResponseEntity<?> saveRole(@RequestBody RoleToUserFrom roleToUser) throws Exception {
        userService.addRoleToUser(roleToUser.getUsername(), roleToUser.getRoleName());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh-token")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if(authorizationHeader!=null && authorizationHeader.startsWith("Bearer ")) {
            try {
                String token = authorizationHeader.substring("Bearer ".length());
                Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
                JWTVerifier verifier = JWT.require(algorithm).build();
                DecodedJWT decodedJWT = verifier.verify(token);
                String username = decodedJWT.getSubject();

                User user = userService.getUser(username);

                String acessToken =
                        JWT.create()
                                .withSubject(user.getUsername())
                                .withExpiresAt(new Date(System.currentTimeMillis() + 1 * 60 *1000))
                                .withIssuer(request.getRequestURL().toString())
                                .withClaim("roles", user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
                                .sign(algorithm);

                response.setHeader("access-token", acessToken);
                response.setHeader("refresh-token", token);
            } catch (Exception e) {
                log.error("login failed {}", e.getMessage());
                response.setHeader("ERROR-X", e.getMessage());
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
        } else {
            throw new RuntimeException("Refresh token is missing!");
        }
    }

    @Data
    class RoleToUserFrom {
        private String username;
        private String roleName;
    }

}
