package users;

import dto.dashboard.UserDTO;
import exceptions.CreditsException;

import java.util.*;

public class UserManager {

    private final Map<String, UserDTO> nameToUser;

    public UserManager() {
        nameToUser = new HashMap<>();
    }

    public synchronized void addUser(String username) {
        UserDTO userDTO = new UserDTO(
                username,
                0,
                0,
                0 ,
                0,
                0);
        nameToUser.putIfAbsent(username, userDTO);
    }

    public synchronized void removeUser(String username) {
        nameToUser.remove(username);
    }

    public synchronized List<UserDTO> getUsers() {
        return Collections.unmodifiableList((List<? extends UserDTO>) nameToUser.values());
    }

    public boolean isUserExists(String username) {
        return nameToUser.containsKey(username);
    }

    public void incrementPrograms(String userName) {
        nameToUser.computeIfPresent(userName,
                (k, user) -> new UserDTO(
                        user.userName(),
                        user.numProgramsUploaded() + 1,
                        user.numFunctionsUploaded(),
                        user.currentCredits(),
                        user.usedCredits(),
                        user.numOfExecutions()
                ));
    }

    public void incrementSubFunctions(String username) {
        nameToUser.computeIfPresent(username,
                (k, user) -> new UserDTO(
                        user.userName(),
                        user.numProgramsUploaded(),
                        user.numFunctionsUploaded() + 1,
                        user.currentCredits(),
                        user.usedCredits(),
                        user.numOfExecutions()
                ));
    }

    public void addCredits(String userName, long creditsToAdd) {
        nameToUser.computeIfPresent(userName,
                (k, user) -> new UserDTO(
                        user.userName(),
                        user.numProgramsUploaded(),
                        user.numFunctionsUploaded(),
                        user.currentCredits() + creditsToAdd,
                        user.usedCredits(),
                        user.numOfExecutions()
                ));
    }

    public void subtractCredits(String userName, long creditsToSubtract) {
        long newCredits;
        long newUsedCredits;

        try {
            newCredits = nameToUser.get(userName).currentCredits() - creditsToSubtract;
            newUsedCredits = nameToUser.get(userName).usedCredits() + creditsToSubtract;
        } catch (NullPointerException e) {
            throw new CreditsException("User doesn't exist.");
        }

        if (newCredits < 0) {
            String errorMessage = "Execution isn't finished." + System.lineSeparator() +
                    "You don't have enough credits." + System.lineSeparator() +
                    "Current credits amount: " + nameToUser.get(userName).currentCredits();
            throw new CreditsException(errorMessage);
        }

        nameToUser.computeIfPresent(userName,
                (k, user) -> new UserDTO(
                        user.userName(),
                        user.numProgramsUploaded(),
                        user.numFunctionsUploaded(),
                        newCredits,
                        newUsedCredits,
                        user.numOfExecutions()
                ));
    }

    public void incrementExecutions(String userName) {
        nameToUser.computeIfPresent(userName,
                (k, user) -> new UserDTO(
                        user.userName(),
                        user.numProgramsUploaded(),
                        user.numFunctionsUploaded(),
                        user.currentCredits(),
                        user.usedCredits(),
                        user.numOfExecutions() + 1
                ));
    }

    public boolean hasEnoughCredits(String userName, long creditsToSubtract) {
        return nameToUser.get(userName).currentCredits() - creditsToSubtract >= 0;
    }
}
