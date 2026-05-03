package com.vtea;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import org.junit.jupiter.api.Test;

public class ArchitectureTest {

    // Nạp toàn bộ code của project VTea vào để máy quét kiểm tra
    private final JavaClasses importedClasses = new ClassFileImporter().importPackages("com.vtea");

    @Test
    public void controllerKhongDuocGoiDatabase() {
        // LUẬT 1: Tầng Controller KHÔNG ĐƯỢC PHÉP chứa code SQL (java.sql)
        ArchRule rule = noClasses()
            .that().resideInAPackage("..controller..")
            .should().dependOnClassesThat().resideInAPackage("java.sql..")
            .allowEmptyShould(true); // Bỏ qua nếu chưa tạo file Controller nào
            
        rule.check(importedClasses);
    }

    @Test
    public void modelPhaiDocLap() {
        // LUẬT 2: Tầng Model (chứa dữ liệu) KHÔNG ĐƯỢC PHÉP gọi ngược lên Controller hay DAO
        ArchRule rule = noClasses()
            .that().resideInAPackage("..model..")
            .should().dependOnClassesThat().resideInAPackage("..controller..")
            .orShould().dependOnClassesThat().resideInAPackage("..dao..")
            .allowEmptyShould(true); // Bỏ qua nếu chưa tạo file Model nào
            
        rule.check(importedClasses);
    }
    
    @Test
    public void daoPhaiDungChuanTen() {
        // LUẬT 3: Bất kỳ file nào nằm trong thư mục 'dao' đều PHẢI có chữ 'DAO' ở cuối tên
        ArchRule rule = classes()
            .that().resideInAPackage("..dao..")
            .should().haveSimpleNameEndingWith("DAO")
            .allowEmptyShould(true); // Bỏ qua nếu thư mục DAO đang trống rỗng
            
        rule.check(importedClasses);
    }
}
