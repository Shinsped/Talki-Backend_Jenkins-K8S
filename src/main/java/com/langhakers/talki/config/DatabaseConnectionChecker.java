package com.langhakers.talki.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.sql.Connection;

@Component
public class DatabaseConnectionChecker implements CommandLineRunner {
    private final DataSource dataSource;

    public DatabaseConnectionChecker(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            System.out.println("✅ 데이터베이스 연결 성공!");
        } catch (Exception e) {
            System.err.println("❌ 데이터베이스 연결 실패: " + e.getMessage());
            throw e;
        }
    }
}

