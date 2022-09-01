package org.openeuler.sbom.manager.service.reader;

import org.openeuler.sbom.manager.model.Sbom;
import org.openeuler.sbom.manager.model.sbom.SbomDocument;
import org.openeuler.sbom.manager.utils.SbomFormat;

import java.io.File;
import java.io.IOException;

public interface SbomReader {

    void read(String productName, File file) throws IOException;

    void read(String productName, SbomFormat format, byte[] fileContent) throws IOException;

    SbomDocument readToDocument(String productName, SbomFormat format, byte[] fileContent) throws IOException;

    Sbom persistSbom(String productName, SbomDocument sbomDocument);
}
