package org.eol.globi.export;

import org.eol.globi.data.GraphDBTestCase;
import org.eol.globi.data.NodeFactory;
import org.eol.globi.data.NodeFactoryException;
import org.eol.globi.data.taxon.CorrectionService;
import org.eol.globi.data.taxon.TaxonServiceImpl;
import org.eol.globi.domain.Location;
import org.eol.globi.domain.PropertyAndValueDictionary;
import org.eol.globi.domain.Specimen;
import org.eol.globi.domain.Study;
import org.eol.globi.domain.Taxon;
import org.eol.globi.domain.Term;
import org.eol.globi.service.TaxonPropertyEnricher;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import java.io.IOException;
import java.io.StringWriter;
import java.text.ParseException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ExporterAssociationAggregatesTest extends GraphDBTestCase {

    @Before
    public void setEnricher() {
        final TaxonPropertyEnricher taxonEnricher = new TaxonPropertyEnricher() {

            @Override
            public void enrich(Taxon taxon) {
                taxon.setExternalId(taxon.getName() + "id");
                taxon.setPath(taxon.getName() + "path");
            }
        };
        nodeFactory = new NodeFactory(getGraphDb(), new TaxonServiceImpl(taxonEnricher, new CorrectionService() {

            @Override
            public String correct(String taxonName) {
                return taxonName;
            }
        }, getGraphDb()));
    }

    @Test
    public void exportCSVNoHeader() throws IOException, NodeFactoryException, ParseException {
        String[] studyTitles = {"myStudy1", "myStudy2"};

        for (String studyTitle : studyTitles) {
            createTestData(null, studyTitle);
        }

        String expected = "\nglobi:assoc:1-2-ATE-5,globi:occur:source:1-2-ATE,http://eol.org/schema/terms/eats,globi:occur:target:1-2-ATE-5,,,,,data source description,,,globi:ref:1" +
                "\nglobi:assoc:9-2-ATE-5,globi:occur:source:9-2-ATE,http://eol.org/schema/terms/eats,globi:occur:target:9-2-ATE-5,,,,,data source description,,,globi:ref:9";

        ExporterAssociationAggregates exporter = new ExporterAssociationAggregates();
        StringWriter row = new StringWriter();
        for (String studyTitle : studyTitles) {
            Study myStudy1 = nodeFactory.findStudy(studyTitle);
            exporter.exportStudy(myStudy1, row, false);
        }


        assertThat(row.getBuffer().toString(), equalTo(expected));
    }

    @Test
    public void exportNoMatchTaxa() throws IOException, NodeFactoryException, ParseException {
        String[] studyTitles = {"myStudy1", "myStudy2"};

        for (String studyTitle : studyTitles) {
            Study myStudy = nodeFactory.getOrCreateStudy(studyTitle, "contributor", "inst", "per", "description", "pubYear", "data source description");
            Specimen specimen = nodeFactory.createSpecimen(PropertyAndValueDictionary.NO_MATCH);
            specimen.ate(nodeFactory.createSpecimen(PropertyAndValueDictionary.NO_MATCH));
            myStudy.collected(specimen);

        }

        String expected = "";

        ExporterAssociationAggregates exporter = new ExporterAssociationAggregates();
        StringWriter row = new StringWriter();
        for (String studyTitle : studyTitles) {
            Study myStudy1 = nodeFactory.findStudy(studyTitle);
            exporter.exportStudy(myStudy1, row, false);
        }


        assertThat(row.getBuffer().toString(), equalTo(expected));
    }

    private void createTestData(Double length, String studyTitle) throws NodeFactoryException, ParseException {
        Study myStudy = nodeFactory.getOrCreateStudy(studyTitle, "contributor", "inst", "per", "description", "pubYear", "data source description");
        Specimen specimen = nodeFactory.createSpecimen("Homo sapiens");
        specimen.setStomachVolumeInMilliLiter(666.0);
        specimen.setLifeStage(new Term("GlOBI:JUVENILE", "JUVENILE"));
        specimen.setPhysiologicalState(new Term("GlOBI:DIGESTATE", "DIGESTATE"));
        specimen.setBodyPart(new Term("GLOBI:BONE", "BONE"));
        Relationship collected = myStudy.collected(specimen);
        Transaction transaction = myStudy.getUnderlyingNode().getGraphDatabase().beginTx();
        try {
            collected.setProperty(Specimen.DATE_IN_UNIX_EPOCH, ExportTestUtil.utcTestTime());
            transaction.success();
        } finally {
            transaction.finish();
        }
        Specimen otherSpecimen = nodeFactory.createSpecimen("Canis lupus");
        otherSpecimen.setVolumeInMilliLiter(124.0);

        specimen.ate(otherSpecimen);
        specimen.ate(otherSpecimen);
        if (null != length) {
            specimen.setLengthInMm(length);
        }

        Location location = nodeFactory.getOrCreateLocation(44.0, 120.0, -60.0);
        specimen.caughtIn(location);
    }

}
