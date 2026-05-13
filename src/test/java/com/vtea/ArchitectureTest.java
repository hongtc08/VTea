package com.vtea;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

public class ArchitectureTest {

    private final JavaClasses importedClasses =
            new ClassFileImporter().importPackages("com.vtea");

    @Test
    void controllerShouldNotAccessDaoOrSqlDirectly() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..controller..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..dao..", "java.sql..");

        rule.check(importedClasses);
    }

    @Test
    void controllerNameShouldEndWithController() {
        ArchRule rule = classes()
                .that().resideInAPackage("..controller..")
                .should().haveSimpleNameEndingWith("Controller");

        rule.check(importedClasses);
    }

    @Test
    void daoNameShouldEndWithDAO() {
        ArchRule rule = classes()
                .that().resideInAPackage("..dao..")
                .should().haveSimpleNameEndingWith("DAO");

        rule.check(importedClasses);
    }

    @Test
    void serviceShouldNotReturnResultSet() {
        ArchRule rule = methods()
                .that().areDeclaredInClassesThat().resideInAPackage("..service..")
                .should().notHaveRawReturnType("java.sql.ResultSet");

        rule.check(importedClasses);
    }

    @Test
    void modelShouldNotDependOnControllerDaoServiceOrJavaFx() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..model..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        "..controller..",
                        "..dao..",
                        "..service..",
                        "javafx..",
                        "java.sql.."
                );

        rule.check(importedClasses);
    }

    @Test
    void serviceShouldNotDependOnControllerOrJavaFx() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..service..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..controller..", "javafx..");

        rule.check(importedClasses);
    }

    @Test
    void daoShouldNotDependOnControllerServiceOrJavaFx() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..dao..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..controller..", "..service..", "javafx..");

        rule.check(importedClasses);
    }
}