package org.opensourceway.sbom.manager.batch.step;

import org.jetbrains.annotations.NotNull;
import org.opensourceway.sbom.cache.constant.CacheConstants;
import org.opensourceway.sbom.clients.license.vo.LicenseInfo;
import org.opensourceway.sbom.constants.BatchContextConstants;
import org.opensourceway.sbom.manager.batch.ExecutionContextUtils;
import org.opensourceway.sbom.manager.dao.LicenseRepository;
import org.opensourceway.sbom.manager.dao.SbomRepository;
import org.opensourceway.sbom.manager.model.License;
import org.opensourceway.sbom.manager.model.Sbom;
import org.opensourceway.sbom.manager.utils.cache.LicenseInfoMapCache;
import org.ossreviewtoolkit.utils.spdx.SpdxConstants;
import org.ossreviewtoolkit.utils.spdx.SpdxException;
import org.ossreviewtoolkit.utils.spdx.SpdxExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FillUpLicenseStep implements Tasklet {

    private static final Logger logger = LoggerFactory.getLogger(FillUpLicenseStep.class);

    @Autowired
    private LicenseInfoMapCache licenseInfoMapCache;

    @Autowired
    private SbomRepository sbomRepository;

    @Autowired
    private LicenseRepository licenseRepository;

    @Override
    public RepeatStatus execute(@NotNull StepContribution contribution, @NotNull ChunkContext chunkContext) {
        ExecutionContext jobContext = ExecutionContextUtils.getJobContext(contribution);
        UUID sbomId = Objects.requireNonNull((UUID) jobContext.get(BatchContextConstants.BATCH_SBOM_ID_KEY), "sbomId is null");
        logger.info("start FillUpLicenseStep sbomId:{}", sbomId);

        Sbom sbom = sbomRepository.findById(sbomId)
                .orElseThrow(() -> new RuntimeException("can't find sbom with id: %s".formatted(sbomId)));
        Map<String, License> existLicenses = licenseRepository.findAll().stream()
                .collect(Collectors.toMap(License::getSpdxLicenseId, Function.identity()));
        sbom.getPackages().stream()
                .filter(pkg -> pkg.getLicenses().size() == 0)
                .forEach(pkg -> {
                    var licenses = parseSpdxLicense(pkg.getLicenseConcluded(), existLicenses);
                    if (ObjectUtils.isEmpty(licenses)) {
                        licenses = parseSpdxLicense(pkg.getLicenseDeclared(), existLicenses);
                    }
                    if (!ObjectUtils.isEmpty(licenses)) {
                        pkg.setLicenses(licenses);
                    }
                });
        licenseRepository.saveAll(existLicenses.values());
        sbomRepository.save(sbom);
        logger.info("finish FillUpLicenseStep sbomId:{}", sbomId);
        return RepeatStatus.FINISHED;
    }

    private Set<License> parseSpdxLicense(String spdxLicense, Map<String, License> existLicenses) {
        if (SpdxConstants.INSTANCE.isNotPresent(spdxLicense)) {
            return null;
        }

        try {
            Map<String, LicenseInfo> licenseInfoMap = licenseInfoMapCache
                    .getLicenseInfoMap(CacheConstants.LICENSE_INFO_MAP_CACHE_KEY_DEFAULT_VALUE);
            SpdxExpression spdxExpression = SpdxExpression.parse(spdxLicense);

            if (!spdxExpression.licenses().stream().distinct().allMatch(licenseInfoMap::containsKey)) {
                return null;
            }

            return spdxExpression.licenses().stream()
                    .distinct()
                    .map(it -> toLicense(it, existLicenses, licenseInfoMap))
                    .collect(Collectors.toSet());
        } catch (SpdxException e) {
            logger.warn("can't parse license: %s".formatted(spdxLicense));
            return null;
        }
    }

    private License toLicense(String spdxLicense, Map<String, License> existLicenses, Map<String, LicenseInfo> licenseInfoMap) {
        License license = existLicenses.get(spdxLicense);
        if (Objects.nonNull(license)) {
            return license;
        }

        LicenseInfo licenseInfo = licenseInfoMap.get(spdxLicense);
        license = new License();
        license.setSpdxLicenseId(spdxLicense);
        license.setIsLegal(true);
        license.setName(licenseInfo.getName());
        license.setUrl(licenseInfo.getReference());
        existLicenses.put(spdxLicense, license);
        return license;
    }
}