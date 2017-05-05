package vnreal.generators.scenariochain;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * List the parameters that are expected by a ChainElement.
 * 
 * @author Andreas Fischer
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ElementParameter {
	String[] parameters();
}
