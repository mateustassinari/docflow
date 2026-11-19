package br.com.docflow;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Regra de dependência entre camadas:
 * <pre>presentation ──► application ──► domain ◄── infrastructure</pre>
 */
@AnalyzeClasses(packages = "br.com.docflow",
        importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule domainIsPure = noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..application..", "..infrastructure..", "..presentation..",
                    "org.springframework..", "jakarta.persistence..")
            .because("o domínio é o centro: não conhece casos de uso, adaptadores nem frameworks. "
                    + "Mova a dependência para a camada de fora ou crie uma porta em domain");

    @ArchTest
    static final ArchRule applicationDoesNotKnowAdapters = noClasses().that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAnyPackage("..infrastructure..", "..presentation..")
            .because("a aplicação fala com o mundo externo por portas (domain/repository, "
                    + "domain/service, application/port) e recebe comandos, não DTOs HTTP");

    @ArchTest
    static final ArchRule presentationDoesNotKnowInfrastructure = noClasses().that().resideInAPackage("..presentation..")
            .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
            .because("controllers e DTOs convertem modelos de domínio ou de leitura, "
                    + "nunca entidades JPA ou adaptadores");
}
