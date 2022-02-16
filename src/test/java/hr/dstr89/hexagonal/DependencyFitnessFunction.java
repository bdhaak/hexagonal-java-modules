package hr.dstr89.hexagonal;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaPackage;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import com.tngtech.archunit.library.metrics.*;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AnalyzeClasses(packages = "hr.dstr89.hexagonal", importOptions = ImportOption.DoNotIncludeTests.class)
public class DependencyFitnessFunction {

    public static final int DEPENDENCY_THRESHOLD = 16;

    @ArchTest
    public static final ArchRule applicationRule = ArchRuleDefinition.classes()
            .that().resideInAPackage("..domain..")
            .should().onlyBeAccessed()
            .byAnyPackage("..domain..", "..application..", "foo");

    /*
    @ArchTest
    public static final ArchRule applicationRule2 = layeredArchitecture()
            .layer("Application").definedBy("..application..")
            .layer("Domain").definedBy("..domain..")
            .layer("Infrastructure").definedBy("..infrastructure..")

            .whereLayer("Application").mayNotBeAccessedByAnyLayer()
            .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application")
            .whereLayer("Infrastructure").mayOnlyBeAccessedByLayers("Application");
*/

    @Test
    void shouldKeepCumulativeComponentDependencyUnderTreshold() {

        JavaClasses classes = new ClassFileImporter().importClasspath();
        Set<JavaPackage> packages = classes.getDefaultPackage().getSubpackages();
        // These components can also be created in a package agnostic way, compare MetricsComponents.from(..)
        MetricsComponents<JavaClass> components = MetricsComponents.fromPackages(packages);

        LakosMetrics metrics = ArchitectureMetrics.lakosMetrics(components);

        System.out.println("CCD: " + metrics.getCumulativeComponentDependency());
        System.out.println("ACD: " + metrics.getAverageComponentDependency());
        System.out.println("RACD: " + metrics.getRelativeAverageComponentDependency());
        System.out.println("NCCD: " + metrics.getNormalizedCumulativeComponentDependency());

        assertTrue(metrics.getCumulativeComponentDependency() <= DEPENDENCY_THRESHOLD,
                "Cumulative Component Dependency should not exceed 16");
    }
}
