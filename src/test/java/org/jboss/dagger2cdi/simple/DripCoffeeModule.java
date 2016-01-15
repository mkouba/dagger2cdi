package org.jboss.dagger2cdi.simple;

import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

@Module(includes = PumpModule.class)
class DripCoffeeModule {
  @Provides @Singleton Heater provideHeater() {
    return new ElectricHeater();
  }
}
