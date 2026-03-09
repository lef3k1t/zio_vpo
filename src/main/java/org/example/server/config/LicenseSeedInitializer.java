package org.example.server.config;

import lombok.RequiredArgsConstructor;
import org.example.server.license.entity.LicenseType;
import org.example.server.license.entity.Product;
import org.example.server.license.repo.LicenseTypeRepository;
import org.example.server.license.repo.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class LicenseSeedInitializer {

    private final ProductRepository productRepository;
    private final LicenseTypeRepository licenseTypeRepository;

    @Bean
    public CommandLineRunner seedLicenseDictionaries() {
        return args -> {
            if (productRepository.count() == 0) {
                productRepository.save(Product.builder().name("Antivirus").blocked(false).build());
                productRepository.save(Product.builder().name("VPN").blocked(false).build());
            }

            if (licenseTypeRepository.count() == 0) {
                licenseTypeRepository.save(LicenseType.builder().name("TRIAL").defaultDurationInDays(7).description("Trial 7 days").build());
                licenseTypeRepository.save(LicenseType.builder().name("MONTH").defaultDurationInDays(30).description("Monthly").build());
                licenseTypeRepository.save(LicenseType.builder().name("YEAR").defaultDurationInDays(365).description("Yearly").build());
            }
        };
    }
}