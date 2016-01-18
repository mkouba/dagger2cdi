package org.jboss.dagger2cdi;

import javax.enterprise.context.ApplicationScoped;

@Juicy
@ApplicationScoped
public class Alpha implements Bravo {

    @Override
    public void ping() {
    }

}
