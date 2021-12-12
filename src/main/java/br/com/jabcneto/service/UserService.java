package br.com.jabcneto.service;

import br.com.jabcneto.entity.User;
import br.com.jabcneto.entity.Role;

import java.util.List;

public interface UserService {
    User saveUser(User user);
    Role saveRole(Role role);
    void addRoleToUser(String username, String roleName) throws Exception;
    User getUser(String username);
    List<User> getUsers();
}
