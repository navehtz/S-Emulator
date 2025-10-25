package users;

import dto.dashboard.UserDTO;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class UserManager {

    private final Set<UserDTO> usersSet;

    public UserManager() {
        usersSet = new HashSet<>();
    }

    public synchronized void addUser(String username) {
        UserDTO userDTO = new UserDTO(username,
                0,
                0,
                0 ,
                0,
                0);
        usersSet.add(userDTO);
    }

    public synchronized void removeUser(String username) {
        usersSet.removeIf(user -> user.userName().equals(username));
    }

    public synchronized Set<UserDTO> getUsers() {
        return Collections.unmodifiableSet(usersSet);
    }

    public boolean isUserExists(String username) {
        return usersSet.stream()
                .anyMatch(user -> user.userName().equals(username));
    }
}
