package ru.grniko.user.services;

import ru.grniko.user.dto.UserForm;
import ru.grniko.user.model.User;

import java.util.List;

public interface UserService {

    void createUser(UserForm form);

    void updateUser(User user);

    void deleteById(Long userId);

    Boolean isDeleteUser(Long userId);

    User findById(Long userId);

    List<User> getAll();
}
