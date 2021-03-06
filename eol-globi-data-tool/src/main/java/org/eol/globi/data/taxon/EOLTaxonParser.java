package org.eol.globi.data.taxon;

import com.Ostermiller.util.CSVParser;
import com.Ostermiller.util.LabeledCSVParser;

import java.io.BufferedReader;
import java.io.IOException;

public class EOLTaxonParser implements TaxonParser {

    @Override
    public void parse(BufferedReader reader, TaxonImportListener listener) throws IOException {
        LabeledCSVParser labeledCSVParser = new LabeledCSVParser(new CSVParser(reader));
        labeledCSVParser.changeDelimiter('\t');
        listener.start();
        while (labeledCSVParser.getLine() != null) {
            TaxonTerm term = new TaxonTerm();
            term.setName(labeledCSVParser.getValueByLabel("scientificName"));
            term.setId(labeledCSVParser.getValueByLabel("taxonID"));
            listener.addTerm(term);
        }
        listener.finish();
    }

    @Override
    public int getExpectedMaxTerms() {
        return 2474168;
    }
}
