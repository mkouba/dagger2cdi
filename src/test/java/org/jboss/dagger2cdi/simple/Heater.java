package org.jboss.dagger2cdi.simple;

interface Heater {
  void on();
  void off();
  boolean isHot();
}
