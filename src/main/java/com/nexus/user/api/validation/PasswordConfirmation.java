package com.nexus.user.api.validation;

// A request that carries a password and a second copy of it typed by the user ("repeat password").
// The two accessors are named so that records with different field names (password /
// confirmPassword, newPassword / confirmNewPassword) can all implement it.
public interface PasswordConfirmation {

    String passwordValue();

    String confirmationValue();
}
