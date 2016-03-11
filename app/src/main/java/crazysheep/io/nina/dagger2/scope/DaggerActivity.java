package crazysheep.io.nina.dagger2.scope;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Scope;

/**
 * scope annotaion
 *
 * Created by crazysheep on 16/3/11.
 */
@Scope
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface DaggerActivity {
}
