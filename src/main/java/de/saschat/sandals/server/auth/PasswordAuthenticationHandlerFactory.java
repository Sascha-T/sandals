package de.saschat.sandals.server.auth;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PasswordAuthenticationHandlerFactory implements AuthHandlerFactory {
    PasswordChecker checker;
    public PasswordAuthenticationHandlerFactory(PasswordChecker checker) {
        this.checker = checker;
    }

    @Override
    public AuthHandler create() {
        return new PasswordAuthenticationHandler(checker);
    }

    @Override
    public byte id() {
        return 0x02;
    }

    public static class DefaultPasswordChecker implements PasswordChecker {
        public List<PasswordEntry> entries = new ArrayList<>();
        public DefaultPasswordChecker(List<PasswordEntry> entries) {this.entries = entries;}
        public DefaultPasswordChecker(List<String> names, List<String> passwords) {
            if(names.size() != passwords.size())
                throw new RuntimeException("Name list size doesn't match password list size.");
            for (int i = 0; i < names.size(); i++)
                entries.add(new PasswordEntry(
                    names.get(i),
                    passwords.get(i)
                ));
        }

        @Override
        public boolean check(String name, String password) {
            for (PasswordEntry entry: entries)
                if(entry.name.equals(name))
                    if(entry.pass.equals(password))
                        return true;
            return false;
        }

        public record PasswordEntry(String name, String pass) {}
    }

    public interface PasswordChecker {
        boolean check(String name, String password);
    }
}
