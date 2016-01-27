package org.jboss.dagger2cdi.simple;

import javax.inject.Inject;

import org.jboss.weld.exceptions.IllegalStateException;

class Thermosiphon implements Pump {
  private final Heater heater;

  @Inject
  Thermosiphon(Heater heater) {
    this.heater = heater;
  }

    @Override
    public void pump() {
        if (heater.isHot()) {
            System.out.println("=> => pumping => =>");
        } else {
            throw new IllegalStateException("Heater is not ready!");
        }
    }
}
