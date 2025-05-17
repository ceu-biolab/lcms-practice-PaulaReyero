package lipid;

import org.drools.ruleunits.api.DataSource;
import org.drools.ruleunits.api.DataStore;
import org.drools.ruleunits.api.RuleUnitData;
import org.drools.ruleunits.api.RuleUnitInstance;

public class AnnotationUnit implements RuleUnitData {

    private final DataStore<Annotation> annotations;

    public AnnotationUnit() {
        this(DataSource.createStore());
    }

    public AnnotationUnit(DataStore<Annotation> annotations) {
        this.annotations = annotations;
    }

    public DataStore<Annotation> getAnnotations() {
        return annotations;
    }
}