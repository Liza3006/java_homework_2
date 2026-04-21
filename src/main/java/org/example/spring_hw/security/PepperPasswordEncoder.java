package org.example.spring_hw.security;

import org.springframework.security.crypto.password.PasswordEncoder;

public class PepperPasswordEncoder implements PasswordEncoder {
  private final PasswordEncoder delegate;
  private final String pepper;

  public PepperPasswordEncoder(PasswordEncoder delegate, String pepper) {
    this.delegate = delegate;
    this.pepper = pepper;
  }

  @Override
  public String encode(CharSequence rawPassword) {
    return delegate.encode(rawPassword + pepper);
  }

  @Override
  public boolean matches(CharSequence rawPassword, String encodedPassword) {
    return delegate.matches(rawPassword + pepper, encodedPassword);
  }
}
