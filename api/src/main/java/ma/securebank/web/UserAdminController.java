package ma.securebank.web;

import ma.securebank.model.Models.RoleRequest;
import ma.securebank.model.Models.UserView;
import ma.securebank.repo.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Fonctions d'administration des utilisateurs. */
@RestController
public class UserAdminController {

    private final UserRepository users;

    public UserAdminController(UserRepository users) {
        this.users = users;
    }

    @GetMapping("/api/admin/users")
    public List<UserView> list() {
        return users.findAll();
    }

    @DeleteMapping("/api/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        users.delete(id);
    }

    @PutMapping("/api/users/{id}/role")
    public void changeRole(@PathVariable long id, @RequestBody RoleRequest request) {
        users.updateRole(id, request.role());
    }
}
