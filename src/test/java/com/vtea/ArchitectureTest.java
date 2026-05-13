package com.vtea;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.junit.jupiter.api.Test;

public class ArchitectureTest {

    @Test
    public void validateArchitecture() {
        // Load toàn bộ class trong package com.vtea
        JavaClasses importedClasses = new ClassFileImporter().importPackages("com.vtea");

        // 1. Controller KHÔNG ĐƯỢC PHÉP gọi DAO hoặc Database
        ArchRule controllerRule = ArchRuleDefinition.classes()
                .that().resideInAPackage("..controller..")
                .should().onlyDependOnClassesThat()
                .resideOutsideOfPackages("..dao..", "java.sql..");
        controllerRule.check(importedClasses);

        // 2. Tên file DAO phải kết thúc bằng chữ "DAO" và nằm trong package dao
        ArchRule daoNamingRule = ArchRuleDefinition.classes()
                .that().resideInAPackage("..dao..")
                .should().haveSimpleNameEndingWith("DAO");
        daoNamingRule.check(importedClasses);

        // 3. Tên file Controller phải kết thúc bằng chữ "Controller"
        ArchRule controllerNamingRule = ArchRuleDefinition.classes()
                .that().resideInAPackage("..controller..")
                .should().haveSimpleNameEndingWith("Controller");
        controllerNamingRule.check(importedClasses);

        // 4. Service KHÔNG ĐƯỢC PHÉP trả về java.sql.ResultSet (Ngăn leak database details ra ngoài)
        ArchRule serviceRule = ArchRuleDefinition.methods()
                .that().areDeclaredInClassesThat().resideInAPackage("..service..")
                .should().notHaveRawReturnType("java.sql.ResultSet");
        serviceRule.check(importedClasses);
    }
}